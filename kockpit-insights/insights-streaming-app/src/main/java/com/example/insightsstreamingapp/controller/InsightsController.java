package com.example.insightsstreamingapp.controller;

import com.example.insightsstreamingapp.service.InsightsService;
import com.example.insightsstreamingapp.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/insights/dashboard")
@CrossOrigin(origins = "*")
@Slf4j
public class InsightsController {

    private final InsightsService insightsService;

    @Autowired
    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String env) {
        DashboardSummaryDto summary = insightsService.getDashboardSummary(from, to, domain, env);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/charts/status-distribution")
    public ResponseEntity<List<PieChartDataDto>> getStatusDistribution(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String env) {
        List<PieChartDataDto> data = insightsService.getStatusDistribution(from, to, domain, env);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/charts/method-distribution")
    public ResponseEntity<List<PieChartDataDto>> getMethodDistribution(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String env) {
        List<PieChartDataDto> data = insightsService.getMethodDistribution(from, to, domain, env);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/charts/domain-env-distribution")
    public ResponseEntity<List<PieChartDataDto>> getDomainEnvDistribution(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String env) {
        List<PieChartDataDto> data = insightsService.getDomainEnvDistribution(from, to, domain, env);
        return ResponseEntity.ok(data);
    }



    @GetMapping("/filters")
    public ResponseEntity<FiltersDto> getAvailableFilters() {
        FiltersDto filters = insightsService.getAvailableFilters();
        return ResponseEntity.ok(filters);
    }



}