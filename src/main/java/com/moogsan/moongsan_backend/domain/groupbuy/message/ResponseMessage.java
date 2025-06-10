package com.moogsan.moongsan_backend.domain.groupbuy.message;

public final class ResponseMessage {

    private ResponseMessage() {}

    /// SUCCESS

    public static final String CREATE_SUCCESS =
            "공구 게시글이 성공적으로 업로드되었습니다.";

    public static final String UPDATE_SUCCESS =
            "공구 게시글이 성공적으로 수정되었습니다.";

    public static final String LEAVE_SUCCESS =
            "공구 참여가 성공적으로 취소되었습니다.";

    public static final String GENERATE_SUCCESS =
            "상품 상세 설명이 성공적으로 생성되었습니다.";

    public static final String END_SUCCESS =
            "공구 게시글이 성공적으로 종료되었습니다.";

    public static final String DELETE_SUCCESS =
            "공구 게시글이 성공적으로 삭제되었습니다.";

    public static final String GET_DETAIL_SUCCESS =
            "공구 게시글 상세 정보를 성공적으로 조회했습니다.";

    public static final String GET_UPDATE_SUCCESS =
            "공구 게시글 수정용 정보를 성공적으로 조회했습니다.";

    public static final String GET_ACCOUNT_SUCCESS =
            "공구 게시글 주최자 계좌 정보를 성공적으로 조회했습니다.";

    public static final String GET_HOSTED_SUCCESS =
            "주최 공구 리스트를 성공적으로 조회했습니다.";

    public static final String GET_PARTICIPATED_SUCCESS =
            "참여 공구 리스트를 성공적으로 조회했습니다.";

    public static final String GET_WISH_SUCCESS =
            "관심 공구 리스트를 성공적으로 조회했습니다.";

    public static final String GET_LIST_SUCCESS =
            "전체 공구 리스트를 성공적으로 조회했습니다.";

    public static final String GET_PARTICIPANTS_SUCCESS =
            "공구 참여자 리스트를 성공적으로 조회했습니다.";


    ///  FAIL

    public static final String NOT_DIVISOR =
            "상품 주문 단위는 상품 전체 수량의 약수여야 합니다.";

    public static final String NOT_OPEN =
            "공구가 열려있는 상태에서만 요청 가능합니다.";

    public static final String NOT_EXIST =
            "존재하지 않는 공구입니다.";

    public static final String AFTER_DELETED =
            "삭제된 공구입니다.";

    public static final String NOT_EXIST_ORDER =
            "존재하지 않는 주문입니다.";

    public static final String NOT_HOST =
            "공구의 주최자만 요청 가능합니다.";

    public static final String NOT_PARTICIPANT =
            "공구의 참여자만 요청 가능합니다.";

    public static final String EXIST_PARTICIPANT =
            "참여자가 1명 이상일 경우 공구를 삭제할 수 없습니다.";

    public static final String BEFORE_CLOSED =
            "모집 마감 이후에만 요청 가능합니다.";

    public static final String AFTER_ENDED =
            "이미 종료된 공구입니다.";

    public static final String BEFORE_PICKUP_DATE =
            "공구 종료는 공구 픽업 일자 이후에만 가능합니다.";

    public static final String BEFORE_FIXED =
            "공구 종료는 공구 체결 이후에만 가능합니다.";

    public static final String NOT_EXIST_CATEGORY =
            "존재하지 않는 카테고리입니다.";

    public static final String BAD_REQUEST =
            "입력 형식이 올바르지 않습니다.";
}
