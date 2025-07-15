package com.moogsan.moongsan_backend.domain.notification.message;

public final class ResponseMessage {

    private ResponseMessage() {
    }

    /// SUCCESS

    public static final String GET_PAST_NOTIFICATION_SUCCESS =
            "과거 알람을 성공적으로 조회했습니다.";

    /// FAIL
    public static final String NOTIFICATION_NOT_FOUND =
            "존재하지 않는 알림입니다.";
}
