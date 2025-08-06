package com.accor.wcp.sample.audit;

import com.accor.wcp.audit.AuditedDelegateExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {
    @Value("${async.executor.corePoolSize:10}")
    private int corePoolSize;

    @Value("${async.executor.maxPoolSize:50}")
    private int maxPoolSize;

    @Value("${async.executor.queueCapacity:50}")
    private int queueCapacity;

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("Async Executor -");
        executor.initialize();
        return new AuditedDelegateExecutor(executor);
    }
}
