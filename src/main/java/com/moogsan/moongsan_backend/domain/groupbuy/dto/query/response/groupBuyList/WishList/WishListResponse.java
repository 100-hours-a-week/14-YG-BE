package com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.WishList;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WishListResponse {

    // 식별/메타
    private Long postId;         // 공구 게시글 아이디
    private String title;          // 공구 게시글 제목
    private String postStatus;     // 공구 진행 상태

    // 본문
    private String location;       // 거래 장소
    private String imageKey;       // thumbnail 이미지

    // 숫자 데이터
    private int unitPrice;         // 상품 단위 가격
    private int soldAmount;        // 판매 수량
    private int totalAmount;       // 상품 주문 수량: totalAmount - leftAmount
    private int participantCount;  // 참여 인원 수

    // 상태/플래그
    private boolean dueSoon;       // 마감 임박 여부
    private boolean isWish;        // 관심 여부

    @JsonProperty("isWish")
    public boolean isWish() {
        return isWish;
    }
}
