package com.moogsan.moongsan_backend.domain.user.service;

import com.moogsan.moongsan_backend.domain.user.dto.request.MyPageAccountRequest;
import com.moogsan.moongsan_backend.domain.user.dto.request.MyPageBasicRequest;
import com.moogsan.moongsan_backend.domain.user.dto.request.MyPageImageRequest;
import com.moogsan.moongsan_backend.domain.user.dto.request.MyPagePasswordRequest;
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
    public void updateProfileImage(Long userId, MyPageImageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND));
        user.updateImage(request.getImageUrl());
    }

    // 비밀번호 수정
    public void updatePassword(Long userId, MyPagePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND));
        user.updatePassword(passwordEncoder.encode(request.getPassword()));
    }

    // 계좌 정보 수정
    public void updateAccountInfo(Long userId, MyPageAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND));
        user.updateAccount(request.getAccountBank(), request.getAccountNumber());
    }

    // 기본 정보 수정 (이름, 닉네임, 전화번호)
    public void updateBasicInfo(Long userId, MyPageBasicRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND));

        String name = request.getName() != null ? request.getName() : user.getName();
        String nickname = request.getNickname() != null ? request.getNickname() : user.getNickname();
        String phoneNumber = request.getPhoneNumber() != null ? request.getPhoneNumber() : user.getPhoneNumber();

        if (!nickname.equals(user.getNickname()) && userRepository.existsByNickname(nickname)) {
            throw new UserException(UserErrorCode.DUPLICATE_VALUE, "이미 등록된 닉네임입니다.");
        }

        if (!phoneNumber.equals(user.getPhoneNumber()) && userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new UserException(UserErrorCode.DUPLICATE_VALUE, "이미 등록된 전화번호입니다.");
        }

        user.updateBasicInfo(name, nickname, phoneNumber);
    }
}
