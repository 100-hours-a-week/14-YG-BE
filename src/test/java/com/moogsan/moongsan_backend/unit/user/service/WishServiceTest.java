package com.moogsan.moongsan_backend.unit.user.service;

import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.entity.Wish;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.domain.user.repository.WishRepository;
import com.moogsan.moongsan_backend.domain.user.service.WishService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WishServiceTest {

    @InjectMocks
    private WishService wishService;

    @Mock private WishRepository wishRepository;
    @Mock private UserRepository userRepository;
    @Mock private GroupBuyRepository groupBuyRepository;

    private final Long userId = 1L;
    private final Long postId = 10L;
    private User user;
    private GroupBuy groupBuy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(userId).build();
        groupBuy = GroupBuy.builder().id(postId).build();
    }

    @Test
    @DisplayName("관심 등록 성공")
    void addWish_success() {
        when(wishRepository.existsByUserIdAndGroupBuyId(userId, postId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupBuyRepository.findById(postId)).thenReturn(Optional.of(groupBuy));

        wishService.addWish(userId, postId);

        verify(wishRepository).save(any(Wish.class));
    }

    @Test
    @DisplayName("이미 관심 등록된 경우 예외 발생")
    void addWish_alreadyExists() {
        when(wishRepository.existsByUserIdAndGroupBuyId(userId, postId)).thenReturn(true);

        assertThatThrownBy(() -> wishService.addWish(userId, postId))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining("이미 관심 등록된 공구입니다.");
    }

    @Test
    @DisplayName("유저가 존재하지 않으면 예외 발생")
    void addWish_userNotFound() {
        when(wishRepository.existsByUserIdAndGroupBuyId(userId, postId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishService.addWish(userId, postId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("유저를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("공구글이 존재하지 않으면 예외 발생")
    void addWish_groupBuyNotFound() {
        when(wishRepository.existsByUserIdAndGroupBuyId(userId, postId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupBuyRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishService.addWish(userId, postId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("공구글을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("관심 삭제 성공")
    void removeWish_success() {
        Wish wish = Wish.builder().user(user).groupBuy(groupBuy).build();

        when(wishRepository.findByUserIdAndGroupBuyId(userId, postId)).thenReturn(Optional.of(wish));

        wishService.removeWish(userId, postId);

        verify(wishRepository).delete(wish);
    }

    @Test
    @DisplayName("관심 목록이 없으면 예외 발생")
    void removeWish_notFound() {
        when(wishRepository.findByUserIdAndGroupBuyId(userId, postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishService.removeWish(userId, postId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("관심 목록이 존재하지 않습니다.");
    }
}