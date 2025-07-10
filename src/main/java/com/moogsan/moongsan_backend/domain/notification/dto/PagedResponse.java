package com.moogsan.moongsan_backend.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PagedResponse<T> {

    private List<T> items;
    private Long nextCursor;
    private boolean hasNext;
}
