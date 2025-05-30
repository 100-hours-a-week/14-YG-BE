package com.moogsan.moongsan_backend.domain.user.mapper;

import java.util.HashMap;
import java.util.Map;

public class BankCodeMapper {

    private static final Map<String, String> bankCodeMap = new HashMap<>();

    static {
        bankCodeMap.put("KB국민은행", "004");
        bankCodeMap.put("SC제일은행", "023");
        bankCodeMap.put("경남은행", "039");
        bankCodeMap.put("광주은행", "034");
        bankCodeMap.put("기업은행", "003");
        bankCodeMap.put("농협", "011");
        bankCodeMap.put("대구은행", "031");
        bankCodeMap.put("부산은행", "032");
        bankCodeMap.put("산업은행", "002");
        bankCodeMap.put("수협", "007");
        bankCodeMap.put("신한은행", "088");
        bankCodeMap.put("신협", "048");
        bankCodeMap.put("외환은행", "005");
        bankCodeMap.put("우리은행", "020");
        bankCodeMap.put("우체국", "071");
        bankCodeMap.put("전북은행", "037");
        bankCodeMap.put("제주은행", "035");
        bankCodeMap.put("축협", "012");
        bankCodeMap.put("하나은행", "081");
        bankCodeMap.put("한국씨티은행", "027");
        bankCodeMap.put("K뱅크", "089");
        bankCodeMap.put("카카오뱅크", "090");
    }

    public static String getBankCode(String bankName) {
        return bankCodeMap.getOrDefault(bankName, null); // 못 찾으면 null 반환
    }
}