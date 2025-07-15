package com.moogsan.moongsan_backend.image.presentation.controller;

import com.moogsan.moongsan_backend.image.presentation.dto.ImageUploadResponse;
import com.moogsan.moongsan_backend.image.application.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/image")
@Slf4j
public class ImageController {

    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<ImageUploadResponse> uploadImages(
            @RequestParam("imageFiles") List<MultipartFile> imageFiles) {
        try {
            List<String> urls = imageService.storeImages(imageFiles);
            if (urls.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ImageUploadResponse("upload_fail_client", Collections.emptyList()));
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ImageUploadResponse("upload_success", urls));

        } catch (IOException e) {
            log.error("Image upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImageUploadResponse("upload_fail_server", Collections.emptyList()));
        }
    }
}
