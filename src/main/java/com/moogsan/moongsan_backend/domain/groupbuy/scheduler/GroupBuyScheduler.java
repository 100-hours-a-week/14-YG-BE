package com.moogsan.moongsan_backend.domain.groupbuy.scheduler;

import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.CancelGroupBuyParticipant;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.ClosePastDueGroupBuys;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.EndPastPickupGroupBuys;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class GroupBuyScheduler {

    private final ClosePastDueGroupBuys closePastDueGroupBuys;
    private final EndPastPickupGroupBuys endPastPickupGroupBuys;
    private final CancelGroupBuyParticipant cancelGroupBuyParticipant;

    // 공구 게시글 dueDate 기반 자동 공구 마감 스케줄러(매 정각(0분)과 30분에 작동)
    @Scheduled(cron = "0 0/30 * * * *")
    public void closeExpiredGroupBuys() {
        LocalDateTime now = LocalDateTime.now();
        closePastDueGroupBuys.closePastDueGroupBuys(now);
    }

    // 공구 게시글 pickupDate 기반 자동 공구 종료 스케줄러 (매 정각(0분)과 31분에 작동)
    @Scheduled(cron = "0 0/31 * * * *")
    public void endPastPickupGroupBuys() {
        LocalDateTime now = LocalDateTime.now();
        endPastPickupGroupBuys.endPastPickupGroupBuys(now);
    }

    // 공구 참여 자동 취소 스케줄러 (매 정각 1분10분)과 40분에 작동)
    @Scheduled(cron = "0 1/40 * * * *")
    public void runAutoCancellation() {
        cancelGroupBuyParticipant.cancelUnconfirmedOrders();
    }

}
