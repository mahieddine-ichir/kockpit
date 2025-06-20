package org.kockpit.audit.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.kockpit.audit.backend.model.AuditReportSummary;
import org.kockpit.audit.backend.model.HttpExchangeAudit;
import org.kockpit.audit.backend.model.HttpRequestSummary;
import org.kockpit.audit.backend.services.AuditReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-reports")
@RequiredArgsConstructor
public class AuditReportController {

    private final AuditReportService service;

    // return ID,domain, env of the audits (maybe I will use it in the right navbar)
    @GetMapping
    public List<AuditReportSummary> listSummaries() {
        return service.getReportsSummaries();
    }

    //Get all HTTP requests in the selected audit report (for table )
    @GetMapping("/{id}/requests")
    public List<HttpRequestSummary> getHttpRequests(@PathVariable String id) {
        return service.getHttpRequests(id);
    }

    //Get full detail of the selected HTTP request
    @GetMapping("/{id}/requests/{traceId}")
    public HttpExchangeAudit getHttpRequestDetails(
            @PathVariable String id,
            @PathVariable String traceId
    ) {
        return service.getHttpRequestDetails(id, traceId);
    }


}
