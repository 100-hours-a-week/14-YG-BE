package com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserAccountResponse {

    private String name;             // 실명
    private String accountBank;      // 주최자 계좌 은행
    private String accountNumber;    // 주최자 계좌 번호
    
}
