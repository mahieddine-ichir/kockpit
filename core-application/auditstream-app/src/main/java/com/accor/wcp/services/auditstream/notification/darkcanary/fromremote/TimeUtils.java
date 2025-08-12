package com.accor.wcp.services.auditstream.notification.darkcanary.fromremote;

import com.accor.wcp.services.auditstream.notification.darkcanary.config.TimeInterval;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.time.DayOfWeek.*;

@UtilityClass
public class TimeUtils {

    static List<DayOfWeek> parseDays(TimeInterval timeInterval) {
        String day = timeInterval.getDay();
        if (! StringUtils.hasText(day)) {
            return Collections.emptyList();
        }
        if (day.contains("-")) {
            return Arrays.stream(day.split("-"))
                    .map(TimeUtils::parseDay)
                    .toList();
        } else {
            return List.of(parseDay(day));
        }
    }

    static List<Integer> parseHours(TimeInterval timeInterval) {
        String hour = timeInterval.getHour();
        if (! StringUtils.hasText(hour)) {
            return Collections.emptyList();
        }
        if (hour.contains("-")) {
            return Arrays.stream(hour.split("-"))
                    .map(Integer::parseInt)
                    .toList();
        } else {
            return List.of(Integer.parseInt(hour));
        }
    }

    private static DayOfWeek parseDay(String day) {
        return switch (day) {
            case "MON" -> MONDAY;
            case "TUE" -> TUESDAY;
            case "WED" -> WEDNESDAY;
            case "THU" -> THURSDAY;
            case "FRI" -> FRIDAY;
            case "SAT" -> SATURDAY;
            case "SUN" -> SUNDAY;
            default -> throw new IllegalArgumentException("Invalid day: " + day);
        };
    }

    static boolean isEligible(TimeInterval timeInterval, LocalDateTime localDateTime) {
        return isHourEligible(timeInterval, localDateTime) &&
                isDayEligible(timeInterval, localDateTime);
    }

    private static boolean isHourEligible(TimeInterval timeInterval, LocalDateTime localDateTime) {
        // hours
        List<Integer> integers = parseHours(timeInterval);
        if (integers.isEmpty()) {
            return false;
        } else if (integers.size() == 1) {
            return localDateTime.getHour() == integers.get(0);
        } else {
            int hour = localDateTime.getHour();
            return (hour >= integers.get(0) && hour <= integers.get(1));
        }
    }

    private static boolean isDayEligible(TimeInterval timeInterval, LocalDateTime localDateTime) {
        // hours
        List<DayOfWeek> dayOfWeeks = parseDays(timeInterval);
        if (dayOfWeeks.isEmpty()) {
            return false;
        } else if (dayOfWeeks.size() == 1) {
            return localDateTime.getDayOfWeek() == dayOfWeeks.get(0);
        } else {
            DayOfWeek dayOfWeek = localDateTime.getDayOfWeek();
            return (dayOfWeek.getValue() >= dayOfWeeks.get(0).getValue() &&
                    dayOfWeek.getValue() <= dayOfWeeks.get(1).getValue());
        }
    }
}
