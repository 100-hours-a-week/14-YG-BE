package com.moogsan.moongsan_backend.participantchat.application.service.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.participantchat.presentation.dto.query.response.ChatMessagePageResponse;
import com.moogsan.moongsan_backend.participantchat.presentation.dto.query.response.ChatMessageResponse;
import com.moogsan.moongsan_backend.participantchat.domain.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.participantchat.domain.entity.ChatParticipant;
import com.moogsan.moongsan_backend.participantchat.domain.entity.ChatRoom;
import com.moogsan.moongsan_backend.participantchat.domain.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.participantchat.domain.exception.specific.NotParticipantException;
import com.moogsan.moongsan_backend.participantchat.application.mapper.ChatMessageQueryMapper;
import com.moogsan.moongsan_backend.participantchat.domain.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.participantchat.domain.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.moogsan.moongsan_backend.global.util.ObjectIdScoreUtil.toScore;
import static com.moogsan.moongsan_backend.participantchat.domain.constant.ParticipantChatConstants.CASHE_REDIS_KEY;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GetPastMessages {

    private static final int PAGE_SIZE = 20;
    private final ChatMessageQueryMapper chatMessageQueryMapper;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public ChatMessagePageResponse getPastMessages(
            User currentUser,
            Long chatRoomId,
            String cursorId,
            boolean isPrev
    ) {
        // 유효성 검사
        fetchAndValidate(chatRoomId, currentUser.getId());

        // Redis 조회 (방향 분기)
        String redisKey = CASHE_REDIS_KEY + chatRoomId;
        Set<String> cachedMembers = fetchCachedMessageIds(redisKey, cursorId, isPrev);

        // 캐시 메시지를 ID로 변환
        List<String> idList = extractId(cachedMembers);
        if (cursorId != null) idList.removeIf(id -> id.equals(cursorId));

        // 부족한 메시지 MongoDB 조회
        List<ChatMessageDocument> dbDocs = fetchMongoDB(idList, chatRoomId, cursorId, isPrev, redisKey);

        // 5) ID 병합
        Stream<ObjectId> fromCache = idList.stream().limit(PAGE_SIZE).map(ObjectId::new);
        Stream<ObjectId> fromDb = dbDocs.stream().map(doc -> new ObjectId(doc.getId()));
        List<ObjectId> finalIds = Stream.concat(fromCache, fromDb)
                .distinct()
                .limit(PAGE_SIZE)
                .collect(Collectors.toList());

        // 6) 최종 정렬 및 조회
        Sort.Direction finalDirection = isPrev ? Sort.Direction.DESC : Sort.Direction.ASC;
        Query finalQ = new Query(Criteria.where("_id").in(finalIds));
        finalQ.with(Sort.by(finalDirection, "_id"));
        List<ChatMessageDocument> finalPage = mongoTemplate.find(finalQ, ChatMessageDocument.class);

        // 6-1) 정방향 응답을 위해 정렬 (오름차순으로 보내기)
        if (isPrev) {
            Collections.reverse(finalPage);
        }

        // 7) hasNext 계산
        boolean hasNext = finalPage.size() == PAGE_SIZE && (!dbDocs.isEmpty() || cachedMembers.size() > PAGE_SIZE);

        // 8) DTO 변환
        List<ChatMessageResponse> responses = assemblePageResponse(finalPage);

        // 다음 커서 지정
        String beforeCursor = null;
        if (!finalPage.isEmpty()) {
            beforeCursor = isPrev ? finalPage.getFirst().getId() : finalPage.getLast().getId();
        }

        return ChatMessagePageResponse.builder()
                .chatMessageResponses(responses)
                .beforeCursorId(beforeCursor)
                .hasBefore(hasNext)
                .build();
    }

    private void fetchAndValidate(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);
        boolean isParticipant = chatParticipantRepository
                .existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoom.getId(), userId);
        if (!isParticipant) throw new NotParticipantException("참여자만 메세지를 조회할 수 있습니다.");
    }

    private Set<String> fetchCachedMessageIds(String redisKey, String cursorId, boolean isPrev) {
        double cursorScore = cursorId != null
                ? toScore(cursorId)
                : (isPrev ? Double.MAX_VALUE : Double.MIN_VALUE);

        // 방향 분기 Redis 조회
        return isPrev
                ? redisTemplate.opsForZSet().reverseRangeByScore(redisKey, cursorScore - 1, Double.NEGATIVE_INFINITY, 0, PAGE_SIZE + 1)
                : redisTemplate.opsForZSet().rangeByScore(redisKey, cursorScore + 1, Double.MAX_VALUE, 0, PAGE_SIZE + 1);
    }

    private List<String> extractId(Set<String> cachedMembers) {
        return cachedMembers.stream()
                .map(member -> {
                    member = member.trim();
                    if (member.startsWith("{")) {
                        try {
                            JsonNode node = objectMapper.readTree(member);
                            return node.get("id").asText();
                        } catch (JsonProcessingException e) {
                            log.warn("Invalid JSON in cache: {}", member);
                            return null;
                        }
                    }
                    return member;
                })
                .filter(Objects::nonNull)
                .filter(ObjectId::isValid)
                .distinct()
                .toList();
    }

    private List<ChatMessageDocument> fetchMongoDB(
            List<String> idList, Long chatRoomId, String cursorId, boolean isPrev, String redisKey){

        List<ChatMessageDocument> docs = new ArrayList<>();
        if (idList.size() < PAGE_SIZE) {
            int need = PAGE_SIZE - idList.size();
            Query q = new Query(Criteria.where("chatRoomId").is(chatRoomId));
            if (cursorId != null) {
                Criteria cursorCriteria = Criteria.where("_id");
                q.addCriteria(isPrev
                        ? cursorCriteria.lt(new ObjectId(cursorId))
                        : cursorCriteria.gt(new ObjectId(cursorId)));
            }
            Sort.Direction direction = isPrev ? Sort.Direction.DESC : Sort.Direction.ASC;
            q.with(Sort.by(direction, "_id")).limit(need + 1);

            List<ChatMessageDocument> fetched = mongoTemplate.find(q, ChatMessageDocument.class);
            List<ChatMessageDocument> toCache = fetched.stream().limit(need).toList();

            toCache.forEach(doc -> {
                double score = toScore(doc.getId());
                Boolean added = redisTemplate.opsForZSet().add(redisKey, doc.getId(), score);
                if (Boolean.TRUE.equals(added)) {
                    redisTemplate.expire(redisKey, Duration.ofHours(1));
                }
            });

            docs.addAll(toCache);
        }
        return docs;
    }

    private List<ChatMessageResponse> assemblePageResponse(List<ChatMessageDocument> finalPage) {
        Set<Long> partIds = finalPage.stream()
                .map(ChatMessageDocument::getChatParticipantId)
                .collect(Collectors.toSet());
        Map<Long, ChatParticipant> parts = chatParticipantRepository.findAllById(partIds)
                .stream().collect(Collectors.toMap(ChatParticipant::getId, p -> p));

        return finalPage.stream().map(doc -> {
            ChatParticipant p = parts.get(doc.getChatParticipantId());
            return chatMessageQueryMapper.toMessageResponse(
                    doc,
                    Optional.ofNullable(p).map(part -> part.getUser().getNickname()).orElse("Unknown"),
                    Optional.ofNullable(p).map(part -> part.getUser().getImageKey()).orElse(null)
            );
        }).toList();
    }
}
