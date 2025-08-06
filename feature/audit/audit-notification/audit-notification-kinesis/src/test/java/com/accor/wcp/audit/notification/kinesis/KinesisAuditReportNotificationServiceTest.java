package com.accor.wcp.audit.notification.kinesis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.accor.wcp.audit.AuditReport;
import com.accor.wcp.audit.AuditReport.AuditJsonReport;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;

@ExtendWith(MockitoExtension.class)
class KinesisAuditReportNotificationServiceTest {

  KinesisAuditReportNotificationService underTest;

  @Mock KinesisAsyncClient kinesisAsyncClient;

  @BeforeEach
  void setUp() {
    underTest = new KinesisAuditReportNotificationService(kinesisAsyncClient, "streamName");
  }

  @Test
  void should_put_records_to_kinesis_stream() {
    MockAuditJsonReport firstAuditReport = new MockAuditJsonReport(AuditReport.builder().build());
    MockAuditJsonReport secondAuditReport = new MockAuditJsonReport(AuditReport.builder().build());

    List<AuditJsonReport> auditReports = List.of(firstAuditReport, secondAuditReport);
    underTest.notify(auditReports);
    verify(kinesisAsyncClient, times(1)).putRecords(any(PutRecordsRequest.class));
  }

  private class MockAuditJsonReport extends AuditJsonReport {

    public MockAuditJsonReport(AuditReport auditReport) {
      super(auditReport, new ArrayList<>());
    }

    @Override
    public String getAuditJson() {
      return "{}";
    }
  }
}
