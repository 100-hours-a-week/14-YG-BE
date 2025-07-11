package com.moogsan.moongsan_backend.domain.chatting.anonymous.service;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.dto.ChatAnonDto;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.entity.ChatAnon;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.repository.ChatAnonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SendChatAnonService {
    private final ChatAnonRepository chatAnonRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 게시글 ID(postId)를 기반으로 현재까지 사용된 aliasId 중 가장 큰 값에 1을 더해서 반환
    public int generateAliasId(Long postId) {
        List<Integer> aliasIds = chatAnonRepository.findByPostId(postId).stream()
                .map(ChatAnon::getAliasId)
                .distinct()
                .toList();

        if (aliasIds.isEmpty()) {
            return 1; // 첫 번째 익명 사용자
        }

        return aliasIds.stream().max(Integer::compareTo).orElse(0) + 1;
    }

    // 수신된 메시지를 처리하고 저장 및 브로드캐스트하는 메서드
    public void processMessage(Long postId, Integer aliasId, ChatAnonDto message) {
        // createdAt이 비어있으면 현재 시간으로 설정
        if (message.getCreatedAt() == null) {
            LocalDateTime now = LocalDateTime.now();
            message.setCreatedAt(now);
        }

        // 수신된 메시지 로그 출력
        System.out.println("📨 [DEBUG] 수신된 메시지 - postId: " + postId + ", aliasId: " + aliasId + ", message: " + message.getMessage());

        // ChatAnon 엔티티로 변환
        ChatAnon entity = ChatAnon.builder()
                .postId(postId)
                .aliasId(aliasId)
                .message(message.getMessage())
                .createdAt(message.getCreatedAt())
                .build();

        // MongoDB 저장
        chatAnonRepository.save(entity);

        // 전체 메시지 개수 디버그 출력
        List<ChatAnon> allMessages = chatAnonRepository.findByPostId(postId);
        System.out.println("🔢 [DEBUG] 전체 메시지 개수 (createdAt 여부 상관없이): " + allMessages.size());

        if (allMessages.size() > 10) {
            List<ChatAnon> messagesToDelete = allMessages.stream()
                    .sorted(Comparator.comparing(
                        msg -> msg.getCreatedAt() != null ? msg.getCreatedAt() : LocalDateTime.MIN
                    )) // createdAt이 null이면 가장 오래된 것으로 간주
                    .limit(allMessages.size() - 10) // 최근 10개 제외
                    .toList();

            List<String> idsToDelete = messagesToDelete.stream()
                    .map(ChatAnon::getId)
                    .toList();

            chatAnonRepository.deleteAllById(idsToDelete);
        }

        // 구독 중인 클라이언트에게 메시지 브로드캐스트
        String destination = "/topic/chat/" + postId;
        System.out.println("📤 [DEBUG] 메시지 브로드캐스트 - destination: " + destination);
        messagingTemplate.convertAndSend(destination, message);
    }
}
