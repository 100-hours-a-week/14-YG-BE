package com.moogsan.moongsan_backend.domain.chatting.dto.query;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChatRoomPagedResponse {

    private List<ChatRoomResponse> chatRooms;                // 채팅 게시글 리스트
    private LocalDateTime nextCursorJoinedAt;           // 다음 페이지용 postId
    private boolean hasMore;              // 다음 페이지 존재 여부

}
