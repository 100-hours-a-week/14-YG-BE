package com.moogsan.moongsan_backend.domain.chatting.anonymous.controller;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.dto.ChatAnonDto;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.entity.ChatAnon;
import com.moogsan.moongsan_backend.domain.chatting.anonymous.repository.ChatAnonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-anon")
public class ChatAnonQueryController {
    private final ChatAnonRepository chatAnonRepository;

    @GetMapping("/{postId}")
    public List<ChatAnonDto> getAllMessages(@PathVariable Long postId){
        List<ChatAnon> entities = chatAnonRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return entities.stream()
                .map(ChatAnonDto::from)
                .toList();
    }
}
