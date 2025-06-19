package com.moogsan.moongsan_backend.unit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.domain.user.dto.request.UpdateProfileAccountRequest;
import com.moogsan.moongsan_backend.domain.user.dto.request.UpdateProfileImageRequest;
import com.moogsan.moongsan_backend.domain.user.dto.request.UpdateProfilePasswordRequest;
import com.moogsan.moongsan_backend.domain.user.dto.request.UpdateProfileRequest;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.service.UpdateProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
    UpdateProfileContoller Test 목록

    /api/users/profile/image
    -200
    -400의 경우 없음, null일 시, 기본 이미지 사용

    /api/users/profile/password
    -200
    -400: 비밀번호 형식 오류

    /api/users/profile/account
    -200
    -400의 경우 없음, 본인인증 전에는 프론트에서 비활성화

    /api/users/profile
    -200
    -400: 닉네임 확인
    -400:
 */

@Disabled
@SpringBootTest
@AutoConfigureMockMvc
@Import(UpdateProfileControllerTest.TestConfig.class) // 아래 TestConfig 설정을 현재 테스트에 주입
public class UpdateProfileControllerTest {


    private User user;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("test123!")
                .nickname("testuser")
                .name("테스트유저")
                .imageKey(null)
                .type("USER")
                .build();
        auth = new UsernamePasswordAuthenticationToken(
            new CustomUserDetails(user), null, new CustomUserDetails(user).getAuthorities()
        );
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UpdateProfileService updateProfileService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public UpdateProfileService updateProfileService() {
            return org.mockito.Mockito.mock(UpdateProfileService.class);
        }
    }

    @Test
    @DisplayName("프로필 이미지 수정 성공")
    void updateProfileImage_shouldReturn200() throws Exception {
        UpdateProfileImageRequest request = new UpdateProfileImageRequest();
        request.setImageKey("tesT.jpg");

        mockMvc.perform(patch("/api/users/profile/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(auth))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("프로필 이미지가 수정되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        // 서비스가 정상적으로 호출되었는지 확인
        verify(updateProfileService).updateProfileImage(
                org.mockito.Mockito.eq(1L),
                org.mockito.Mockito.argThat(arg -> arg != null && "tesT.jpg".equals(arg.getImageKey()))
        );
    }

    @Test
    @DisplayName("비밀번호 수정 성공")
    void updatePassword_shouldReturn200() throws Exception {
        // 요청 객체 생성
        UpdateProfilePasswordRequest request = new UpdateProfilePasswordRequest();
        request.setPassword("newTest123!");

        mockMvc.perform(patch("/api/users/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(auth))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 수정되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(updateProfileService).updatePassword(
                org.mockito.Mockito.eq(1L),
                org.mockito.Mockito.argThat(arg ->
                        arg != null &&
                                "newTest123!".equals(arg.getPassword()))
        );
    }

    @Test
    @DisplayName("비밀번호 수정 실패 - 잘못된 요청 형식")
    void updatePassword_shouldReturn400_whenRequestInvalid() throws Exception {
        UpdateProfilePasswordRequest request = new UpdateProfilePasswordRequest();
        request.setPassword("failedtest");

        mockMvc.perform(patch("/api/users/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(auth))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.password").value("비밀번호는 8~30자이며, 반드시 하나 이상의 특수문자(!@#$%^*+=-)를 포함해야 합니다."));
    }

    @Test
    @DisplayName("계좌정보 수정 성공")
    void updateAccountInfo_shouldReturn200() throws Exception {
        UpdateProfileAccountRequest request = new UpdateProfileAccountRequest();
        request.setAccountBank("신한은행");
        request.setAccountNumber("110123456789");
        request.setName("유저");

        mockMvc.perform(patch("/api/users/profile/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(auth))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("계좌정보가 수정되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(updateProfileService).updateAccountInfo(
                org.mockito.Mockito.eq(1L),
                org.mockito.Mockito.argThat(arg ->
                        arg != null &&
                                "신한은행".equals(arg.getAccountBank()) &&
                                "110123456789".equals(arg.getAccountNumber()))
        );
    }

    @Test
    @DisplayName("기본정보 수정 성공")
    void updateBasicInfo_shouldReturn200() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("updatedUser");

        mockMvc.perform(patch("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(auth))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("기본 정보가 수정되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(updateProfileService).updateBasicInfo(
                org.mockito.Mockito.eq(1L),
                org.mockito.Mockito.argThat(arg ->
                        arg != null && "updatedUser".equals(arg.getNickname()))
        );
    }

    @Test
    @DisplayName("기본정보 수정 실패 - 잘못된 요청 형식")
    void updateBasicInfo_shouldReturn400_whenInvalid() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname(""); // 유효하지 않은 닉네임

        mockMvc.perform(patch("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(auth))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.nickname").exists());
    }

    @Test
    @DisplayName("기본정보 수정 실패 - 유효하지 않은 전화번호")
    void updateBasicInfo_shouldReturn400_whenInvalidPhoneNumber() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setPhoneNumber("01012345");

        mockMvc.perform(patch("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authentication(auth))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.phoneNumber").exists());
    }
}