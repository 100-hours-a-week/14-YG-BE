package com.moogsan.moongsan_backend.unit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.domain.user.controller.UserController;
import com.moogsan.moongsan_backend.domain.user.dto.request.LoginRequest;
import com.moogsan.moongsan_backend.domain.user.dto.request.SignUpRequest;
import com.moogsan.moongsan_backend.domain.user.dto.response.CheckDuplicationResponse;
import com.moogsan.moongsan_backend.domain.user.dto.response.LoginResponse;
import com.moogsan.moongsan_backend.domain.user.dto.response.UserProfileResponse;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.exception.code.UserErrorCode;
import com.moogsan.moongsan_backend.domain.user.service.*;
import com.moogsan.moongsan_backend.global.security.jwt.JwtUtil;
import com.moogsan.moongsan_backend.support.security.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoginService loginService;
    @MockBean
    private SignUpService signUpService;
    @MockBean(name = "nicknameService")
    private CheckDuplicationService nicknameService;
    @MockBean(name = "emailService")
    private CheckDuplicationService emailService;
    @MockBean
    private LogoutService logoutService;
    @MockBean
    private WithdrawService withdrawService;
    @MockBean
    private TokenRefreshService tokenRefreshService;
    @MockBean
    private WishService wishService;
    @MockBean
    private UserProfileService userProfileService;
    @MockBean
    private CheckAccountService checkAccountService;
    @MockBean
    private JwtUtil jwtUtil;

    /*
    UserController 테스트 케이스 전체 목록

    1. POST /api/users – 회원가입
	•	✅ 회원가입 성공
	•	✅ 이메일 형식 오류
	•	✅ 비밀번호 형식 오류
	•	✅ 닉네임 누락
	•	✅ 실명 누락
	•	✅ 전화번호 형식 오류
	•	✅ 이미 존재하는 이메일
	•	✅ 이미 존재하는 닉네임
	•	✅ 이미 존재하는 전화번호

	2. POST /api/users/token – 로그인
	•	✅ 로그인 성공
	•	✅ 잘못된 이메일/비밀번호
	•	✅ 이메일 형식 오류
	•	✅ 비밀번호 누락

	3. GET /api/users/check-email – 이메일 중복 확인
	•	✅ 사용 가능한 이메일
	•	✅ 이미 사용 중인 이메일
	•	✅ 이메일 파라미터 누락

	4. GET /api/users/check-nickname – 닉네임 중복 확인
	•	✅ 사용 가능한 닉네임
	•	✅ 이미 사용 중인 닉네임
	•	✅ 닉네임 파라미터 누락

	5. GET /api/users/profile – 유저 프로필 조회
	•	✅ 로그인된 유저 프로필 조회 성공
	•	✅ 비로그인 상태에서 접근

	6. DELETE /api/users/token – 로그아웃
	•	✅ 로그아웃 성공
	•	✅ 비로그인 상태에서 접근

	7. DELETE /api/users – 회원 탈퇴
	•	✅ 회원 탈퇴 성공
	•	✅ 비로그인 상태에서 접근

	8. POST /api/users/token/refresh – 액세스 토큰 재발급
	•	✅ 리프레시 토큰 유효
	•	✅ 리프레시 토큰 만료/변조

	9. POST /api/users/wish/{postId} – 관심 등록
	•	✅ 관심 등록 성공
	•	✅ 비로그인 상태
	•	✅ 존재하지 않는 postId

	10. DELETE /api/users/wish/{postId} – 관심 삭제
	•	✅ 관심 삭제 성공
	•	✅ 비로그인 상태
	•	✅ 관심 등록이 안된 postId 삭제 요청

	11. GET /api/users/check/account – 계좌 실명 인증
	•	✅ 정상 인증
	•	✅ 지원하지 않는 은행
	•	✅ 은행명 누락
	•	✅ 계좌번호 누락
	•	✅ 이름 누락
	•	✅ 계좌 정보 불일치로 인한 실패

     */

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
    @DisplayName("회원가입 실패 - 비밀번호 형식 오류")
    void signUp_fail_invalid_password() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "email@email.com", "pass",
                "nickname", "realName", "01000000000",
                "bankName", "12345679012", null
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    @DisplayName("회원가입 실패 - 닉네임 누락")
    void signUp_fail_missing_nickname() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "email@email.com", "password123!",
                null, "realName", "01000000000",
                "bankName", "12345679012", null
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.nickname").exists());
    }

    @Test
    @DisplayName("회원가입 실패 - 실명 누락")
    void signUp_fail_missing_realName() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "email@email.com", "password123!",
                "nickname", null, "01000000000",
                "bankName", "12345679012", null
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.name").exists());
    }

    @Test
    @DisplayName("회원가입 실패 - 전화번호 형식 오류")
    void signUp_fail_invalid_phone_number() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "email@email.com", "password123!",
                "nickname", "realName", "010-0000-0000",
                "bankName", "12345679012", null
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.phoneNumber").exists());
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    void signUp_fail_duplicate_email() throws Exception {
        Mockito.doThrow(new com.moogsan.moongsan_backend.domain.user.exception.base.UserException(
                        com.moogsan.moongsan_backend.domain.user.exception.code.UserErrorCode.DUPLICATE_VALUE,
                        "이미 등록된 이메일입니다."))
                .when(signUpService).signUp(any(), any());

        SignUpRequest request = new SignUpRequest(
                "duplicate@email.com", "password123!",
                "nickname", "realName", "01000000000",
                "bankName", "12345679012", null
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 등록된 이메일입니다."));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 닉네임")
    void signUp_fail_duplicate_nickname() throws Exception {
        Mockito.doThrow(new com.moogsan.moongsan_backend.domain.user.exception.base.UserException(
                        com.moogsan.moongsan_backend.domain.user.exception.code.UserErrorCode.DUPLICATE_VALUE,
                        "이미 등록된 닉네임입니다."))
                .when(signUpService).signUp(any(), any());

        SignUpRequest request = new SignUpRequest(
                "email@email.com", "password123!",
                "dupNick", "realName", "01000000000",
                "bankName", "12345679012", null
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 등록된 닉네임입니다."));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 전화번호")
    void signUp_fail_duplicate_phone_number() throws Exception {
        Mockito.doThrow(new com.moogsan.moongsan_backend.domain.user.exception.base.UserException(
                        com.moogsan.moongsan_backend.domain.user.exception.code.UserErrorCode.DUPLICATE_VALUE,
                        "이미 등록된 전화번호입니다."))
                .when(signUpService).signUp(any(), any());

        SignUpRequest request = new SignUpRequest(
                "email@email.com", "password123!",
                "nickname", "realName", "01000000000",
                "bankName", "12345679012", null
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 등록된 전화번호입니다."));
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
    @DisplayName("로그인 실패 - 잘못된 이메일 또는 비밀번호")
    void login_fail_invalid_credentials() throws Exception {
        Mockito.doThrow(new com.moogsan.moongsan_backend.domain.user.exception.base.UserException(
                        UserErrorCode.UNAUTHORIZED,
                        "이메일 또는 비밀번호가 올바르지 않습니다."))
                .when(loginService).login(any(), any());

        LoginRequest request = new LoginRequest("wrong@email.com", "wrongPassword!");

        mockMvc.perform(post("/api/users/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 형식 오류")
    void login_fail_invalid_email_format() throws Exception {
        LoginRequest request = new LoginRequest("invalid-email", "password123!");

        mockMvc.perform(post("/api/users/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.email").value("올바른 이메일 형식이어야 합니다."));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 누락")
    void login_fail_missing_password() throws Exception {
        LoginRequest request = new LoginRequest("email@email.com", null);

        mockMvc.perform(post("/api/users/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.password").exists());
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
    @DisplayName("이메일 중복 체크 - 이미 존재")
    void checkEmail_exists() throws Exception {
        Mockito.when(emailService.checkEmail(anyString()))
                .thenReturn(new CheckDuplicationResponse("YES"));

        mockMvc.perform(get("/api/users/check-email")
                        .param("email", "used@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이미 등록된 이메일입니다."));
    }

    @Test
    @DisplayName("이메일 중복 체크 - 파라미터 누락")
    void checkEmail_missing_param() throws Exception {
        mockMvc.perform(get("/api/users/check-email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("닉네임 중복 체크 - 사용 가능")
    void checkNickname_available() throws Exception {
        Mockito.when(nicknameService.checkNickname(anyString()))
                .thenReturn(new CheckDuplicationResponse("NO"));

        mockMvc.perform(get("/api/users/check-nickname")
                        .param("nickname", "availableNick"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용 가능한 닉네임입니다."));
    }

    @Test
    @DisplayName("닉네임 중복 체크 - 이미 존재")
    void checkNickname_exists() throws Exception {
        Mockito.when(nicknameService.checkNickname(anyString()))
                .thenReturn(new CheckDuplicationResponse("YES"));

        mockMvc.perform(get("/api/users/check-nickname")
                        .param("nickname", "takenNick"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이미 등록된 닉네임입니다."));
    }

    @Test
    @DisplayName("닉네임 중복 체크 - 파라미터 누락")
    void checkNickname_missing_param() throws Exception {
        mockMvc.perform(get("/api/users/check-nickname"))
                .andExpect(status().isBadRequest());
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

    @Test
    @DisplayName("유저 프로필 조회 실패 - 비로그인 상태")
    void getProfile_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Requested-With", "XMLHttpRequest"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(id = 1L)
    @DisplayName("로그아웃 성공")
    void logout_success() throws Exception {
        mockMvc.perform(delete("/api/users/token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃이 성공적으로 처리되었습니다."));
    }

    @Test
    @DisplayName("로그아웃 실패 - 비로그인 상태")
    void logout_unauthenticated() throws Exception {
        mockMvc.perform(delete("/api/users/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Requested-With", "XMLHttpRequest"))
                .andDo(print()) // 응답 상태/내용 출력
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(id = 1L)
    @DisplayName("회원 탈퇴 성공")
    void withdraw_success() throws Exception {
        mockMvc.perform(delete("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원탈퇴가 완료되었습니다."));
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 비로그인 상태")
    void withdraw_unauthenticated() throws Exception {
        mockMvc.perform(delete("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("액세스 토큰 재발급 성공 - 유효한 리프레시 토큰")
    void refreshToken_success() throws Exception {
        Mockito.doNothing().when(tokenRefreshService).refreshAccessToken(any(), any());

        mockMvc.perform(post("/api/users/token/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("RefreshToken", "validRefreshToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access Token이 재발급되었습니다."));
    }

    @Test
    @DisplayName("액세스 토큰 재발급 실패 - 리프레시 토큰 만료 또는 변조")
    void refreshToken_fail_invalidToken() throws Exception {
        Mockito.doThrow(new com.moogsan.moongsan_backend.domain.user.exception.base.UserException(
                        UserErrorCode.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."))
                .when(tokenRefreshService).refreshAccessToken(any(), any());

        mockMvc.perform(post("/api/users/token/refresh")
                        .header("Authorization", "Bearer invalidRefreshToken"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."));
    }

    @Test
    @WithMockCustomUser(id = 1L)
    @DisplayName("관심 등록 성공")
    void addWish_success() throws Exception {
        mockMvc.perform(post("/api/users/wish/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("관심 등록이 완료되었습니다."));
    }

    @Test
    @DisplayName("관심 등록 실패 - 비로그인 상태")
    void addWish_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/users/wish/100"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(id = 1L)
    @DisplayName("관심 등록 실패 - 존재하지 않는 postId")
    void addWish_nonexistentPost() throws Exception {
        Mockito.doThrow(new com.moogsan.moongsan_backend.domain.user.exception.base.UserException(
                        UserErrorCode.NOT_FOUND, "존재하지 않는 게시글입니다."))
                .when(wishService).addWish(anyLong(), eq(999L));

        mockMvc.perform(post("/api/users/wish/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."));
    }

    @Test
    @WithMockCustomUser(id = 1L)
    @DisplayName("관심 삭제 성공")
    void removeWish_success() throws Exception {
        mockMvc.perform(delete("/api/users/wish/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("관심 등록이 취소되었습니다."));
    }

    @Test
    @DisplayName("관심 삭제 실패 - 비로그인 상태")
    void removeWish_unauthenticated() throws Exception {
        mockMvc.perform(delete("/api/users/wish/100"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(id = 1L)
    @DisplayName("관심 삭제 실패 - 관심 등록이 안된 postId")
    void removeWish_notWishedPost() throws Exception {
        Mockito.doThrow(new com.moogsan.moongsan_backend.domain.user.exception.base.UserException(
                        UserErrorCode.NOT_FOUND, "관심 등록이 되어 있지 않은 게시글입니다."))
                .when(wishService).removeWish(anyLong(), eq(999L));

        mockMvc.perform(delete("/api/users/wish/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("관심 등록이 되어 있지 않은 게시글입니다."));
    }

    @Nested
    @DisplayName("계좌 실명 인증 API")
    class CheckAccountTests {

        @Test
        @DisplayName("정상 인증 - 지원 은행 + 실명 일치")
        void checkAccount_success() throws Exception {
            mockMvc.perform(get("/api/users/check/account")
                            .param("name", "박건")
                            .param("accountBank", "광주은행")
                            .param("accountNumber", "613121041573"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("본인인증이 성공하였습니다."));
        }

        @Test
        @DisplayName("지원하지 않는 은행")
        void checkAccount_invalidBank() throws Exception {
            mockMvc.perform(get("/api/users/check/account")
                            .param("name", "박건")
                            .param("accountBank", "모르겠은행")
                            .param("accountNumber", "613121041573"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("지원하지 않는 은행입니다."));
        }

        @Test
        @DisplayName("이름 누락")
        void checkAccount_missingName() throws Exception {
            mockMvc.perform(get("/api/users/check/account")
                            .param("accountBank", "광주은행")
                            .param("accountNumber", "613121041573"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("은행명 누락")
        void checkAccount_missingBank() throws Exception {
            mockMvc.perform(get("/api/users/check/account")
                            .param("name", "박건")
                            .param("accountNumber", "613121041573"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("계좌번호 누락")
        void checkAccount_missingAccountNumber() throws Exception {
            mockMvc.perform(get("/api/users/check/account")
                            .param("name", "박건")
                            .param("accountBank", "광주은행"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("계좌 정보 불일치로 인한 실패")
        void checkAccount_notMatched() throws Exception {
            Mockito.doThrow(new UserException(UserErrorCode.UNAUTHORIZED, "계좌주명이 실명과 일치하지 않습니다."))
                    .when(checkAccountService)
                    .checkBankAccountHolder(eq("034"), eq("613121041573"), eq("홍길순"));

            mockMvc.perform(get("/api/users/check/account")
                            .param("name", "홍길순")
                            .param("accountBank", "광주은행")
                            .param("accountNumber", "613121041573"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("계좌주명이 실명과 일치하지 않습니다."));
        }
    }
}