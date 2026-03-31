package org.kockpit.kinesis.s3;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.api.AuditConsumerEvent;
import org.kockpit.audit.stream.api.model.AuditReport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.CollectionUtils;

import java.util.List;

@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class KinesisToS3Application implements ApplicationListener<AuditConsumerEvent> {

    private final List<AuditConsumer> consumerList;

    public static void main(String[] args) {
        SpringApplication.run(KinesisToS3Application.class, args);
    }

    @PostConstruct
    void init() {
        if (CollectionUtils.isEmpty(consumerList)) {
            log.error("No AuditConsumer registered!");
            throw new RuntimeException("No AuditConsumer registered!");
        }
        consumerList.forEach(c -> log.info("➡️ Starting consumer {}", c.getClass().getSimpleName()));
    }

    @Override
    public void onApplicationEvent(AuditConsumerEvent event) {
        consumerList.forEach(consumer -> consumer.accept((AuditReport) event.getSource()));
    }

    @Override
    public boolean supportsAsyncExecution() {
        return false;
    }
}