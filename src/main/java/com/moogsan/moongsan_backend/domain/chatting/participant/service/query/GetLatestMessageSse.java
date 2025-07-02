package com.moogsan.moongsan_backend.domain.chatting.participant.service.query;

import com.moogsan.moongsan_backend.domain.chatting.participant.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.participant.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.domain.chatting.participant.exception.specific.NotParticipantException;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import com.moogsan.moongsan_backend.domain.chatting.participant.mapper.ChatMessageQueryMapper;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.participant.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.moogsan.moongsan_backend.domain.chatting.participant.message.ResponseMessage.NOT_PARTICIPANT;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetLatestMessageSse {

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageQueryMapper chatMessageQueryMapper;

    // 채팅방별 롱폴링 요청 큐
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter getLatestMessagesSse(
            User currentUser, Long chatRoomId
    ) {
        // 채팅방 조회 -> 없으면 404
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        // 참여자인지 조회 -> 아니면 403
        boolean isParticipant = chatParticipantRepository.existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoomId, currentUser.getId());

        if(!isParticipant) {
            throw new NotParticipantException(NOT_PARTICIPANT);
        }

        // SseEmitter 생성
        SseEmitter emitter = new SseEmitter(0L);
        registerEmitter(chatRoomId, emitter);

        return emitter;
    }

    public void notifyNewMessageSse(
            ChatMessageDocument newMessage,
            String nickname,
            String imageKey,
            SecurityContext context
    ) {
        Long chatRoomId = newMessage.getChatRoomId();
        List<SseEmitter> list = emitters.getOrDefault(chatRoomId, new ArrayList<>());
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
            // securedTask.run();
            Thread.startVirtualThread(securedTask); // 새로운 Loom 가상 스레드를 띄워서 r.setResult(...)를 비동기적으로 실행
        }
    }

    private void registerEmitter(Long chatRoomId, SseEmitter emitter) {
        emitters
                .computeIfAbsent(chatRoomId, id -> Collections.synchronizedList(new ArrayList<>()))
                .add(emitter);

        emitter.onCompletion(() -> removeEmitter(chatRoomId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();;
            removeEmitter(chatRoomId, emitter);
        });
    }

    private void removeEmitter(Long chatRoomId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(chatRoomId);
        if (list != null) {
            list.remove(emitter);
        }
    }
}
