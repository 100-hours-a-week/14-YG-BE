package com.moogsan.moongsan_backend.domain.chatting.anonymous.service;

import java.util.List;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.repository.ChatAnonRepository;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.entity.ChatAnon;

public class GenerateAliasIdService {
    private final ChatAnonRepository chatAnonRepository;

    public GenerateAliasIdService(ChatAnonRepository chatAnonRepository) {
        this.chatAnonRepository = chatAnonRepository;
    }

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
}
