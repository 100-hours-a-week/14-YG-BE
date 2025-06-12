package com.moogsan.moongsan_backend.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * User 엔티티 클래스.
 * 'users' 테이블과 매핑되며, 회원 정보를 관리한다.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB에서 자동 증가
    private Long id;    // 고유 식별자 (PK)

    @Column(nullable = false, length = 255)
    private String email;   // 이메일 (로그인 ID 역할 가능)

    @Column(length = 60)
    private String nickname;// 닉네임

    @Column(length = 36)
    private String name;    // 실명

    @Column(name = "phone_number", length = 11)
    private String phoneNumber; // 전화번호 (하이픈 제외)

    @Column(length = 60)
    private String password;    // 비밀번호 (암호화 저장)

    @Column(name = "account_bank", length = 30)
    private String accountBank; // 계좌 은행명

    @Column(name = "account_number", length = 32)
    private String accountNumber;   // 계좌 번호

    @Builder.Default
    @Column(name = "image_key", length = 512)
    private String imageKey = null; // 프로필 이미지 URL

    @Column(nullable = false, length = 30)
    private String type;    // 사용자 유형 (예: USER, ADMIN)

    @Column(nullable = false, length = 30)
    private String status;  // 사용자 상태 (예: ACTIVE, SUSPENDED, DEACTIVATED)

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt; // 가입 시각

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;   // 마지막 정보 수정 시각

    @Column(name = "logout_at")
    private LocalDateTime logoutAt; // 마지막 로그아웃 시각

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;    // 탈퇴(삭제) 시각

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_" + this.type);
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.deletedAt == null && !"DELETED".equalsIgnoreCase(this.status);
    }
    public void setLastLoginAt() {
        this.logoutAt = java.time.LocalDateTime.now();
    }

    public void updateImage(String imageKey) {
        this.imageKey = imageKey;
        this.modifiedAt = LocalDateTime.now();
    }

    public void updatePassword(@NotBlank String encodedPassword) {
        this.password = encodedPassword;
        this.modifiedAt = LocalDateTime.now();
    }

    public void updateAccount(@NotBlank(message = "은행명은 필수입니다.") String accountBank,
                              @NotBlank(message = "계좌번호는 필수입니다.")
                              @Pattern(regexp = "^\\d+$", message = "계좌번호는 숫자만 입력해야 합니다.") String accountNumber,
                              @NotBlank(message = "실명은 필수입니다.") String name) {
        this.accountBank = accountBank;
        this.accountNumber = accountNumber;
        this.name = name;
        this.modifiedAt = LocalDateTime.now();
    }

    public void updateBasicInfo(@Size(min = 2, max = 12, message = "닉네임은 2자 이상 12자 이하여야 합니다.") String nickname,
                                 @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 하이픈(-) 없이 10~11자리 숫자여야 합니다.") String phoneNumber) {
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.modifiedAt = LocalDateTime.now();
    }
}