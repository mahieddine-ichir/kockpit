package com.accor.wcp.services.auditstream.notification.kinesis;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.service.AuditReportService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.kinesis.exceptions.InvalidStateException;
import software.amazon.kinesis.exceptions.ShutdownException;
import software.amazon.kinesis.lifecycle.events.InitializationInput;
import software.amazon.kinesis.lifecycle.events.LeaseLostInput;
import software.amazon.kinesis.lifecycle.events.ProcessRecordsInput;
import software.amazon.kinesis.lifecycle.events.ShardEndedInput;
import software.amazon.kinesis.lifecycle.events.ShutdownRequestedInput;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

/**
 * Quick local bench with curl and wcp-samples. xargs -I % -P 10 curl
 * "http://localhost:8080/wcpsamples/sayHello/cyril" < <(printf '%s\n' {1..5000}) This class
 * implement kinesis records to audit elastic search storage logics.
 */
class RecordProcessor implements ShardRecordProcessor {

  private static final Logger log = LoggerFactory.getLogger(RecordProcessor.class);
  private final AuditReportService auditReportService;
  private final AuditRequestConverter auditRequestConverter;
  private boolean leaseLostFlag;

  public RecordProcessor(
      AuditRequestConverter auditRequestConverter, AuditReportService auditReportService) {
    this.auditRequestConverter = auditRequestConverter;
    this.auditReportService = auditReportService;
  }

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
    List<AuditReportRequest> auditReportRequests =
        auditRequestConverter.convertToAuditReportRequests(records);

    // Bulk index
    auditReportService.report(auditReportRequests);

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
