package com.moogsan.moongsan_backend.participantchat.application.service.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.participantchat.presentation.dto.command.request.CreateChatMessageRequest;
import com.moogsan.moongsan_backend.participantchat.domain.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.participantchat.domain.entity.ChatParticipant;
import com.moogsan.moongsan_backend.participantchat.domain.entity.ChatRoom;
import com.moogsan.moongsan_backend.participantchat.domain.exception.specific.ChatRoomInvalidStateException;
import com.moogsan.moongsan_backend.participantchat.domain.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.participantchat.domain.exception.specific.NotParticipantException;
import com.moogsan.moongsan_backend.participantchat.application.mapper.ChatMessageCommandMapper;
import com.moogsan.moongsan_backend.participantchat.domain.repository.ChatMessageRepository;
import com.moogsan.moongsan_backend.participantchat.domain.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.participantchat.domain.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.participantchat.application.service.query.GetLatestMessageSse;
import com.moogsan.moongsan_backend.participantchat.application.service.query.GetLatestMessages;
import com.moogsan.moongsan_backend.participantchat.application.service.websocket.GetLatestMessagesStomp;
import com.moogsan.moongsan_backend.participantchat.domain.util.MessageSequenceGenerator;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;

import static com.moogsan.moongsan_backend.participantchat.domain.constant.ParticipantChatConstants.CASHE_REDIS_KET;
import static com.moogsan.moongsan_backend.participantchat.domain.message.ResponseMessage.DELETED_CHAT_ROOM;
import static com.moogsan.moongsan_backend.groupbuy.domain.message.ResponseMessage.NOT_PARTICIPANT;
import static com.moogsan.moongsan_backend.global.util.ObjectIdScoreUtil.toScore;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CreateChatMessage {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MessageSequenceGenerator messageSequenceGenerator;
    private final ChatMessageCommandMapper chatMessageCommandMapper;
    private final GetLatestMessages getLatestMessages;
    private final GetLatestMessageSse getLatestMessageSse;
    private final GetLatestMessagesStomp getLatestMessagesStomp;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public void createChatMessage(User currentUser, CreateChatMessageRequest request, Long chatRoomId) {

        // 채팅방 조회 및 유효성 검사
        ChatRoom chatRoom = fetchAndValidate(chatRoomId);

        // 참여자인지 조회 -> 아니면 403
        ChatParticipant participant = validateUser(chatRoomId, currentUser.getId());

        // 메세지 순번 생성 (커서 기반 페이징용)
        Long nextSeq = messageSequenceGenerator.getNextMessageSeq(chatRoomId);

        // 메세지 작성 및 저장
        ChatMessageDocument document = chatMessageCommandMapper.toMessageDocument(chatRoom, participant.getId(), request, nextSeq);
        chatMessageRepository.save(document);

        /* SecurityContext context = SecurityContextHolder.getContext();
        // 롱 폴링
        getLatestMessages.notifyNewMessage(document, currentUser.getNickname(), currentUser.getImageKey(), context);
        // sse
        getLatestMessageSse.notifyNewMessageSse(document,currentUser.getNickname(),currentUser.getImageKey(),context); */

        // socket
        getLatestMessagesStomp.notifyNewMessage(document, currentUser.getNickname(), currentUser.getImageKey());

        // 메세지 캐싱
        cacheMessage(chatRoomId, document);

    }

    private ChatRoom fetchAndValidate(Long chatRoomId) {
        // 채팅방 조회 -> 없으면 404
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        // 채팅방 삭제 여부 조회 -> 409
        if(chatRoom.getDeletedAt() != null) {
            throw new ChatRoomInvalidStateException(DELETED_CHAT_ROOM);
        }

        return chatRoom;
    }

    private ChatParticipant validateUser(Long userId, Long chatRoomId) {
        return chatParticipantRepository
                .findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoomId, userId)
                .orElseThrow(() -> new NotParticipantException(NOT_PARTICIPANT));
    }

    private void cacheMessage(Long chatRoomId, ChatMessageDocument document) {
        String redisKey = CASHE_REDIS_KET + chatRoomId;

        try {
            String json = objectMapper.writeValueAsString(document);
            double score = toScore(document.getId()); // tie-breaker 점수
            Boolean added = redisTemplate.opsForZSet().add(redisKey, json, score);
            if (Boolean.TRUE.equals(added)) {
                redisTemplate.expire(redisKey, Duration.ofHours(1)); // 키가 새로 생성됐을 때만 TTL 부여
            }
        } catch (JsonProcessingException e) {
            log.warn("❌ Redis 캐싱 실패 [chatRoomId={}]: {}", chatRoomId, e.getMessage());
        }
    }
}
