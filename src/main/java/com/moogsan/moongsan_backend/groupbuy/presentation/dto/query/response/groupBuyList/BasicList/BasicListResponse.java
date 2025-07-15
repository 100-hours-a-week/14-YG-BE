package com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyList.BasicList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.ImageResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BasicListResponse {

    // 식별/메타
    private Long postId;           // 공구 게시글 아이디
    private String title;            // 공구 게시글 제목
    private String name;             // 상품명
    private String postStatus;       // 공구 진행 상태(OPEN, CLOSED, ENDED)

    // 본문
    private List<ImageResponse> imageKeys;  // 상품 이미지 목록

    // 숫자 데이터
    private int unitPrice;           // 주문 단위 가격
    private int unitAmount;          // 주문 단위 수량
    private int soldAmount;          // 판매 수량(totalAmount - leftAmount)
    private int totalAmount;         // 전체 상품 수량
    private int participantCount;    // 참여 인원 수

    // 상태/플래그
    private boolean dueSoon;         // 마감 임박 여부
    private boolean isWish;          // 관심 여부

    // 날짜
    private LocalDateTime createdAt; // 생성 일시

    @JsonProperty("isWish")
    public boolean isWish() {
        return isWish;
    }

}
