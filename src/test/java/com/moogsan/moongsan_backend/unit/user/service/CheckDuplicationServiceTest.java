package com.moogsan.moongsan_backend.unit.user.service;

import com.moogsan.moongsan_backend.domain.user.dto.response.CheckDuplicationResponse;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.domain.user.service.CheckDuplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckDuplicationServiceTest {

    @InjectMocks
    private CheckDuplicationService checkDuplicationService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- 닉네임 중복 체크 ----------

    @Test
    @DisplayName("닉네임 중복 - YES")
    void checkNickname_duplicate() {
        String nickname = "tester";
        when(userRepository.existsByNickname(nickname)).thenReturn(true);

        CheckDuplicationResponse response = checkDuplicationService.checkNickname(nickname);

        assertThat(response.getIsDuplication()).isEqualTo("YES");
    }

    @Test
    @DisplayName("닉네임 중복 - NO")
    void checkNickname_available() {
        String nickname = "uniqueNick";
        when(userRepository.existsByNickname(nickname)).thenReturn(false);

        CheckDuplicationResponse response = checkDuplicationService.checkNickname(nickname);

        assertThat(response.getIsDuplication()).isEqualTo("NO");
    }

    @Test
    @DisplayName("닉네임이 너무 짧을 경우 예외")
    void checkNickname_tooShort() {
        assertThatThrownBy(() -> checkDuplicationService.checkNickname("a"))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("닉네임이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("닉네임이 너무 길 경우 예외")
    void checkNickname_tooLong() {
        assertThatThrownBy(() -> checkDuplicationService.checkNickname("abcdefghijklmnop"))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("닉네임이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("닉네임이 null일 경우 예외")
    void checkNickname_null() {
        assertThatThrownBy(() -> checkDuplicationService.checkNickname(null))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("닉네임이 올바르지 않습니다.");
    }

    // ---------- 이메일 중복 체크 ----------

    @Test
    @DisplayName("이메일 중복 - YES")
    void checkEmail_duplicate() {
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        CheckDuplicationResponse response = checkDuplicationService.checkEmail(email);

        assertThat(response.getIsDuplication()).isEqualTo("YES");
    }

    @Test
    @DisplayName("이메일 중복 - NO")
    void checkEmail_available() {
        String email = "newuser@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        CheckDuplicationResponse response = checkDuplicationService.checkEmail(email);

        assertThat(response.getIsDuplication()).isEqualTo("NO");
    }

    @Test
    @DisplayName("이메일 형식이 틀리면 예외")
    void checkEmail_invalidFormat() {
        assertThatThrownBy(() -> checkDuplicationService.checkEmail("not-an-email"))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("이메일의 형식이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("이메일이 null일 경우 예외")
    void checkEmail_null() {
        assertThatThrownBy(() -> checkDuplicationService.checkEmail(null))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("이메일의 형식이 올바르지 않습니다.");
    }
}