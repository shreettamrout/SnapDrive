package com.shreettam.mini_google_drive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5);           // Minimum threads
        executor.setMaxPoolSize(10);           // Maximum threads
        executor.setQueueCapacity(100);        // Queue before spawning extra threads
        executor.setThreadNamePrefix("Async-"); // Thread name prefix
        executor.initialize();

        return executor;
    }
}
