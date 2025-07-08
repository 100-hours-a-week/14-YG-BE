package com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PagedResponse<T> {

    private long count;                   // 공구 게시글 개수
    private List<T> posts;                // 공구 게시글 리스트

    private Integer nextCursor;           // 다음 페이지용 postId
    private Integer nextCursorPrice;      // 다음 페이지용 unitPrice
    private LocalDateTime nextCreatedAt;  // 다음 페이지용 createdAt
    private Integer nextSoldRatio;        // 다음 페이지용 soldRatio

    private boolean hasMore;              // 다음 페이지 존재 여부

}
