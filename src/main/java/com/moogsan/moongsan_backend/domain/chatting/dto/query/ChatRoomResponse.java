package com.moogsan.moongsan_backend.domain.chatting.dto.query;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomListResponse {

    // 식별
    private Long chatRoomId;            // 채팅방 아이디

    // 본문
    private String title;               // 공구 게시글 제목
    private String location;            // 공구 게시글 거래 장소
    private String lastMessageId;       // 마지막 메세지 아이디
    private String lastMessageContent;  // 마지막 메세지 내용

    // 숫자 데이터
    private int soldAmount;             // 판매 수량(totalAmount - leftAmount)
    private int totalAmount;            // 전체 상품 수량
    private int participantCount;       // 참여 인원 수

}
