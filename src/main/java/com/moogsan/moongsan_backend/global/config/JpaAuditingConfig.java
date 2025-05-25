package com.moogsan.moongsan_backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableJpaAuditing
@Profile("!test")   // 테스트 프로필이면 빈을 만들지 않도록 설정
public class JpaAuditingConfig {}

