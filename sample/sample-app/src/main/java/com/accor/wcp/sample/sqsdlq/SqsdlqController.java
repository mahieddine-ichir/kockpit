package com.accor.wcp.sample.sqsdlq;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Component
@RequiredArgsConstructor
public class SqsdlqController {

    private final SqsdlqService sqsdlqService;

    @GetMapping(value = "/sqsdlq/errorMessage")
    public ResponseEntity<String> sqsdlqSamples() {
        sqsdlqService.sendToSqsdlq();
        return ResponseEntity.ok("Message sent to sqs");
    }

    @GetMapping(value = "/sqsdlq/fifo/errorMessage")
    public ResponseEntity<String> sqsdlqSamplesFifo() {
        sqsdlqService.sendToSqsdlqFifo();
        return ResponseEntity.ok("Message sent to samples fifo sqs");
    }

    @GetMapping(value = "/sqsdlq/binary/errorMessage")
    public ResponseEntity<String> sqsdlqBinaryAttributeSamples() {
        sqsdlqService.sendToSqsdlqWithBinary();
        return ResponseEntity.ok("Message sent (with binary attribute) to sqs");
    }
}
