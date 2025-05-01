package com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipatedList;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParticipatedListResponse {

    // 식별/메타
    private Long postId;         // 공구 게시글 아이디
    private String title;          // 공구 게시글 제목
    private String postStatus;     // 공구 진행 상태(OPEN, CLOSED, ENDED)

    // 본문
    private String location;       // 거래 장소
    private String imageUrl;       // thumbnail 이미지

    // 숫자 데이터
    private int price;             // 총 상품 가격
    private int orderQuantity;     // 주문 개수(참여자)
    private int soldAmount;        // 판매 수량(totalAmount - leftAmount)
    private int totalAmount;       // 상품 전체 수량
    private int participantCount;  // 참여 인원 수

    // 상태/플래그
    private String orderStatus;    // 주문 상태: PENDING(입금 전), PAID(입금 됨, 입금 확인 전), CONFIRMED(입금 확인), CANCELED(취소)
    private boolean dueSoon;       // 마감 임박 여부
    private boolean isWish;        // 관심 여부

}
