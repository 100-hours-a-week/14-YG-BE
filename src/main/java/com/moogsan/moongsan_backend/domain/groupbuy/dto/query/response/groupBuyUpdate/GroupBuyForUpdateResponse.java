package com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyUpdate;


import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.ImageResponse;
import lombok.Builder;
import lombok.Getter;


import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class GroupBuyForUpdateResponse {

    // 식별/메타
    private String title;                  // 공구 게시글 제목
    private String name;                   // 상품명

    // 본문
    private String description;            // 공구 상세 설명
    private String url;                    // 상품 URL
    private List<ImageResponse> imageUrls; // 이미지 URL 리스트
    private LocalDateTime dueDate;         // 마감 일자
    private String location;               // 거래 장소
    private LocalDateTime pickupDate;      // 픽업 예정 일자

    // 숫자 데이터
    private int price;                     // 상품 총 가격
    private int unitAmount;                // 상품 주문 단위 수량
    private int totalAmount;               // 전체 상품 수량
}
