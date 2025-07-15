package com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyList.ParticipantList;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ParticipantListResponse {

    private List<ParticipantResponse> participants; // 참여자 리스트

}
