package com.accor.wcp.console.services.audit.console.backend.search.dashboard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services/dashboard")
@CrossOrigin(origins = "*", originPatterns = "*")
@RequiredArgsConstructor
@Slf4j
public class DashboardApi {

    private final DashboardService dashboardService;

    @GetMapping
    DashboardPage byKeyValue(
            @RequestParam("key") String key, @RequestParam("value") String value,
            @RequestParam(value = "from", defaultValue = "0") Integer from,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {
        return dashboardService.byKeyValue(key, value, from, size);
    }
}
