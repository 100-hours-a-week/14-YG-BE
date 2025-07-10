package com.moogsan.moongsan_backend.domain.chatting.participant.message;

public class ResponseMessage {

    /// SUCCESS

    public static final String CREATE_SUCCESS =
            "공구 게시글이 성공적으로 업로드되었습니다.";


    ///  FAIL

    public static final String CHAT_ROOM_NOT_FOUND =
            "존재하지 않는 채팅방입니다.";

    public static final String DELETED_CHAT_ROOM =
            "삭제된 채팅방에는 메세지를 보낼 수 없습니다.";

    public static final String NOT_PARTICIPANT =
            "채팅방의 참여자만 요청 가능합니다.";

    public static final String ALREADEY_JOINED =
            "이미 참여한 채팅방입니다.";

    public static final String CHAT_ROOM_INVALID_STATE =
            "채팅방 상태가 유효하지 않아 요청을 처리할 수 없습니다.";

    public static final String ORDER_NOT_FOUND =
            "공구의 참여자만 채팅방에 참가할 수 있습니다.";

}
