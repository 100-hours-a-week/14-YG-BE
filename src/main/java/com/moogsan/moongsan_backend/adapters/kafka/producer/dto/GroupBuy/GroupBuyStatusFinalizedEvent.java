package com.moogsan.moongsan_backend.adapters.kafka.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 토픽: groupbuy.status.finalized
 * 설명: 공구 체결 알림용 이벤트
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GroupBuyStatusFinalizedEvent extends BaseEvent{
    private Long groupBuyId;            // 공구 게시글 아이디
    private Long hostId;                // 공구 주최자 아아디
    private List<Long> participantIds;  // 공구 참여자 아이디 리스트
    private String groupBuyTitle;       // 공구 게시글 제목
    private String participantCount;    // 공구 참여자 수
    private String totalQty;                // 전체 상품 수
}
