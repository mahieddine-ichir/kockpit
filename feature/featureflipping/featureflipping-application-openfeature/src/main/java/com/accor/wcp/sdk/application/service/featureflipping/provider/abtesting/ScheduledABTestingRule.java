package com.accor.wcp.sdk.application.service.featureflipping.provider.abtesting;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;

/**
 * Configuration example:
 * scheduled.startDate:2024-12-01
 * scheduled.endDate:2025-12-31
 * scheduled.startTime:14:00
 * scheduled.endTime:15:00
 */
@Slf4j
public class ScheduledABTestingRule implements ABTestingRule {
    @Override
    public String id() {
        return "scheduled";
    }

    @Override
    public boolean activate(Map<String, String> configs) {
        String startDateStr = configs.get("scheduled.startDate");
        String endDateStr = configs.get("scheduled.endDate");
        String startTimeStr = configs.get("scheduled.startTime");
        String endTimeStr = configs.get("scheduled.endTime");

        TemporalAccessor startDate = DateTimeFormatter.ISO_DATE.parse(startDateStr);
        TemporalAccessor endDate = DateTimeFormatter.ISO_DATE.parse(endDateStr);

        TemporalAccessor startTime = DateTimeFormatter.ISO_TIME.parse(startTimeStr);
        TemporalAccessor endTime = DateTimeFormatter.ISO_TIME.parse(endTimeStr);

        LocalDate now = LocalDate.now();
        if (now.isAfter(LocalDate.from(startDate)) && now.isBefore(LocalDate.from(endDate))) {
            LocalTime time = LocalTime.now();
            return time.isAfter(LocalTime.from(startTime)) && time.isBefore(LocalTime.from(endTime));
        }

        return false;
    }
}
