package com.moogsan.moongsan_backend.unit.image.service;

import com.moogsan.moongsan_backend.domain.image.dto.PresignResponse;
import com.moogsan.moongsan_backend.domain.image.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    @Captor
    private ArgumentCaptor<CopyObjectRequest> copyCaptor;

    @Captor
    private ArgumentCaptor<DeleteObjectRequest> deleteCaptor;

    private final String bucket = "test-bucket";

    @BeforeEach
    void setUp() {
        // private 필드인 bucket 값을 테스트용으로 주입
        ReflectionTestUtils.setField(s3Service, "bucket", bucket);
    }

    @Test
    void presign_shouldReturnKeyAndUrl() throws MalformedURLException {
        // given
        PresignedPutObjectRequest mockPresigned = mock(PresignedPutObjectRequest.class);
        URL dummyUrl = new URL("https://example.com/upload");
        when(mockPresigned.url()).thenReturn(dummyUrl);
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresigned);

        // when
        PresignResponse resp = s3Service.presign();

        // then
        assertThat(resp.getUrl()).isEqualTo(dummyUrl.toString());
        assertThat(resp.getKey()).startsWith("images/");
        verify(s3Presigner).presignPutObject(any(PutObjectPresignRequest.class));
    }

    @Test
    void moveImage_shouldCopyThenDelete() {
        // given
        String srcKey  = "tmp/abc-123.png";
        String destKey = "group-buys/42/abc-123.png";
        when(s3Client.copyObject(any(CopyObjectRequest.class)))
                .thenReturn(CopyObjectResponse.builder().build());
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        // when
        s3Service.moveImage(srcKey, destKey);

        // then
        verify(s3Client).copyObject(copyCaptor.capture());
        verify(s3Client).deleteObject(deleteCaptor.capture());

        CopyObjectRequest copyReq = copyCaptor.getValue();
        assertThat(copyReq.sourceBucket()).isEqualTo(bucket);
        assertThat(copyReq.sourceKey())  .isEqualTo(srcKey);
        assertThat(copyReq.destinationBucket()).isEqualTo(bucket);
        assertThat(copyReq.destinationKey())  .isEqualTo(destKey);

        DeleteObjectRequest delReq = deleteCaptor.getValue();
        assertThat(delReq.bucket()).isEqualTo(bucket);
        assertThat(delReq.key())   .isEqualTo(srcKey);
    }

    @Test
    void deleteImage_shouldDeleteObject() {
        // given
        String key = "users/7/avatar.png";
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        // when
        s3Service.deleteImage(key);

        // then
        verify(s3Client).deleteObject(deleteCaptor.capture());
        DeleteObjectRequest delReq = deleteCaptor.getValue();
        assertThat(delReq.bucket()).isEqualTo(bucket);
        assertThat(delReq.key())   .isEqualTo(key);
    }
}
