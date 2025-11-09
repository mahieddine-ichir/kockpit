package org.kockpit.audit.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.api.model.AuditReport;
import software.amazon.kinesis.exceptions.InvalidStateException;
import software.amazon.kinesis.exceptions.ShutdownException;
import software.amazon.kinesis.lifecycle.events.*;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
class RecordProcessor implements ShardRecordProcessor {

  private final AuditRequestConverter auditRequestConverter;

  private final AuditConsumer auditConsumer;

  private boolean leaseLostFlag;

  @Override
  public void initialize(InitializationInput initializationInput) {
    // Nothing to do at initialization
  }

  @Override
  public void processRecords(ProcessRecordsInput processRecordsInput) {
    long startTimeMs = System.currentTimeMillis();

    // Data is read here from the Kinesis data stream
    List<KinesisClientRecord> records = processRecordsInput.records();

    // Convert data
    List<AuditReport> auditReportRequests =
        auditRequestConverter.convertToAuditReportRequests(records);

    // delegate to consumers
    auditReportRequests.stream().filter(Objects::nonNull)
              .forEach(auditConsumer);

    long endTimeMs = System.currentTimeMillis();
    long elapsed = endTimeMs - startTimeMs;
    log.debug(">>>>> TIME TO PROCESS {} msgs = {} ms <<<<<", records.size(), elapsed);

    // If shard was lost, it cannot checkpoint
    if (leaseLostFlag) {
      return;
    }

    try {
      /*
       * KCL assumes that the call to checkpoint means that all records have been
       * processed, records which are passed to the record processor.
       */
      processRecordsInput.checkpointer().checkpoint();

    } catch (Exception e) {
      log.error("Error during checkpoint: {}", e.getMessage());
    }
  }

  @Override
  public void leaseLost(LeaseLostInput leaseLostInput) {
    log.warn("LeaseLostInput {}", leaseLostInput);
    this.leaseLostFlag = true;
  }

  @Override
  public void shardEnded(ShardEndedInput shardEndedInput) {
    try {
      shardEndedInput.checkpointer().checkpoint();
    } catch (ShutdownException | InvalidStateException e) {
      log.warn("Shard ended exception: {}", e.getMessage(), e);
    }
  }

  @Override
  public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
    try {
      shutdownRequestedInput.checkpointer().checkpoint();
    } catch (ShutdownException | InvalidStateException e) {
      log.warn("Shutdown requested exception: {}", e.getMessage(), e);
    }
  }
}
