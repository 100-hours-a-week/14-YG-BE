package com.moogsan.moongsan_backend.global.infrastructure.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SseSender {

    private final TaskExecutor sseTaskExecutor;

    public void sendAsync(SseEmitter emitter, String name, Object data) {
        sseTaskExecutor.execute(() -> {
            try {
                emitter.send(name == null
                ? SseEmitter.event().data(data)
                : SseEmitter.event().name(name).data(data));
            } catch (IOException | IllegalStateException ex) {
                try {
                    emitter.completeWithError(ex);
                } catch (Exception exception){}
            }
        });
    }
}
