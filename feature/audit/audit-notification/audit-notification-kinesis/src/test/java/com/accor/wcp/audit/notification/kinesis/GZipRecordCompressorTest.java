package com.accor.wcp.audit.notification.kinesis;

import com.accor.wcp.audit.AuditReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
class GZipRecordCompressorTest {

    GZipRecordCompressor compressor = new GZipRecordCompressor();

    @Test
    void compress_big_file() throws IOException {

        byte[] bytes = this.getClass().getResourceAsStream("/irma/irma-response1.json").readAllBytes();

        AuditReport.AuditJsonReport auditReport = new MockAudit(AuditReport.builder().build(), new String(bytes));

        Runtime runtime = Runtime.getRuntime();

        long currentTimeMillis = System.currentTimeMillis();
        long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();

        PutRecordsRequestEntry compressed = compressor.apply(auditReport);
        // working code here
        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
        log.info("Memory increased: {} Ko, computed in {} ms", (usedMemoryAfter - usedMemoryBefore) / 1024, System.currentTimeMillis() - currentTimeMillis);
    }

    class MockAudit extends AuditReport.AuditJsonReport {

        private final String json;

        MockAudit(AuditReport auditReport, String json) {
            super(auditReport, new ArrayList<>());
            this.json = json;
        }

        @Override
        public String getAuditJson() {
            return json;
        }
    }
}