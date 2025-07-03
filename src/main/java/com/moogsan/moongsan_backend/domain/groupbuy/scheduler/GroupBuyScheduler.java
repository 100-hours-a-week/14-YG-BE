package com.moogsan.moongsan_backend.domain.groupbuy.scheduler;

import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService.*;
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
    private final PublishDueApproachingEvents scheduleDueApproachingGroupBuys;
    private final PublishPickupApproachingEvents schedulePickupApproachingGroupBuys;

    // 공구 게시글 dueDate 기반 자동 공구 마감 스케줄러(매 정각(0분)과 30분에 작동)
    @Scheduled(cron = "0 0/30 * * * *")
    public void closeExpiredGroupBuys() {
        LocalDateTime now = LocalDateTime.now();
        closePastDueGroupBuys.closePastDueGroupBuys(now);
    }

    // 공구 게시글 pickupDate 기반 자동 공구 종료 스케줄러 (매 정각(1분)과 31분에 작동)
    @Scheduled(cron = "0 1/31 * * * *")
    public void endPastPickupGroupBuys() {
        LocalDateTime now = LocalDateTime.now();
        endPastPickupGroupBuys.endPastPickupGroupBuys(now);
    }

    // 공구 참여 자동 취소 스케줄러 (매 정각 1분10분)과 40분에 작동)
    @Scheduled(cron = "0 10/40 * * * *")
    public void runAutoCancellation() {
        cancelGroupBuyParticipant.cancelUnconfirmedOrders();
    }

    // 공구 마감 예정 알림 (dueDate 하루 전, 매일 오전 10시 50분)
    @Scheduled(cron = "0 0 10 50 * *")
    public void notifyDueApproachingGroupBuys() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
        scheduleDueApproachingGroupBuys.publishDueApproachingEvents(tomorrow);
    }

    // 픽업 예정 알림 (pickupDate 하루 전, 매일 오전 11시 10분)
    @Scheduled(cron = "0 0 11 10 * *")
    public void notifyPickupApproachingGroupBuys() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
        schedulePickupApproachingGroupBuys.publishPickupApproachingEvents(tomorrow);
    }

}
