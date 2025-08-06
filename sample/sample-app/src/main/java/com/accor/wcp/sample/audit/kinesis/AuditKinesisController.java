package com.accor.wcp.sample.audit.kinesis;

import com.accor.wcp.aws.kinesis.producer.AbstractKinesisProducer;
import java.util.List;
import java.io.IOException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Component
@RequiredArgsConstructor
public class AuditKinesisController extends AbstractKinesisProducer {

    private final AuditKinesisProducerService auditKinesisProducerService;

    @GetMapping(value = "/audit/kinesis/messages/{times}")
    public ResponseEntity<List<String>> auditKinesisSamples(@PathVariable("times") int times) {
        return ResponseEntity.ok(auditKinesisProducerService.sendToKinesis(times));
    }

    @GetMapping(value = "/audit/kinesis/big-report")
    public ResponseEntity<String> auditKinesisBigReport() throws IOException {
        return ResponseEntity.ok(auditKinesisProducerService.sendBigReportToKinesis());
    }
}
