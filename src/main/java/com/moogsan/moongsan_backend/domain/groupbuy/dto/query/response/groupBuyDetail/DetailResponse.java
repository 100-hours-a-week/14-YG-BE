package com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.ImageResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DetailResponse {

    // 식별/메타
    private Long postId;                  // 공구 게시글 아이디
    private String title;                   // 공구 게시글 제목
    private String name;                    // 공구 상품명
    private String postStatus;              // 공구 진행 상태

    // 본문
    private String description;             // 상품 상세 설명
    private String url;                     // 상품 URL
    private List<ImageResponse> imageKeys;  // 상품 이미지 목록
    private String location;                // 장소

    // 숫자 데이터
    private int unitPrice;                  // 상품 단위 당 가격
    private int unitAmount;                 // 상품 주문 단위
    private int soldAmount;                 // 판매 수량
    private int totalAmount;                // 전체 수량
    private int leftAmount;                 // 남은 수량
    private int participantCount;           // 참여 인원 수

    // 상태/플래그
    private boolean dueSoon;                // 마감 임박 여부
    private boolean isWish;                 // 관심 여부
    private boolean isHost;                 // 주최 여부
    private boolean isParticipant;          // 참여 여부

    // 날짜
    private LocalDateTime createdAt;       // 공구글 생성 시각
    private LocalDateTime dueDate;         // 마감 일자
    private LocalDateTime pickupDate;      // 픽업 일자

    // 연관 객체
    private UserProfileResponse userProfileResponse;    // 주최자 정보

    @JsonProperty("isWish")
    public boolean isWish() {
        return isWish;
    }

    @JsonProperty("isHost")
    public boolean isHost() {
        return isHost;
    }

    @JsonProperty("isParticipant")
    public boolean isParticipant() {
        return isParticipant;
    }
}
