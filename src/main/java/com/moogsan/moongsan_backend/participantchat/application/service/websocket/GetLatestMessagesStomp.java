package com.moogsan.moongsan_backend.participantchat.application.service.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.participantchat.presentation.dto.query.response.ChatMessageResponse;
import com.moogsan.moongsan_backend.participantchat.domain.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.participantchat.domain.exception.specific.NotParticipantException;
import com.moogsan.moongsan_backend.participantchat.application.mapper.ChatMessageQueryMapper;
import com.moogsan.moongsan_backend.participantchat.domain.repository.ChatMessageRepository;
import com.moogsan.moongsan_backend.participantchat.domain.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.participantchat.domain.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.moogsan.moongsan_backend.participantchat.domain.message.ResponseMessage.NOT_PARTICIPANT;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.SERIALIZATION_FAIL;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetLatestMessagesStomp {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageQueryMapper chatMessageQueryMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // 최근 목록 조회용
    public List<ChatMessageResponse> getMessagesAfter(User user, Long chatRoomId, String lastMessageId) {
        boolean isParticipant = chatParticipantRepository.existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoomId, user.getId());
        if (!isParticipant) throw new NotParticipantException(NOT_PARTICIPANT);

        List<ChatMessageDocument> newMessages = chatMessageRepository.findMessagesAfter(chatRoomId, lastMessageId);

        return newMessages.stream()
                .map(doc -> chatMessageQueryMapper.toMessageResponse(doc, user.getNickname(), user.getImageKey()))
                .toList();
    }

    // 브로드 캐스트용
    public void notifyNewMessage(
            ChatMessageDocument message,
            String nickname,
            String imageKey
    ) {
        Long chatRoomId = message.getChatRoomId();
        log.info("[WS-TEST] broadcasting {}", message);

        ChatMessageResponse response = chatMessageQueryMapper
                .toMessageResponse(message, nickname, imageKey);

        try {
            String payload = objectMapper.writeValueAsString(response);
            messagingTemplate.convertAndSend("/sub/chat-participant/" + chatRoomId, payload);
            log.info("[WS] push to /sub/chat-participant/{}", chatRoomId);
        } catch (
        JsonProcessingException e) {
            throw new RuntimeException(SERIALIZATION_FAIL, e);
        }
    }
}
