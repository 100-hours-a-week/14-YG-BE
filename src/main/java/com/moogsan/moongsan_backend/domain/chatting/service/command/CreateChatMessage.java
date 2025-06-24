package com.moogsan.moongsan_backend.domain.chatting.service.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.domain.chatting.dto.command.request.CreateChatMessageRequest;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatParticipant;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.ChatRoomInvalidStateException;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.NotParticipantException;
import com.moogsan.moongsan_backend.domain.chatting.mapper.ChatMessageCommandMapper;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatMessageRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.chatting.service.query.GetLatestMessageSse;
import com.moogsan.moongsan_backend.domain.chatting.service.query.GetLatestMessages;
import com.moogsan.moongsan_backend.domain.chatting.util.MessageSequenceGenerator;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;

import static com.moogsan.moongsan_backend.domain.chatting.message.ResponseMessage.DELETED_CHAT_ROOM;
import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.NOT_PARTICIPANT;
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
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public void createChatMessage(User currentUser, CreateChatMessageRequest request, Long chatRoomId) {

        // 채팅방 조회 -> 없으면 404
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        // 채팅방이 삭제되지 않았는지 조회
        if(chatRoom.getDeletedAt() != null) {
            throw new ChatRoomInvalidStateException(DELETED_CHAT_ROOM);
        }

        // 참여자인지 조회 -> 아니면 403
        ChatParticipant participant = chatParticipantRepository
                .findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoomId, currentUser.getId())
                .orElseThrow(() -> new NotParticipantException(NOT_PARTICIPANT));


        // 메세지 순번 생성 (커서 기반 페이징용)
        Long nextSeq = messageSequenceGenerator.getNextMessageSeq(chatRoomId);

        // 메세지 작성
        ChatMessageDocument document = chatMessageCommandMapper
                .toMessageDocument(chatRoom, participant.getId(), request, nextSeq);
        SecurityContext context = SecurityContextHolder.getContext();
        chatMessageRepository.save(document);

        // 롱 폴링
        getLatestMessages.notifyNewMessage(document, currentUser.getNickname(), currentUser.getImageKey(), context);

        // sse
        /*
        getLatestMessageSse.notifyNewMessageSse(
                document,
                currentUser.getNickname(),
                currentUser.getImageKey(),
                context
        );

         */

        String redisKey = "chatting:messages:" + chatRoomId;

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
