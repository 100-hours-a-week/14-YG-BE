package com.moogsan.moongsan_backend.global.realtime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GroupBuyRealtimePayload {
    Long groupBuyId;
    Integer soldAmount;
    Integer leftAmount;
}
