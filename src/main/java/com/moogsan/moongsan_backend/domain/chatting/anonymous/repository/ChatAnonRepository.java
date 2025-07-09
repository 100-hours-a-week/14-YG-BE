package com.moogsan.moongsan_backend.domain.chatting.anonymous.repository;

import com.moogsan.moongsan_backend.domain.chatting.anonymous.entity.ChatAnon;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatAnonRepository extends MongoRepository<ChatAnon, String> {

    @Query(value = "{ 'postId': ?0 }", fields = "{ 'aliasId': 1 }")
    List<ChatAnon> findByPostId(Long postId);

    List<ChatAnon> findByPostIdOrderByCreatedAtAsc(Long postId);

    long countByPostId(Long postId);
}
