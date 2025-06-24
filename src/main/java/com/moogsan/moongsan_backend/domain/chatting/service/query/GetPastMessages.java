package com.moogsan.moongsan_backend.domain.chatting.service.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessagePageResponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatParticipant;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.NotParticipantException;
import com.moogsan.moongsan_backend.domain.chatting.mapper.ChatMessageQueryMapper;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatRoomRepository;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.moogsan.moongsan_backend.global.util.ObjectIdScoreUtil.toScore;

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
        // 1) 권한 및 방 검증
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);
        boolean isParticipant = chatParticipantRepository
                .existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoomId, currentUser.getId());
        if (!isParticipant) throw new NotParticipantException("참여자만 메세지를 조회할 수 있습니다.");

        String redisKey = "chatting:messages:" + chatRoomId;
        double cursorScore = cursorId != null
                ? toScore(cursorId)
                : (isPrev ? Double.MAX_VALUE : Double.MIN_VALUE);

        // 2) Redis 조회 (방향 분기)
        Set<String> cachedMembers = isPrev
                ? redisTemplate.opsForZSet().reverseRangeByScore(redisKey, cursorScore - 1, Double.NEGATIVE_INFINITY, 0, PAGE_SIZE + 1)
                : redisTemplate.opsForZSet().rangeByScore(redisKey, cursorScore + 1, Double.MAX_VALUE, 0, PAGE_SIZE + 1);

        // 3) 캐시 메시지를 ID로 변환
        List<String> idList = cachedMembers.stream()
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
                .collect(Collectors.toList());
        if (cursorId != null) idList.removeIf(id -> id.equals(cursorId));

        // 4) 부족한 메시지 MongoDB 조회
        List<ChatMessageDocument> dbDocs = new ArrayList<>();
        if (idList.size() < PAGE_SIZE) {
            int need = PAGE_SIZE - idList.size();
            Query q = new Query(Criteria.where("chatRoomId").is(chatRoomId));
            if (cursorId != null) {
                Criteria cursorCriteria = Criteria.where("_id");
                q.addCriteria(isPrev
                        ? cursorCriteria.lt(new ObjectId(cursorId))
                        : cursorCriteria.gt(new ObjectId(cursorId)));
            }
            q.with(Sort.by(isPrev ? Sort.Direction.DESC : Sort.Direction.ASC, "_id")).limit(need + 1);
            List<ChatMessageDocument> fetched = mongoTemplate.find(q, ChatMessageDocument.class);
            List<ChatMessageDocument> toCache = fetched.stream().limit(need).toList();

            toCache.forEach(doc -> {
                double score = toScore(doc.getId());
                Boolean added = redisTemplate.opsForZSet().add(redisKey, doc.getId(), score);
                if (Boolean.TRUE.equals(added)) {
                    redisTemplate.expire(redisKey, Duration.ofHours(1));
                }
            });

            dbDocs.addAll(toCache);
        }

        // 5) ID 병합
        Stream<ObjectId> fromCache = idList.stream().limit(PAGE_SIZE).map(ObjectId::new);
        Stream<ObjectId> fromDb = dbDocs.stream().map(doc -> new ObjectId(doc.getId()));
        List<ObjectId> finalIds = Stream.concat(fromCache, fromDb)
                .distinct()
                .limit(PAGE_SIZE)
                .collect(Collectors.toList());

        // 6) 최종 정렬 및 조회
        Query finalQ = new Query(Criteria.where("_id").in(finalIds));
        finalQ.with(Sort.by(Sort.Order.asc("_id")));
        List<ChatMessageDocument> finalPage = mongoTemplate.find(finalQ, ChatMessageDocument.class);

        // 정렬 역전 보정
        if (isPrev) {
            Collections.reverse(finalPage);
        }

        // 7) hasNext 계산
        boolean hasNext = finalPage.size() == PAGE_SIZE && (
                !dbDocs.isEmpty() || cachedMembers.size() > PAGE_SIZE
        );

        // 8) DTO 변환
        Set<Long> partIds = finalPage.stream()
                .map(ChatMessageDocument::getChatParticipantId)
                .collect(Collectors.toSet());
        Map<Long, ChatParticipant> parts = chatParticipantRepository.findAllById(partIds)
                .stream().collect(Collectors.toMap(ChatParticipant::getId, p -> p));
        List<ChatMessageResponse> responses = finalPage.stream().map(doc -> {
            ChatParticipant p = parts.get(doc.getChatParticipantId());
            return chatMessageQueryMapper.toMessageResponse(
                    doc,
                    Optional.ofNullable(p).map(part -> part.getUser().getNickname()).orElse("Unknown"),
                    Optional.ofNullable(p).map(part -> part.getUser().getImageKey()).orElse(null)
            );
        }).collect(Collectors.toList());

        String nextCursor = finalPage.isEmpty() ? null : finalPage.getLast().getId();

        return ChatMessagePageResponse.builder()
                .chatMessageResponses(responses)
                .nextCursorId(nextCursor)
                .hasNext(hasNext)
                .build();
    }
}
