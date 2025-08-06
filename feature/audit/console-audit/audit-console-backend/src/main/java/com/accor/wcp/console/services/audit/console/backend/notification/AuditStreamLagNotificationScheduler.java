package com.accor.wcp.console.services.audit.console.backend.notification;

import static java.util.Objects.isNull;

import com.accor.wcp.console.sdk.notification.NotificationLevelType;
import com.accor.wcp.console.sdk.notification.WCPConsoleUserNotificationService;
import com.accor.wcp.console.sdk.notification.impl.DefaultUserNotification;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatch.model.*;

@Component
@Slf4j
@EnableScheduling
class AuditStreamLagNotificationScheduler {

  private final WCPConsoleUserNotificationService userNotificationService;
  private final AmazonMetricRetrieverService amazonMetricRetrieverService;

  @Value("${aws.env}")
  private String env;

  private String sentNotificationId;

  private static final int RATE = 1000 * 60 * 5;

  AuditStreamLagNotificationScheduler(
      WCPConsoleUserNotificationService usernotification,
      AmazonMetricRetrieverService amazonMetricRetrieverService) {
    this.userNotificationService = usernotification;
    this.amazonMetricRetrieverService = amazonMetricRetrieverService;
  }

  @Scheduled(fixedRate = AuditStreamLagNotificationScheduler.RATE)
  void verifiedAuditStreamLag() {
    try {

      long limitLag = 1000 * 60 * 10;

      DefaultUserNotification notification =
          DefaultUserNotification.builder()
              .level(NotificationLevelType.WARNING)
              .applicationId("core")
              .description("Audit stream alert KO - more than 10 minutes delay")
              .date(new Date())
              .build();

      List<MetricDataResult> metric =
          amazonMetricRetrieverService.getKinesisMetric(
              "GetRecords.IteratorAgeMilliseconds", "auditstream-" + env);

      if (metric.isEmpty()) {
        return;
      }

      MetricDataResult metricData = metric.get(0);

      if (metricData.values().isEmpty()) {
        return;
      }

      Double lastValue = metricData.values().get(metricData.values().size() - 1);

      if (lastValue > limitLag) {
        log.warn("Audit stream alert KO - more than 10 minutes delay");
        if (isNull(this.sentNotificationId)) {
          this.sentNotificationId = userNotificationService.create("audit", notification);
        } else {
          userNotificationService.update(sentNotificationId, notification);
        }
      } else {
        if (!isNull(this.sentNotificationId)) {
          notification.setDescription("Audit stream alert OK - less than 10 minutes delay");
          notification.setLevel(NotificationLevelType.INFO);
          userNotificationService.create("audit", notification);
          this.sentNotificationId = null;
        }
      }
    } catch (CloudWatchException e) {
      log.warn(e.awsErrorDetails().errorMessage());
    }
  }
}
