package org.kockpit.backend.services.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardApi {


    private final DashboardService dashboardService;

    @GetMapping("app_details")
    Map<String, List<Object>> appDetails() {
        return dashboardService.appDetails();
    }

    @GetMapping("app_distribution_data")
    List<Map<String, Object>> appDistributionData() {
        return dashboardService.avgDurationByApp();
    }

    @GetMapping("status_distribution_by_appId")
    List<Map<String, Object>> statusDistributionByAppId(
            @RequestParam(required = false, defaultValue = "now-1d") String gte
    ) {
        return dashboardService.statusDistributionByAppId(gte);
    }

    @GetMapping("overTime_by_appId")
    List<Map<String, Object>> overTimeByAppId(
            @RequestParam(required = false, defaultValue = "now-1d") String gte
    ) {
        return dashboardService.overTimeByAppId(gte);
    }
}
