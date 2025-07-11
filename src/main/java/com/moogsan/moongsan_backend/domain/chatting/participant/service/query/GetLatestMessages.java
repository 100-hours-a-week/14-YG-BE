package com.moogsan.moongsan_backend.domain.chatting.participant.service.query;

import com.moogsan.moongsan_backend.domain.chatting.participant.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.participant.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.domain.chatting.participant.exception.specific.NotParticipantException;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import com.moogsan.moongsan_backend.domain.chatting.participant.mapper.ChatMessageQueryMapper;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatMessageRepository;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.moogsan.moongsan_backend.domain.chatting.participant.message.ResponseMessage.NOT_PARTICIPANT;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetLatestMessages {
    private static final long TIMEOUT_MILLIS = 5000L;

    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageQueryMapper chatMessageQueryMapper;

    // 채팅방별 롱폴링 요청 큐 (유저 ID → 연결 1개만)
    private final Map<Long, Map<Long, DeferredResult<List<ChatMessageResponse>>>> listeners = new ConcurrentHashMap<>();

    public List<ChatMessageResponse> getLatestMessages(
            User currentUser, Long chatRoomId, String lastMessageId
    ) {
        // 채팅방 조회 -> 없으면 404
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        // 참여자인지 조회 -> 아니면 403
        boolean isParticipant = chatParticipantRepository.existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoomId, currentUser.getId());

        if(!isParticipant) {
            throw new NotParticipantException(NOT_PARTICIPANT);
        }

        // 마지막 메세지 이후 새로운 메세지 존재 여부 확인
        List<ChatMessageDocument> newMessages = chatMessageRepository.findMessagesAfter(chatRoomId, lastMessageId);

        return newMessages.stream()
                .map(doc -> chatMessageQueryMapper
                        .toMessageResponse(doc, currentUser.getNickname(), currentUser.getImageKey()))
                .collect(Collectors.toList());
    }

    public DeferredResult<List<ChatMessageResponse>> createLongPollingResult(User currentUser, Long chatRoomId) {
        Long userId = currentUser.getId(); // 유저 ID를 키로 사용
        DeferredResult<List<ChatMessageResponse>> dr = new DeferredResult<>(TIMEOUT_MILLIS);

        listeners
                .computeIfAbsent(chatRoomId, k -> new ConcurrentHashMap<>())
                .put(userId, dr);


        dr.onTimeout(() -> {
            listeners.getOrDefault(chatRoomId, Map.of()).remove(userId);
            dr.setResult(Collections.emptyList());
        });
        dr.onCompletion(() -> {
            listeners.getOrDefault(chatRoomId, Map.of()).remove(userId);
        });

        return dr;
    }


    public void notifyNewMessage(
            ChatMessageDocument newMessage,
            String nickname,
            String imageKey,
            SecurityContext context
    ) {
        Long chatRoomId = newMessage.getChatRoomId();
        Map<Long, DeferredResult<List<ChatMessageResponse>>> roomListeners = listeners.getOrDefault(chatRoomId, Map.of());

        ChatMessageResponse response = chatMessageQueryMapper.toMessageResponse(newMessage, nickname, imageKey);

        for (Map.Entry<Long, DeferredResult<List<ChatMessageResponse>>> entry : roomListeners.entrySet()) {
            Long userId = entry.getKey();
            DeferredResult<List<ChatMessageResponse>> dr = entry.getValue();

            Runnable task = () -> dr.setResult(List.of(response));
            Runnable securedTask = new DelegatingSecurityContextRunnable(task, context);
            // securedTask.run(); // 현재 스레드에서 즉시 실행 (r.setResult(...)가 현재 스레드에서 동기적 실행
            Thread.startVirtualThread(securedTask);
        }
    }
}
