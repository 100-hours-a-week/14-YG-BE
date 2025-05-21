package com.moogsan.moongsan_backend.domain.user.service;

import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.entity.Wish;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.domain.user.repository.WishRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WishService {

    private final WishRepository wishRepository;
    private final UserRepository userRepository;
    private final GroupBuyRepository groupBuyRepository;

    // 관심 목록 등록
    public void addWish(Long userId, Long postId) {
        boolean exists = wishRepository.existsByUserIdAndGroupBuyId(userId, postId);
        if (exists) {
            throw new EntityExistsException("이미 관심 등록된 공구입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("공구글을 찾을 수 없습니다."));

        Wish wish = Wish.builder()
                .user(user)
                .groupBuy(groupBuy)
                .build();

        wishRepository.save(wish);
    }

    // 관심 목록 삭제
    public void removeWish(Long userId, Long postId) {
        Wish wish = wishRepository.findByUserIdAndGroupBuyId(userId, postId)
                .orElseThrow(() -> new EntityNotFoundException("관심 목록이 존재하지 않습니다."));

        wishRepository.delete(wish);
    }
}
