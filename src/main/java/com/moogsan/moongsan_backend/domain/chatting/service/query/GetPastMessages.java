package com.moogsan.moongsan_backend.domain.chatting.service.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessagePageResponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatParticipant;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import org.bson.types.ObjectId;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.NotParticipantException;
import com.moogsan.moongsan_backend.domain.chatting.mapper.ChatMessageQueryMapper;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatRoomRepository;
import org.springframework.data.redis.connection.RedisZSetCommands.Range;
import org.springframework.data.redis.connection.RedisZSetCommands.Limit;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import static com.moogsan.moongsan_backend.global.util.ObjectIdScoreUtil.toScore;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GetPastMessages {

    private static final int PAGE_SIZE = 10;
    private final ChatMessageQueryMapper chatMessageQueryMapper;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public ChatMessagePageResponse getPastMessages(
            User currentUser,
            Long chatRoomId,
            String cursorId
    ) {

        // 채팅방 조회 -> 없으면 404
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        // 참여자인지 조회 -> 아니면 403
        boolean isParticipant = chatParticipantRepository.existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoomId, currentUser.getId());

        if(!isParticipant) {
            throw new NotParticipantException("참여자만 메세지를 조회할 수 있습니다.");
        }

        // 레디스 캐싱
        String redisKey = "chatting:messages:" + chatRoomId;

        // 레디스 캐시 조회
        double minScore = 0.0;
        double maxScore = cursorId != null ? toScore(cursorId) : Double.MAX_VALUE;

        Set<String> cachedJsons = redisTemplate.opsForZSet()
                .reverseRangeByScore(redisKey, minScore, maxScore, 0, PAGE_SIZE + 1);


        List<ChatMessageDocument> cachedMessages = Objects.requireNonNull(cachedJsons).stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, ChatMessageDocument.class);
                    } catch (JsonProcessingException e) {
                        log.warn("❌ Redis 메세지 파싱 실패: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(doc -> !doc.getId().equals(cursorId))
                .toList();


        // hasNext 판단 & 실제 페이지 사이즈로 자르기
        List<ChatMessageDocument> page = new ArrayList<>(cachedMessages);
        boolean hasNext = false;

        if (page.size() < PAGE_SIZE) {
            int needCount = PAGE_SIZE - page.size();

            // 커서 기반 필터 생성
            Query q = new Query();
            if (cursorId == null || !ObjectId.isValid(cursorId)) {
                q.addCriteria(Criteria.where("chatRoomId").is(chatRoomId));
            } else {
                q.addCriteria(new Criteria().andOperator(
                        Criteria.where("chatRoomId").is(chatRoomId),
                        Criteria.where("_id").lt(new ObjectId(cursorId))
                ));
            }

            q.with(Sort.by(Sort.Order.desc("_id"))).limit(needCount + 1);

            List<ChatMessageDocument> cursorDocs =
                    mongoTemplate.find(q, ChatMessageDocument.class);

            List<ChatMessageDocument> mongoDocs = mongoTemplate.find(q, ChatMessageDocument.class);
            hasNext = mongoDocs.size() > needCount;
            List<ChatMessageDocument> mongoPage = mongoDocs.stream().limit(needCount).toList();
            page.addAll(mongoPage);

            mongoPage.forEach(doc -> {
                try {
                    String json = objectMapper.writeValueAsString(doc);
                    double score = toScore(doc.getId()); // tie-breaker 점수
                    Boolean added =  redisTemplate.opsForZSet().add(redisKey, json, score);
                    if (Boolean.TRUE.equals(added)) {
                        redisTemplate.expire(redisKey, Duration.ofHours(24)); // 키가 새로 생성됐을 때만 TTL 부여
                    }
                } catch (JsonProcessingException e) {
                    log.warn("❌ Redis 캐싱 실패: {}", e.getMessage());
                }
            });

            redisTemplate.expire(redisKey, Duration.ofHours(1));
        } else {                        // ★ 캐시에서 이미 10개 꽉 찬 경우
            // 마지막 메시지 ID 기준으로 DB에 추가 1건만 확인
            String lastId = page.get(PAGE_SIZE - 1).getId();

            Query extraQ = new Query().addCriteria(
                    new Criteria().andOperator(
                            Criteria.where("chatRoomId").is(chatRoomId),
                            Criteria.where("_id").lt(new ObjectId(lastId))
                    ));
            extraQ.with(Sort.by(Sort.Order.desc("_id"))).limit(1);

            hasNext = !mongoTemplate.find(extraQ, ChatMessageDocument.class).isEmpty();
        }


        page = page.stream().limit(PAGE_SIZE).toList();

        log.info("[Chat-Cache] room={}, cursorId={}, hitCount={}, finalSize={}, hasNext={}",
                chatRoomId, cursorId, cachedMessages.size(), page.size(), hasNext);


        // 빈 페이지인 경우 바로 리턴
        if (page.isEmpty()) {
            return ChatMessagePageResponse.builder()
                    .chatMessageResponses(Collections.emptyList())
                    .nextCursorId(null)         // 또는 cursorId 그대로
                    .hasNext(false)
                    .build();
        }

        // 참여자 정보 매핑
        Set<Long> participantIds = page.stream()
                .map(ChatMessageDocument::getChatParticipantId)
                .collect(Collectors.toSet());
        List<ChatParticipant> participants = chatParticipantRepository.findAllById(participantIds);
        Map<Long, User> participantIdToUser = participants.stream()
                .collect(Collectors.toMap(
                        ChatParticipant::getId,
                        ChatParticipant::getUser
                ));

        ChatMessageDocument last = page.getLast();
        String nextCursorId    = last.getId();
        LocalDateTime nextTime = last.getCreatedAt();

        // DTO 변환 후 반환
        List<ChatMessageResponse> responses =  page.stream()
                .map(doc -> {
                    User user = participantIdToUser.get(doc.getChatParticipantId());
                    String nickname = (user != null ? user.getNickname() : "알수없음");
                    String imageKey = (user != null ? user.getImageKey() : null);
                    return chatMessageQueryMapper.toMessageResponse(doc, nickname, imageKey);
                })
                .collect(Collectors.toList());

        return ChatMessagePageResponse.builder()
                .chatMessageResponses(responses)
                .nextCursorId(nextCursorId)
                .hasNext(hasNext)
                .build();
    }
}
