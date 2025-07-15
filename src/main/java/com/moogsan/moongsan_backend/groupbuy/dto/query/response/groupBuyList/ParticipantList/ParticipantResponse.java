package com.moogsan.moongsan_backend.groupbuy.dto.query.response.groupBuyList.ParticipantList;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParticipantResponse {

    // 식별/메타
    private Long participantId;  // 참여자 아이디
    private String nickname;       // 참여자 닉네임
    private String orderName;      // 입금자명

    // 본문
    private String phoneNumber;    // 전화번호
    private String imageKey;       // 프로필 이미지


    // 숫자 데이터
    private int orderQuantity;     // 주문 개수

    // 상태/플래그
    private String orderStatus;    // 주문 상태(PAID, CONFIRMED)

}
