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
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetLatestMessageSse {
    private static final long TIMEOUT_MILLIS = 5000L;

    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageQueryMapper chatMessageQueryMapper;

    // 채팅방별 롱폴링 요청 큐
    // private final Map<Long, List<DeferredResult<List<ChatMessageResponse>>>> listeners = new ConcurrentHashMap<>();
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter getLatesetMessagesSse(
            User currentUser, Long chatRoomId, String lastMessageId
    ) {
        // 채팅방 조회 -> 없으면 404
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        // 참여자인지 조회 -> 아니면 403
        boolean isParticipant = chatParticipantRepository.existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoomId, currentUser.getId());

        if(!isParticipant) {
            throw new NotParticipantException("참여자만 메세지를 조회할 수 있습니다.");
        }

        // SseEmitter 생성
        SseEmitter emitter = new SseEmitter(0L);

        // emitters 맵에 추가
        emitters
                .computeIfAbsent(chatRoomId, id -> Collections.synchronizedList(new ArrayList<>()))
                .add(emitter);

        // 타임아웃(클라이언트 비연결) 혹은 연결 종료 시 emitters에서 제거
        emitter.onCompletion(() -> {
            List<SseEmitter> list = emitters.get(chatRoomId);
            if (list != null) {
                list.remove(emitter);
            }
        });

        emitter.onTimeout(() -> {
            emitter.complete();
            List<SseEmitter> list = emitters.get(chatRoomId);
            if (list != null) {
                list.remove(emitter);
            }
        });

        return emitter;
    }

    public void notifyNewMessage(
            ChatMessageDocument newMessage,
            String nickname,
            String imageKey,
            SecurityContext context
    ) {
        Long chatRoomId = newMessage.getChatRoomId();
        List<SseEmitter> list = emitters.getOrDefault(chatRoomId, new ArrayList<>());

        // log.info("🔔 notifyNewMessage 호출됨: chatRoomId={}, messageId={}", chatRoomId, newMessage.getId());
        // log.info("🧍‍♂️ 응답 대기 중인 클라이언트 수: {}", results.size());
        ChatMessageResponse response = chatMessageQueryMapper.toMessageResponse(newMessage, nickname, imageKey);
        for (SseEmitter emitter : list) {
            Runnable task = () -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("new-message")
                            .data(response));
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            };

            Runnable securedTask = new DelegatingSecurityContextRunnable(task, context);
            Thread.startVirtualThread(securedTask); // 새로운 Loom 가상 스레드를 띄워서 r.setResult(...)를 비동기적으로 실행
        }
    }

    private DeferredResult<List<ChatMessageResponse>> wrapResult(List<ChatMessageDocument> messages) {
        List<ChatMessageResponse> responses = messages.stream()
                .map(doc -> chatMessageQueryMapper.toMessageResponse(doc, "알수없음", null)) // 빠른 반환이라 간략화
                .collect(Collectors.toList());
        DeferredResult<List<ChatMessageResponse>> result = new DeferredResult<>();
        result.setResult(responses);
        return result;
    }
}

