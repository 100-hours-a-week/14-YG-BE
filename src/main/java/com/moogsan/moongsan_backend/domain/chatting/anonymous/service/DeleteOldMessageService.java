package com.moogsan.moongsan_backend.domain.chatting.anonymous.service;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.entity.ChatAnon;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.repository.ChatAnonRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class DeleteOldMessageService {
    private final ChatAnonRepository chatAnonRepository;

    public DeleteOldMessageService(ChatAnonRepository chatAnonRepository) {
        this.chatAnonRepository = chatAnonRepository;
    }

    public void deleteOldMessages(Long postId) {
        List<ChatAnon> allMessages = chatAnonRepository.findByPostId(postId);

        if (allMessages.size() > 10) {
            List<ChatAnon> messagesToDelete = allMessages.stream()
                .sorted(Comparator.comparing(
                    msg -> msg.getCreatedAt() != null ? msg.getCreatedAt() : LocalDateTime.MIN
                ))
                .limit(allMessages.size() - 10)
                .toList();

            List<String> idsToDelete = messagesToDelete.stream()
                .map(ChatAnon::getId)
                .toList();

            chatAnonRepository.deleteAllById(idsToDelete);
        }

        System.out.println("🟡 [DeleteOldMessage] 현재 메시지 개수: " + chatAnonRepository.countByPostId(postId));
    }
}
