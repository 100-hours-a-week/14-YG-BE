package com.moogsan.moongsan_backend.domain.chatting_anon.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatAnonRepository {

    public List<Integer> findDistinctAliasByPostId(Long postId) {
        // TODO: implement actual MongoDB query to find distinct aliasIds for a post
        return List.of(); // placeholder
    }
}
