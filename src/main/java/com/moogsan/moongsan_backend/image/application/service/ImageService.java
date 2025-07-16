package com.moogsan.moongsan_backend.image.application.service;

import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.command.request.CreateGroupBuyRequest;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.command.request.UpdateGroupBuyRequest;
import com.moogsan.moongsan_backend.image.application.mapper.ImageMapper;
import com.moogsan.moongsan_backend.image.domain.entity.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static com.moogsan.moongsan_backend.image.domain.constant.ImageConstants.GROUP_BUYS_PREFIX;
import static com.moogsan.moongsan_backend.image.domain.constant.ImageConstants.TMP_PREFIX;

@Service
@Transactional
@RequiredArgsConstructor
public class ImageService {

    private final ImageMapper imageMapper;
    private final S3Service s3Service;

    public void moveAndMapImages(CreateGroupBuyRequest request, GroupBuy groupBuy) {
        List<String> destKeys = request.getImageKeys().stream()
                .map(srcKey -> {
                    String fileName = srcKey.substring(srcKey.lastIndexOf('/') + 1);
                    String destKey  = GROUP_BUYS_PREFIX + fileName;
                    s3Service.moveImage(srcKey, destKey);
                    return destKey;
                }).toList();

        imageMapper.mapImagesToGroupBuy(destKeys, groupBuy);
    }

    public GroupBuy syncUpdatedImages(UpdateGroupBuyRequest request, GroupBuy groupBuy) {
        List<String> requested = Optional.ofNullable(request.getImageKeys())
                .orElseGet(Collections::emptyList);
        List<String> existing  = groupBuy.getImages().stream()
                .map(Image::getImageKey)
                .toList();

        // 삭제 대상: 기존에 있었지만 요청에 없는 키
        existing.stream()
                .filter(key -> !requested.contains(key))
                .forEach(key -> {
                    s3Service.deleteImage(key);
                });

        // S3 파일 이동
        List<String> finalKeys = new ArrayList<>();
        for (String key : requested) {
            if (key.startsWith(GROUP_BUYS_PREFIX)) {
                // 이미 영구폴더에 있음 → 그대로
                finalKeys.add(key);
            } else if (key.startsWith(TMP_PREFIX)) {
                String fileName = key.substring(key.lastIndexOf('/') + 1);
                String destKey  = GROUP_BUYS_PREFIX + fileName;
                s3Service.moveImage(key, destKey);
                finalKeys.add(destKey);
            } else {
                throw new IllegalArgumentException("Invalid image key: " + key);
            }
        }

        imageMapper.mapImagesToGroupBuy(finalKeys, groupBuy);
        return groupBuy;
    }
}
