package com.moogsan.moongsan_backend.unit.user.service;

import com.moogsan.moongsan_backend.domain.user.dto.response.UserProfileResponse;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.domain.user.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserProfileServiceTest {

    @InjectMocks
    private UserProfileService userProfileService;

    @Mock
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("tester")
                .name("홍길동")
                .phoneNumber("01012345678")
                .accountBank("은행")
                .accountNumber("1234567890")
                .imageKey("image.jpg")
                .type("USER")
                .build();
    }

    @Test
    @DisplayName("유저 프로필 조회 성공")
    void getUserProfile_success() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        UserProfileResponse response = userProfileService.getUserProfile(1L);

        // then
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getNickname()).isEqualTo("tester");
        assertThat(response.getImageUrl()).isEqualTo("image.jpg");
        assertThat(response.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(response.getAccountBank()).isEqualTo("은행");
        assertThat(response.getAccountNumber()).isEqualTo("1234567890");
        assertThat(response.getType()).isEqualTo("USER");
    }

    @Test
    @DisplayName("유저가 존재하지 않으면 예외 발생")
    void getUserProfile_userNotFound() {
        // given
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> userProfileService.getUserProfile(2L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }
}