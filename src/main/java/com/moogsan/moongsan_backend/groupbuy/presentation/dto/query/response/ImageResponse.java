package com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ImageResponse {

    // 식별/메타
    private int imageSeqNo;     // 이미지 순서

    // 본문
    private String imageKey;    // 이미지 URL(리사이즈된 버전 URL)

    // 상태/플래그
    private boolean thumbnail;  // 썸네일 여부

}
