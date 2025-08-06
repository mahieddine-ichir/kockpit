package com.accor.wcp.console.services.audit.console.backend.notification;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.CloudWatchException;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.cloudwatch.model.MetricStat;

@Service
class AmazonMetricRetrieverService {

  private final CloudWatchClient cloudWatchClient;

  AmazonMetricRetrieverService(CloudWatchClient amazonCloudwatchForAuditService) {
    this.cloudWatchClient = amazonCloudwatchForAuditService;
  }

  List<MetricDataResult> getKinesisMetric(String metricName, String streamName)
      throws CloudWatchException {
    long offsetInMilliseconds = 1000 * 60 * 5;

    Instant endDate = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    Instant start =
        Instant.ofEpochMilli(endDate.toEpochMilli() - offsetInMilliseconds)
            .truncatedTo(ChronoUnit.SECONDS);

    Metric met =
        Metric.builder()
            .metricName(metricName)
            .namespace("AWS/Kinesis")
            .dimensions(Dimension.builder().name("StreamName").value(streamName).build())
            .build();

    MetricStat metStat = MetricStat.builder().stat("Maximum").period(60).metric(met).build();

    MetricDataQuery dataQUery =
        MetricDataQuery.builder()
            .metricStat(metStat)
            .id("iteratorAgeAuditStream")
            .returnData(true)
            .build();

    List<MetricDataQuery> dq = new ArrayList<>();
    dq.add(dataQUery);

    GetMetricDataRequest getMetReq =
        GetMetricDataRequest.builder()
            .maxDatapoints(100)
            .startTime(start)
            .endTime(endDate)
            .metricDataQueries(dq)
            .build();

    GetMetricDataResponse response = this.cloudWatchClient.getMetricData(getMetReq);
    List<MetricDataResult> data = response.metricDataResults();

    return data;
  }
}
