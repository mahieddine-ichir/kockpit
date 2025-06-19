package org.kockpit.audit.backend.controllers;

import org.kockpit.audit.backend.DTOs.AuditReportSummary;
import org.kockpit.audit.backend.DTOs.HttpRequestSummary;
import org.kockpit.audit.backend.DataModel.HttpExchangeAudit;
import org.kockpit.audit.backend.DataModel.SearchAuditReport;
import org.kockpit.audit.backend.services.AuditReportService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/audit-reports")
public class AuditReportController {
    private final AuditReportService service;

    public AuditReportController(AuditReportService service) {
        this.service = service;
    }

    @GetMapping
    public List<SearchAuditReport> getReports() {
        return service.getAll();
    }

    // return ID,domain, env of the audits (maybe I will use it in the right navbar)
    @GetMapping("/full")
    public List<AuditReportSummary> listSummaries() throws IOException {
        return service.getReportsSummaries();
    }

    // Fetch full audit report for detail sidebar
    @GetMapping("/{id}")
    public SearchAuditReport getReport(@PathVariable String id) {
        return service.getById(id);
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
