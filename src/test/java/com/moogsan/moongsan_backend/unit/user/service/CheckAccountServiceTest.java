package com.moogsan.moongsan_backend.unit.user.service;

import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.exception.code.UserErrorCode;
import com.moogsan.moongsan_backend.domain.user.service.CheckAccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CheckAccountServiceTest {

    private final CheckAccountService checkAccountService = new CheckAccountService();

    @Test
    @DisplayName("실명과 계좌번호가 일치하면 예외 없이 통과")
    void checkBankAccountHolder_success() {
        // given
        String bankCode = "034";
        String bankNum = "613121041573";
        String name = "박건";

        // when & then
        assertThatCode(() ->
                checkAccountService.checkBankAccountHolder(bankCode, bankNum, name)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("실제 계좌명이 일치하지 않으면 예외 발생")
    void checkBankAccountHolder_invalidName() {
        // given
        String bankCode = "034";
        String bankNum = "613121041573";
        String wrongName = "홍길순"; // 틀린 이름

        // when & then
        assertThatThrownBy(() ->
                checkAccountService.checkBankAccountHolder(bankCode, bankNum, wrongName)
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining("계좌주명이 실명과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 계좌번호일 경우 예외 발생")
    void checkBankAccountHolder_invalidAccount() {
        // given
        String bankCode = "011";
        String invalidAccountNum = "00000000000"; // 없는 번호
        String name = "홍길동";

        assertThatThrownBy(() ->
                checkAccountService.checkBankAccountHolder(bankCode, invalidAccountNum, name)
        )
                .isInstanceOf(UserException.class)
                .hasMessageContaining("계좌번호가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("Iamport 서버 오류 시 NOT_FOUND 발생")
    void checkBankAccountHolder_serverError() {
        // given
        String bankCode = "011";
        String bankNum = "12345678901";
        String name = "홍길동";

        // 현재 환경에서는 404 응답 → NOT_FOUND 발생이 실제 시나리오
        assertThatThrownBy(() ->
                checkAccountService.checkBankAccountHolder(bankCode, bankNum, name)
        )
                .isInstanceOf(UserException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.NOT_FOUND);
    }
}