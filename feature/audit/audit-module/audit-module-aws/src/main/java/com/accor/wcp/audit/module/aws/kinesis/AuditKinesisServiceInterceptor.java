package com.accor.wcp.audit.module.aws.kinesis;

import static com.accor.wcp.audit.module.aws.kinesis.KinesisAudit.logKinesisQuery;

import com.accor.wcp.audit.AuditNotStartedException;
import com.accor.wcp.audit.AuditorEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.interceptor.Context.BeforeExecution;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;

@Slf4j
public class AuditKinesisServiceInterceptor implements ExecutionInterceptor {

  static ApplicationContext context;
  static List<String> kinesisStreamNames;
  private static final ObjectMapper OBJECT_MAPPER;

  static {
    OBJECT_MAPPER = new ObjectMapper();
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
  }

  public static void setApplicationContext(ApplicationContext context) {
    AuditKinesisServiceInterceptor.context = context;
  }

  public static void setKinesisStreamNames(List<String> kinesisStreamNames) {
    AuditKinesisServiceInterceptor.kinesisStreamNames = kinesisStreamNames;
  }

  @Override
  public void beforeExecution(
      BeforeExecution beforeExecution, ExecutionAttributes executionAttributes) {
    if (Objects.nonNull(context)
        && Objects.nonNull(beforeExecution)
        && beforeExecution.request() instanceof PutRecordRequest
        && !CollectionUtils.isEmpty(kinesisStreamNames)) {
      this.auditKinesisInformation(beforeExecution);
    }
  }

  private void auditKinesisInformation(BeforeExecution execution) {
    PutRecordRequest request = (PutRecordRequest) execution.request();
    AuditorEventService auditorEvents = context.getBean(AuditorEventService.class);

    if (kinesisStreamNames.stream().anyMatch(name -> request.streamName().startsWith(name))) {
      try {
        KinesisAudit kinesisAuditEvent =
            KinesisAudit.builder()
                .startTime(System.currentTimeMillis())
                .endTime(System.currentTimeMillis())
                .streamName(request.streamName())
                .partitionKey(request.partitionKey())
                .payload(retrievePayload(request.data()))
                .build();
        logKinesisQuery(kinesisAuditEvent);
        auditorEvents.addAuditEvents("builtin.kinesis-service", List.of(kinesisAuditEvent));
      } catch (AuditNotStartedException ex) {
        log.info("Audit not started, ignoring this");
      }
    }
  }

  private String retrievePayload(SdkBytes data) {
    byte[] bytes = data.asByteArray();
    try {
      byte[] afterDecompress = decompressRecord(bytes);
      return new String(afterDecompress);
    } catch (IOException e) {
      return "";
    }
  }

  private byte[] decompressRecord(byte[] inputBytes) throws IOException {
    if (isRecordCompressed(inputBytes)) {
      GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(inputBytes));
      return gis.readAllBytes();
    } else {
      return inputBytes;
    }
  }

  private boolean isRecordCompressed(final byte[] compressed) {
    return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC))
        && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
  }
}
