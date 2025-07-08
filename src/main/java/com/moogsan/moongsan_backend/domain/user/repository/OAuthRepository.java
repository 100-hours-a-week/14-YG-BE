package com.moogsan.moongsan_backend.domain.user.repository;

import com.moogsan.moongsan_backend.domain.user.entity.OAuth;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OAuthRepository extends JpaRepository<OAuth, Long> {

    Optional<OAuth> findByProviderAndProviderId(String provider, String providerId);

    boolean existsByProviderAndUserId(String provider, Long userId);

    Optional<OAuth> findByUserAndProvider(User user, String provider);
}
