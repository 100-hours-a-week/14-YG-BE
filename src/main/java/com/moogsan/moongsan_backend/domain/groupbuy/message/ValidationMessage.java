package com.moogsan.moongsan_backend.domain.groupbuy.message;

public class ValidationMessage {
    public static final String NOT_BLANK = "공백만으로는 입력할 수 없습니다.";
    public static final String TITLE_SIZE = "제목은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요.";
    public static final String NAME_SIZE = "상품명은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요.";
    public static final String URL_SIZE = "URL은 1자 이상, 2000자 이하로 입력해주세요.";
    public static final String INVALID_URL = "URL 형식이 올바르지 않습니다.";
    public static final String BLANK_PRICE = "상품 가격은 필수 입력 항목입니다.";
    public static final String PRICE_SIZE = "상품 가격은 1 이상이어야 합니다.";
    public static final String BLANK_TOTAL_AMOUNT = "상품 전체 수량은 필수 입력 항목입니다.";
    public static final String TOTAL_AMOUNT_SIZE = "상품 전체 수량은 1 이상이어야 합니다.";
    public static final String BLANK_UNIT_AMOUNT = "상품 주문 단위는 필수 입력 항목입니다.";
    public static final String UNIT_AMOUNT_SIZE = "상품 주문 단위는 1 이상이어야 합니다.";
    public static final String BLANK_HOST_QUANTITY = "주최자 주문 수량은 필수 입력 항목입니다.";
    public static final String HOST_QUANTITY_SIZE = "주최자 주문 수량은 0 이상이어야 합니다.";
    public static final String DESCRIPTION_SIZE = "상품 설명은 공백을 제외한 2자 이상, 2000자 이하로 입력해주세요.";
    public static final String INVALID_DUEDATE = "마감 일자는 현재 시간 이후여야 합니다.";
    public static final String LOCATION_SIZE = "거래 장소는 공백을 제외한 2자 이상, 85자 이하로 입력해주세요.";
    public static final String INVALID_PICKUPDATE = "픽업 일자는 현재 시간 이후여야 합니다.";
    public static final String INVALID_IMAGE = "tmp/로 시작하는 이미지를 1장 이상, 5장 이하로 등록해주세요.";
    public static final String INVALID_UPDATE_IMAGE = "tmp/ 혹은 group-buys/로 시작하는 이미지를 1장 이상, 5장 이하로 등록해주세요.";
    public static final String BLANK_DATEMODIFICATION_REASON = "픽업 일자가 변경된 경우 사유를 2자 이상, 85자 이하로 작성해야 합니다.";
}
