package com.moogsan.moongsan_backend.image.application.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class ImageService {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private final Path uploadPath;

    public ImageService() throws IOException {
        this.uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        if (!Files.exists(this.uploadPath)) {
            Files.createDirectories(this.uploadPath);
        }
    }

    // 다중 이미지 업로드 처리
    public List<String> storeImages(List<MultipartFile> imageFiles) throws IOException {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : imageFiles) {
            if (file.isEmpty()) continue;

            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String extension = originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String storedFilename = UUID.randomUUID() + extension;

            Path filePath = this.uploadPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            imageUrls.add("/uploads/" + storedFilename);
        }
        return imageUrls;
    }
}
