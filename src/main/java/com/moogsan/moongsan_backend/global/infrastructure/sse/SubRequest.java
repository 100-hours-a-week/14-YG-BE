package com.moogsan.moongsan_backend.global.infrastructure.sse;

import lombok.Data;

import java.util.Set;

@Data
public class SubRequest {
    private String connectionId;   // null이면 해당 유저의 모든 탭
    private Set<String> add;       // 예: ["groupbuy:123"]
    private Set<String> remove;    // 예: ["groupbuy:456"]
}
