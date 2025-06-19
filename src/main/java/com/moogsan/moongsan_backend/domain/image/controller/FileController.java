package com.moogsan.moongsan_backend.domain.image.controller;

import com.moogsan.moongsan_backend.domain.image.dto.PresignResponse;
import com.moogsan.moongsan_backend.domain.image.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class FileController {

    private final S3Service s3Service;

    /**
     * 이미지 엔티티를 먼저 생성한 뒤, S3 PUT용 Presigned URL을 발급합니다.
     * - 이미지 레코드 생성 시점에 imageId와 S3 key를 DB에 저장합니다.
     * - 반환된 URL로 클라이언트가 직접 S3에 업로드할 수 있습니다.
     *
     */
    @GetMapping("/presign")
    public ResponseEntity<PresignResponse> presign() {
        PresignResponse presign = s3Service.presign();
        return ResponseEntity.ok(presign);
    }
}
