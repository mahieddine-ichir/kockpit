package com.accor.wcp.services.auditstream.notification.kinesis;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.accor.wcp.services.auditstream.notification.service.AuditReportService;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.kinesis.lifecycle.events.ProcessRecordsInput;
import software.amazon.kinesis.processor.RecordProcessorCheckpointer;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

@ExtendWith(MockitoExtension.class)
class RecordProcessorTest {

  @Mock AuditReportService auditReportService;
  @Mock RecordProcessorCheckpointer recordProcessorCheckpointer;

  RecordProcessor underTest;

  @BeforeEach
  void setup() {
    AuditRequestConverter auditRequestConverter = new AuditRequestConverter();
    underTest = new RecordProcessor(auditRequestConverter, auditReportService);
  }

  @Test
  void should_process_records() throws IOException {
    ByteBuffer recordData = ByteBuffer.wrap("{\"domain\":\"dev\"}".getBytes());
    KinesisClientRecord record = KinesisClientRecord.builder().data(recordData).build();
    ProcessRecordsInput recordsInput =
        ProcessRecordsInput.builder()
            .records(List.of(record))
            .checkpointer(recordProcessorCheckpointer)
            .build();

    underTest.processRecords(recordsInput);

    verify(auditReportService, times(1)).report(any(List.class));
  }
}
