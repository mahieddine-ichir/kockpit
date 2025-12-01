package org.kockpit.backend.services.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/{domain}/{env}/dashboard")
@RequiredArgsConstructor
public class DashboardApi {


    private final DashboardService dashboardService;

    @GetMapping("app_details")
    Map<String, List<Object>> appDetails(
            @PathVariable String domain,
            @PathVariable String env
    ) {
        return dashboardService.appDetails(domain, env);
    }

    @GetMapping("app_distribution_data")
    List<Map<String, Object>> appDistributionData(
            @PathVariable String domain,
            @PathVariable String env,
            @RequestParam(required = false, defaultValue = "now-1d") String gte
            ) {
        return dashboardService.avgDurationByApp(domain, env, gte);
    }

    @GetMapping("status_distribution_by_appId")
    List<Map<String, Object>> statusDistributionByAppId(
            @PathVariable String domain,
            @PathVariable String env,
            @RequestParam(required = false, defaultValue = "now-1d") String gte
    ) {
        return dashboardService.statusDistributionByAppId(domain, env, gte);
    }

    @GetMapping("overTime_by_appId")
    List<Map<String, Object>> overTimeByAppId(
            @PathVariable String domain,
            @PathVariable String env,
            @RequestParam(required = false, defaultValue = "now-1d") String gte
    ) {
        return dashboardService.overTimeByAppId(domain, env, gte);
    }
}
