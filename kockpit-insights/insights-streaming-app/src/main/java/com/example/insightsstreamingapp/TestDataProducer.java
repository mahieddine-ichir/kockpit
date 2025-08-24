package com.example.insightsstreamingapp;

import org.kockpit.audit.stream.api.AuditReport;
import org.kockpit.audit.stream.api.IndexedKeyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
class TestDataProducer implements CommandLineRunner {

    @Autowired
    private KafkaTemplate<String, AuditReport> kafkaTemplate;

    private final List<String> domains = Arrays.asList("rcu", "local");
    private final List<String> envs = Arrays.asList("local", "rec");
    private final List<String> methods = Arrays.asList("GET", "POST", "PUT", "DELETE");
    private final List<String> statuses = Arrays.asList("200", "201", "400", "500");

    @Override
    public void run(String... args) {
        new Thread(() -> {
            while (true) {
                AuditReport report = new AuditReport();
                report.setId(UUID.randomUUID().toString());
                report.setDomain(domains.get((int) (Math.random() * domains.size())));
                report.setEnv(envs.get((int) (Math.random() * envs.size())));
                report.setStart(Instant.now());

                String method = methods.get((int) (Math.random() * methods.size()));
                String status = statuses.get((int) (Math.random() * statuses.size()));
                int duration = 50 + (int) (Math.random() * 200);

                IndexedKeyValue uriKv = new IndexedKeyValue();
                uriKv.setKey("requestUri");
                uriKv.setValue("/api/v1/" + report.getDomain());

                IndexedKeyValue methodKv = new IndexedKeyValue();
                methodKv.setKey("httpMethod");
                methodKv.setValue(method);

                IndexedKeyValue statusKv = new IndexedKeyValue();
                statusKv.setKey("httpStatus");
                statusKv.setValue(status);
                statusKv.setValueInteger(Integer.parseInt(status));

                IndexedKeyValue durationKv = new IndexedKeyValue();
                durationKv.setKey("duration");
                durationKv.setValue(String.valueOf(duration));
                durationKv.setValueInteger(duration);

                report.setIndexedKeyValues(Arrays.asList(
                        uriKv, methodKv, statusKv, durationKv
                ));

                kafkaTemplate.send("audit", report);
                System.out.println("Sent test audit: " + report.getId() +
                        " | Domain: " + report.getDomain() +
                        " | Env: " + report.getEnv() +
                        " | Status: " + status);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
}