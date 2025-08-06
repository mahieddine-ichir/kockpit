package com.accor.wcp.sample.audit.sqs;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@Component
@RequiredArgsConstructor
public class AuditSqsController {

    private final AuditSqsService auditSqsService;

    @GetMapping(value = "/audit/sqs/messages/{times}")
    public ResponseEntity<List<String>> auditSqsSamples(@PathVariable("times") int times) {
        return ResponseEntity.ok(auditSqsService.sendToSqs(times));
    }
}
