package com.moogsan.moongsan_backend.groupbuy.dto.command.response;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private String message;
    private T data;
}

