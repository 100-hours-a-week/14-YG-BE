package com.moogsan.moongsan_backend.image.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Presigned URL과 S3 Key 정보를 담아 프론트엔드에 반환
 */
@AllArgsConstructor
@Getter
public class PresignResponse {
    private String key;   // S3 객체 키 (posts 전환 또는 temp 경로 등)
    private String url;   // Presigned PUT URL
}
