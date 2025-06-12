package com.moogsan.moongsan_backend.domain.user.service;

import com.moogsan.moongsan_backend.domain.user.dto.request.UpdateProfileAccountRequest;
import com.moogsan.moongsan_backend.domain.user.dto.request.UpdateProfileRequest;
import com.moogsan.moongsan_backend.domain.user.dto.request.UpdateProfileImageRequest;
import com.moogsan.moongsan_backend.domain.user.dto.request.UpdateProfilePasswordRequest;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.exception.code.UserErrorCode;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 프로필 이미지 수정
    public void updateProfileImage(Long userId, UpdateProfileImageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND));
        if (request.getImageKey() == null && !request.toString().contains("imageKey")) {
            throw new UserException(UserErrorCode.INVALID_INPUT, "잘못된 요청입니다. 'imageKey' 필드가 필요합니다.");
        }
        user.updateImage(request.getImageKey());
    }

    // 비밀번호 수정
    public void updatePassword(Long userId, UpdateProfilePasswordRequest request) {
        if (request.getPassword() == null || !request.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^*+=-])[A-Za-z\\d!@#$%^*+=-]{8,30}$")) {
            throw new UserException(UserErrorCode.INVALID_INPUT, "비밀번호가 올바르지 않습니다.\n(숫자와 영어, 특수문자로 이루어진 8자 이상, 30자 이하의 문자열,\n특수 문자(!@#$%^*+=-) 한개 이상 입력)");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND));
        user.updatePassword(passwordEncoder.encode(request.getPassword()));
    }

    // 계좌 정보 수정
    public void updateAccountInfo(Long userId, UpdateProfileAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND));
        user.updateAccount(request.getAccountBank(), request.getAccountNumber(), request.getName());
    }

    // 기본 정보 수정 (이름, 닉네임, 전화번호)
    public void updateBasicInfo(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND));

        String nickname = request.getNickname() != null ? request.getNickname() : user.getNickname();
        String phoneNumber = request.getPhoneNumber() != null ? request.getPhoneNumber() : user.getPhoneNumber();

        if (!nickname.equals(user.getNickname()) && userRepository.existsByNickname(nickname)) {
            throw new UserException(UserErrorCode.DUPLICATE_VALUE, "이미 등록된 닉네임입니다.");
        }

        if (!phoneNumber.equals(user.getPhoneNumber()) && userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new UserException(UserErrorCode.DUPLICATE_VALUE, "이미 등록된 전화번호입니다.");
        }

        user.updateBasicInfo(nickname, phoneNumber);
    }
}
