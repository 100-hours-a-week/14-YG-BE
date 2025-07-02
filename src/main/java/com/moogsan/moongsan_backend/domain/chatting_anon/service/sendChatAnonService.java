package com.moogsan.moongsan_backend.domain.chatting_anon.service;

import com.moogsan.moongsan_backend.domain.chatting_anon.repository.ChatAnonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class sendChatAnonService {
    private final ChatAnonRepository chatAnonRepository;

    public int generateAliasId(Long postId) {
        List<Integer> aliasIds = chatAnonRepository.findDistinctAliasByPostId(postId);
        return aliasIds.stream().max(Integer::compareTo).orElse(0) + 1;
    }
}
