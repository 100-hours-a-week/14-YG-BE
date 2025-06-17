package com.moogsan.moongsan_backend.domain.chatting.service.query;

import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.NotParticipantException;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import com.moogsan.moongsan_backend.domain.chatting.mapper.ChatMessageQueryMapper;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatMessageRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.moogsan.moongsan_backend.domain.chatting.message.ResponseMessage.NOT_PARTICIPANT;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetLatestMessages {
    private static final long TIMEOUT_MILLIS = 5000L;

    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageQueryMapper chatMessageQueryMapper;

    // 채팅방별 롱폴링 요청 큐
    private final Map<Long, List<DeferredResult<List<ChatMessageResponse>>>> listeners = new ConcurrentHashMap<>();

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

    public DeferredResult<List<ChatMessageResponse>> createLongPollingResult(Long chatRoomId) {
        DeferredResult<List<ChatMessageResponse>> dr = new DeferredResult<>(TIMEOUT_MILLIS);

        listeners.computeIfAbsent(chatRoomId, id -> Collections.synchronizedList(new ArrayList<>()))
                .add(dr);

        dr.onTimeout(() -> {
            dr.setResult(Collections.emptyList());
            listeners.get(chatRoomId).remove(dr);
        });
        dr.onCompletion(() -> listeners.get(chatRoomId).remove(dr));

        return dr;
    }


    public void notifyNewMessage(
            ChatMessageDocument newMessage,
            String nickname,
            String imageKey,
            SecurityContext context
    ) {
        Long chatRoomId = newMessage.getChatRoomId();
        List<DeferredResult<List<ChatMessageResponse>>> results = listeners.getOrDefault(chatRoomId, new ArrayList<>());

        ChatMessageResponse response = chatMessageQueryMapper.toMessageResponse(newMessage, nickname, imageKey);
        for (DeferredResult<List<ChatMessageResponse>> r : results) {
            Runnable task = () -> {
                r.setResult(List.of(response));
            };

            Runnable securedTask = new DelegatingSecurityContextRunnable(task, context);
            // securedTask.run(); // 현재 스레드에서 즉시 실행 (r.setResult(...)가 현재 스레드에서 동기적 실행
            Thread.startVirtualThread(securedTask); // 새로운 Loom 가상 스레드를 띄워서 r.setResult(...)를 비동기적으로 실행
        }

        results.clear();
    }
}
