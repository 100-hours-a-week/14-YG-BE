package com.moogsan.moongsan_backend.unit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.domain.user.controller.UserController;
import com.moogsan.moongsan_backend.domain.user.dto.request.LoginRequest;
import com.moogsan.moongsan_backend.domain.user.dto.request.SignUpRequest;
import com.moogsan.moongsan_backend.domain.user.dto.response.CheckDuplicationResponse;
import com.moogsan.moongsan_backend.domain.user.dto.response.LoginResponse;
import com.moogsan.moongsan_backend.domain.user.dto.response.UserProfileResponse;
import com.moogsan.moongsan_backend.domain.user.service.*;
import com.moogsan.moongsan_backend.support.security.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private LoginService loginService;
    @MockBean private SignUpService signUpService;
    @MockBean(name = "nicknameService") private CheckDuplicationService nicknameService;
    @MockBean(name = "emailService") private CheckDuplicationService emailService;
    @MockBean private LogoutService logoutService;
    @MockBean private WithdrawService withdrawService;
    @MockBean private TokenRefreshService tokenRefreshService;
    @MockBean private WishService wishService;
    @MockBean private UserProfileService userProfileService;
    @MockBean private CheckAccountService checkAccountService;

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "email@email.com", "password123!",
                "nickname", "realName", "01000000000",
                "bankName", "12345679012", null
        );

        LoginResponse response = new LoginResponse(
                "nickname", "realName", null, "USER"
        );

        Mockito.when(signUpService.signUp(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data.nickname").value("nickname"))
                .andExpect(jsonPath("$.data.name").value("realName"))
                .andExpect(jsonPath("$.data.type").value("USER"));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 형식 오류")
    void signUp_fail_invalid_email() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "invalid-email", "password123!",
                "nickname", "realName", "01000000000",
                "bankName", "12345679012", null
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.email").value("이메일의 형식이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest("email@email.com", "password123!");

        LoginResponse response = new LoginResponse(
                "nickname", "realName", null, "USER"
        );

        Mockito.when(loginService.login(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/users/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인이 완료되었습니다."))
                .andExpect(jsonPath("$.data.nickname").value("nickname"))
                .andExpect(jsonPath("$.data.name").value("realName"))
                .andExpect(jsonPath("$.data.type").value("USER"));
    }

    @Test
    @DisplayName("이메일 중복 체크 - 사용 가능")
    void checkEmail_available() throws Exception {
        Mockito.when(emailService.checkEmail(anyString()))
                .thenReturn(new CheckDuplicationResponse("NO"));

        mockMvc.perform(get("/api/users/check-email")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용 가능한 이메일입니다."));
    }

    @Test
    @DisplayName("유저 프로필 조회 성공")
    @WithMockCustomUser(id = 1L)
    void getProfile_success() throws Exception {
        UserProfileResponse response = new UserProfileResponse(null, "nickname", "realName",
                "email@email.com", "01000000000", "bankName",
                "1234566789012", "USER");

        Mockito.when(userProfileService.getUserProfile(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("유저 정보 조회에 성공했습니다"))
                .andExpect(jsonPath("$.data.email").value("email@email.com"));
    }
}