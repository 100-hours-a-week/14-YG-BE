package com.moogsan.moongsan_backend.domain.chatting.participant.dto.query;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChattingListResponse {

    // 식별/메타
    private Long postId;               // 공구 게시글 아이디
    private String title;              // 공구 게시글 제목
    private String postStatus;         // 공구 진행 상태(OPEN, CLOSED, ENDED)

    // 본문
    private String thumbnailImageKey;  // 공구 게시글 대표 사진
    private String location;           // 거래 장소
    private Long lastMessageId;        // 가장 최근 채팅 메세지 아이디
    private String lasMessageContent;  // 가장 최근 채팅 메세지 내용

    // 숫자 데이터
    private int soldAmount;            // 판매 수량(totalAmount - leftAmount)
    private int totalAmount;           // 전체 상품 수량
    private int participantCount;      // 참여 인원 수
}
