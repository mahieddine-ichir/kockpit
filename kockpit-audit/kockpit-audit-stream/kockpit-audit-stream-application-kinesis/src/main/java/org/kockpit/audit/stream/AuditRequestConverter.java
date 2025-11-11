package org.kockpit.audit.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.model.AuditReport;
import org.opensearch.core.common.io.stream.ByteBufferStreamInput;
import org.springframework.stereotype.Component;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

@Component
@Slf4j
class AuditRequestConverter {

  private final ObjectMapper objectMapper;

  AuditRequestConverter() {
    objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
  }

  List<AuditReport> convertToAuditReportRequests(List<KinesisClientRecord> records) {
    return records.stream()
        .map(this::convertToAuditReportRequest)
        .filter(Objects::nonNull)
        .toList();
  }

  private AuditReport convertToAuditReportRequest(KinesisClientRecord kinesisClientRecord) {
    try (InputStream recordData = getRecordData(kinesisClientRecord)) {
      return objectMapper.readValue(recordData, AuditReport.class);
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
