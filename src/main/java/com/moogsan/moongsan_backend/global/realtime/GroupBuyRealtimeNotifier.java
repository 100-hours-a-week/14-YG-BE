package com.moogsan.moongsan_backend.global.realtime;

import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.domain.event.GroupBuyUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupBuyRealtimeNotifier {
    private static final String TYPE = "GROUPBUY_UPDATED";
    private static final String TOPIC_PREFIX = "groupbuy:";

    private final RealtimeAfterCommit afterCommit;
    private final RealtimeBroadcaster broadcaster;

    /** 주문/변경 반영 직후 호출: 커밋되면 sold/left를 SSE로 즉시 브로드캐스트 */
    public void publishUpdated(GroupBuy gb) {
        // 여기서 값들을 미리 캡처(프리미티브/불변) → afterCommit에서 그대로 사용
        final long groupBuyId = gb.getId();
        final int left = gb.getLeftAmount();
        final int total = gb.getTotalAmount();
        final int sold = total - left;
        final String title = gb.getTitle();

        afterCommit.publish(
                TOPIC_PREFIX + groupBuyId,
                TYPE,
                System::currentTimeMillis,
                () -> GroupBuyRealtimePayload.builder()
                        .groupBuyId(groupBuyId)
                        .soldAmount(sold)
                        .leftAmount(left)
                        .build()
        );
    }

    /** Kafka 리스너 등 트랜잭션 밖: rich 이벤트를 즉시 브로드캐스트 */
    public void publishUpdated(GroupBuyUpdatedEvent e) {
        long version = System.currentTimeMillis();

        var payload = GroupBuyRealtimePayload.builder()
                .groupBuyId(e.getGroupBuyId())
                .soldAmount(e.getSoldAmount())
                .leftAmount(e.getLeftAmount())
                .build();

        broadcaster.publish(
                TOPIC_PREFIX + e.getGroupBuyId(),
                TYPE,
                version,
                payload
        );
    }
}
