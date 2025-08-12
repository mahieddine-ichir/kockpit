package com.accor.wcp.services.auditstream.notification.darkcanary.fromremote;

import com.accor.wcp.services.auditstream.notification.darkcanary.config.TimeInterval;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.Month.APRIL;

class TimeUtilsTest {

    @Test
    @DisplayName("day: on -, should return an interval")
    void testParseDaysOnInterval() {
        TimeInterval timeInterval = new TimeInterval();
        timeInterval.setDay("MON-FRI");
        List<DayOfWeek> dayOfWeeks = TimeUtils.parseDays(timeInterval);

        Assertions.assertEquals(DayOfWeek.MONDAY, dayOfWeeks.get(0));
        Assertions.assertEquals(DayOfWeek.FRIDAY, dayOfWeeks.get(1));
    }

    @Test
    @DisplayName("day: on no -, should return a single value")
    void testParseDaysOnSingleValue() {
        TimeInterval timeInterval = new TimeInterval();
        timeInterval.setDay("MON");
        List<DayOfWeek> dayOfWeeks = TimeUtils.parseDays(timeInterval);

        Assertions.assertEquals(1, dayOfWeeks.size());
        Assertions.assertEquals(DayOfWeek.MONDAY, dayOfWeeks.get(0));
    }

    @Test
    @DisplayName("hour: on -, should return an interval")
    void testParseHoursOnInterval() {
        TimeInterval timeInterval = new TimeInterval();
        timeInterval.setHour("0-23");
        List<Integer> hours = TimeUtils.parseHours(timeInterval);

        Assertions.assertEquals(0, hours.get(0));
        Assertions.assertEquals(23, hours.get(1));
    }

    @Test
    @DisplayName("hour: on no -, should return a single value")
    void testParseHoursOnSingleValue() {
        TimeInterval timeInterval = new TimeInterval();
        timeInterval.setHour("1");
        List<Integer> hours = TimeUtils.parseHours(timeInterval);

        Assertions.assertEquals(1, hours.size());
        Assertions.assertEquals(1, hours.get(0));
    }

    @Test
    @DisplayName("testing eligibility on intervals")
    void testEligibleOnEligibleOnIntervals() {
        TimeInterval timeInterval = new TimeInterval();
        timeInterval.setHour("8-20");
        timeInterval.setDay("MON-FRI");

        // tuesday, 10:00
        Assertions.assertTrue(TimeUtils.isEligible(timeInterval, LocalDateTime.of(2025, APRIL, 1, 10, 0)));
        // friday, 10:00
        Assertions.assertTrue(TimeUtils.isEligible(timeInterval, LocalDateTime.of(2025, APRIL, 4, 10, 0)));
        // exclusions
        // friday, 21:00
        Assertions.assertFalse(TimeUtils.isEligible(timeInterval, LocalDateTime.of(2025, APRIL, 4, 21, 0)));
        // saturday, 21:00
        Assertions.assertFalse(TimeUtils.isEligible(timeInterval, LocalDateTime.of(2025, APRIL, 5, 10, 0)));
        // saturday, 00:00
        Assertions.assertFalse(TimeUtils.isEligible(timeInterval, LocalDateTime.of(2025, APRIL, 5, 0, 0)));
    }

    @Test
    @DisplayName("should be eligibility on single values")
    void testEligibleOnEligibleOnSingleValue() {
        TimeInterval timeInterval = new TimeInterval();
        timeInterval.setHour("10");
        timeInterval.setDay("TUE");

        // tuesday, 10:00
        Assertions.assertTrue(TimeUtils.isEligible(timeInterval, LocalDateTime.of(2025, APRIL, 1, 10, 0)));
        // exclusions
        // friday, 10:00
        Assertions.assertFalse(TimeUtils.isEligible(timeInterval, LocalDateTime.of(2025, APRIL, 4, 10, 0)));
        // friday, 21:00
        Assertions.assertFalse(TimeUtils.isEligible(timeInterval, LocalDateTime.of(2025, APRIL, 4, 21, 0)));
        // saturday, 21:00
        Assertions.assertFalse(TimeUtils.isEligible(timeInterval, LocalDateTime.of(2025, APRIL, 5, 10, 0)));
        // saturday, 00:00
        Assertions.assertFalse(TimeUtils.isEligible(timeInterval, LocalDateTime.of(2025, APRIL, 5, 0, 0)));
    }
}