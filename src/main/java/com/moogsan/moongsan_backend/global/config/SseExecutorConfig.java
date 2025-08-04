package com.moogsan.moongsan_backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SseExecutorConfig {

    @Bean
    public TaskExecutor sseTaskExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setThreadNamePrefix("sse-send");
        ex.setCorePoolSize(Math.max(4, Runtime.getRuntime().availableProcessors()));
        ex.setMaxPoolSize(ex.getCorePoolSize());
        ex.setQueueCapacity(10000);
        ex.initialize();
        return ex;
    }
}
