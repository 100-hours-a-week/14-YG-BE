package com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchGroupBuyRequest {
    @NotBlank(message="검색 키워드는 필수 입력 항목입니다.")
    @Size(min = 1, max = 30, message = "검색 키워드는 1자 이상, 30자 이하로 입력해주세요.")
    private String keyword;
}
