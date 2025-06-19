package com.moogsan.moongsan_backend.domain.image.service;

import com.moogsan.moongsan_backend.domain.image.dto.PresignResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

/**
 * FileService: 이미지 업로드를 위한 Presigned URL 발급만 담당
 */
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * S3에 업로드할 Presigned PUT URL 발급
     * @return key, url 정보가 담긴 PresignResponse
     */
    public PresignResponse presign() {
        // 1) S3 key 생성 (images/{UUID})
        String key = "tmp/" + UUID.randomUUID();

        // 2) Presign 요청 객체 생성
        PutObjectRequest objReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PutObjectPresignRequest preReq = PutObjectPresignRequest.builder()
                .putObjectRequest(objReq)
                .signatureDuration(Duration.ofMinutes(2))
                .build();

        // 3) URL 발급
        String url = s3Presigner.presignPutObject(preReq)
                .url().toString();

        // 4) DTO 반환 (imageId 없이 key + url)
        return new PresignResponse(key, url);
    }

    /**
     * S3 객체 이동: copy → delete
     */
    public void moveImage(String srcKey, String destKey) {
        try {
            // 1) 복사
            CopyObjectRequest copyReq = CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(srcKey)
                    .destinationBucket(bucket)
                    .destinationKey(destKey)
                    .build();
            s3Client.copyObject(copyReq);

            // 2) 원본 삭제
            DeleteObjectRequest delReq = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(srcKey)
                    .build();
            s3Client.deleteObject(delReq);

        } catch (S3Exception e) {
            throw new RuntimeException("Failed to move image: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * S3 객체 삭제
     */
    public void deleteImage(String key) {
        try {
            DeleteObjectRequest delReq = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(delReq);
        } catch (S3Exception e) {
            // TODO: 케이스별 예외처리 (존재하지 않을 때, 권한 부족 등)
            throw new RuntimeException("Failed to delete image: " + e.awsErrorDetails().errorMessage(), e);
        }
    }
}
