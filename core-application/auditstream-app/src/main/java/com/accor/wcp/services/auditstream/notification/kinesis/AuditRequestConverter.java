package com.accor.wcp.services.auditstream.notification.kinesis;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.core.common.io.stream.ByteBufferStreamInput;
import org.springframework.stereotype.Component;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

@Component
@Slf4j
class AuditRequestConverter {

  private final ObjectMapper objectMapper;

  AuditRequestConverter() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }

  List<AuditReportRequest> convertToAuditReportRequests(List<KinesisClientRecord> records) {
    return records.stream()
        .map(this::convertToAuditReportRequest)
        .filter(Objects::nonNull)
        .toList();
  }

  private AuditReportRequest convertToAuditReportRequest(KinesisClientRecord kinesisClientRecord) {
    try (InputStream recordData = getRecordData(kinesisClientRecord)) {
      return objectMapper.readValue(recordData, AuditReportRequest.class);
    } catch (IOException e) {
      log.error("Error converting record {} to audit request", kinesisClientRecord, e);
      return null;
    }
  }

  private InputStream getRecordData(KinesisClientRecord kinesisClientRecord) throws IOException {
    ByteBuffer data = kinesisClientRecord.data();
    return decompressRecord(data);
  }

  private InputStream decompressRecord(ByteBuffer byteBuffer) throws IOException {
    if (isRecordCompressed(byteBuffer)) {
      return new GZIPInputStream(new ByteBufferStreamInput(byteBuffer));
    } else {
      return new ByteBufferStreamInput(byteBuffer);
    }
  }

  private boolean isRecordCompressed(final ByteBuffer byteBuffer) {
    return (byteBuffer.get(0) == (byte) (GZIPInputStream.GZIP_MAGIC))
        && (byteBuffer.get(1) == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
  }
}
