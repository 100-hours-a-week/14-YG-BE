package com.moogsan.moongsan_backend.global.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.function.LongSupplier;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class RealtimeAfterCommit {
    private final RealtimeBroadcaster broadcaster;

    /** 버전/페이로드를 lazy하게 만들고 커밋 후 발행 */
    public <T> void publish(
            String topic,
            String type,
            LongSupplier versionSupplier,
            Supplier<T> payloadSupplier
    ) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                long v = versionSupplier.getAsLong();
                T payload = payloadSupplier.get();
                broadcaster.publish(topic, type, v, payload);
            }
        });
    }

    /** 이미 계산된 값으로 바로 등록 (간단 케이스) */
    public <T> void publish(String topic, String type, long version, T payload) {
        publish(topic, type, () -> version, () -> payload);
    }
}
