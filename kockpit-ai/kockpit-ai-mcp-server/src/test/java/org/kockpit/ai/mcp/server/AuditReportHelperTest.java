package org.kockpit.ai.mcp.server;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kockpit.ai.mcp.server.dto.AuditReportDocument;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
class AuditReportHelperTest {

    @Test
    void load_audit() throws IOException {
        String hitAsSource = new String(this.getClass().getResourceAsStream("/audit_1.json").readAllBytes());
        AuditReportDocument auditReportDocument = AuditReportHelper
                .convertFromString(hitAsSource);

        Assertions.assertNotNull(auditReportDocument);
        Assertions.assertEquals("example", auditReportDocument.getDomain());
        Assertions.assertEquals("dev", auditReportDocument.getEnv());
        Assertions.assertEquals("example-app", auditReportDocument.getAppId());
        Assertions.assertEquals("1.23-SNAPSHOT", auditReportDocument.getVersion());
        Assertions.assertEquals("example-app", auditReportDocument.getArtifact());
        Assertions.assertEquals("00000000-0000-0000-0000-000000000001", auditReportDocument.getRequestId());
        Assertions.assertEquals(Instant.parse("2026-03-04T09:23:48.785092571Z"), auditReportDocument.getStart());
        Assertions.assertEquals(Instant.parse("2026-03-04T09:24:06.105071103Z"), auditReportDocument.getEnd());
        Assertions.assertEquals("example-host.internal", auditReportDocument.getHostname());
        Assertions.assertNotNull(auditReportDocument.getCompressedOriginalJsonValue());
        Assertions.assertFalse(auditReportDocument.getCompressedOriginalJsonValue().isEmpty());

        List<Map<String, Object>> indexedExtensions = auditReportDocument.getIndexedExtensions();
        Assertions.assertNotNull(indexedExtensions);
        Assertions.assertEquals(1, indexedExtensions.size());

        List<?> indexedKeyValues = (List<?>) indexedExtensions.get(0).get("indexedKeyValues");
        Assertions.assertNotNull(indexedKeyValues);
        Assertions.assertEquals(7, indexedKeyValues.size());
    }

    @Test
    void decompress_originalJson() throws IOException {
        String hitAsSource = new String(this.getClass().getResourceAsStream("/audit_1.json").readAllBytes());
        AuditReportDocument auditReportDocument = AuditReportHelper.convertFromString(hitAsSource);

        String originalJson = AuditReportHelper.decompressOriginalJson(auditReportDocument.getCompressedOriginalJsonValue());

        Assertions.assertNotNull(originalJson);
        Assertions.assertFalse(originalJson.isEmpty());
        Assertions.assertTrue(originalJson.startsWith("{") || originalJson.startsWith("["),
                "Decompressed value should be a JSON object or array");

        log.info(originalJson);
    }

    @Test
    void extract_kengineFlows_audits() throws IOException {
        String hitAsSource = new String(this.getClass().getResourceAsStream("/audit_1.json").readAllBytes());
        AuditReportDocument auditReportDocument = AuditReportHelper.convertFromString(hitAsSource);
        String originalJson = AuditReportHelper.decompressOriginalJson(auditReportDocument.getCompressedOriginalJsonValue());

        List<Map<String, Object>> kengineFlows = AuditReportHelper.extractKengineFlowsAudits(originalJson);

        Assertions.assertNotNull(kengineFlows);
        Assertions.assertFalse(kengineFlows.isEmpty());
        kengineFlows.forEach(audit -> Assertions.assertEquals("kengine.flows", audit.get("type")));
    }

    @Test
    void extract_builtinHttpExchanges_audits() throws IOException {
        String hitAsSource = new String(this.getClass().getResourceAsStream("/audit_1.json").readAllBytes());
        AuditReportDocument auditReportDocument = AuditReportHelper.convertFromString(hitAsSource);
        String originalJson = AuditReportHelper.decompressOriginalJson(auditReportDocument.getCompressedOriginalJsonValue());

        List<Map<String, Object>> httpExchanges = AuditReportHelper.extractBuiltinHttpExchangesAudits(originalJson);

        Assertions.assertNotNull(httpExchanges);
        Assertions.assertEquals(1, httpExchanges.size());

        Map<String, Object> audit = httpExchanges.get(0);
        Assertions.assertEquals("builtin.httpexchanges", audit.get("type"));

        List<?> events = (List<?>) audit.get("events");
        Assertions.assertNotNull(events);
        Assertions.assertEquals(20, events.size());

        Map<?, ?> firstEvent = (Map<?, ?>) events.get(0);
        Assertions.assertTrue(firstEvent.containsKey("startTime"));
        Assertions.assertTrue(firstEvent.containsKey("endTime"));
        Assertions.assertTrue(firstEvent.containsKey("httpAuditedRequest"));
        Assertions.assertTrue(firstEvent.containsKey("httpAuditedResponse"));
    }

    @Test
    void extract_audits_unknownType_returnsEmpty() throws IOException {
        String hitAsSource = new String(this.getClass().getResourceAsStream("/audit_1.json").readAllBytes());
        AuditReportDocument auditReportDocument = AuditReportHelper.convertFromString(hitAsSource);
        String originalJson = AuditReportHelper.decompressOriginalJson(auditReportDocument.getCompressedOriginalJsonValue());

        List<Map<String, Object>> result = AuditReportHelper.extractAuditsByType(originalJson, "unknown.type");

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }
}
