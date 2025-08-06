package com.accor.wcp.console.services.core.application.heartbit;

import com.accor.wcp.console.services.core.application.heartbit.model.HeartBitDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.kinesis.exceptions.InvalidStateException;
import software.amazon.kinesis.exceptions.ShutdownException;
import software.amazon.kinesis.lifecycle.events.*;
import software.amazon.kinesis.processor.ShardRecordProcessor;

class HeartBitRecordProcessor implements ShardRecordProcessor {

  private static final Logger log = LoggerFactory.getLogger(HeartBitRecordProcessor.class);
  private final HeartBitInMemoryManager heartBitInMemoryManager;
  private final ObjectMapper objectMapper;

  public HeartBitRecordProcessor(HeartBitInMemoryManager heartBitInMemoryManager) {
    this.heartBitInMemoryManager = heartBitInMemoryManager;
    objectMapper = new ObjectMapper();
  }

  public void initialize(InitializationInput initializationInput) {
    // Nothing to initialize
  }

  @Override
  public void processRecords(ProcessRecordsInput processRecordsInput) {

    // Data is read here from the Kinesis data stream
    processRecordsInput
        .records()
        .forEach(
            kinesisRecord -> {
              log.debug(
                  "Processing HeartBit Record For Partition Key : {}, record: {}",
                  kinesisRecord.partitionKey(),
                  kinesisRecord);

              // Old notification? => skip it
              if (kinesisRecord
                  .approximateArrivalTimestamp()
                  .isBefore(Instant.now().minus(30, ChronoUnit.SECONDS))) {
                return;
              }

              try {
                ByteBuffer data = kinesisRecord.data();
                byte[] b = new byte[data.remaining()];
                data.get(b);

                HeartBitDto heartBitDto = objectMapper.readValue(b, HeartBitDto.class);
                heartBitInMemoryManager.updateWith(heartBitDto);
                log.debug("heartBitDto: {}", heartBitDto);
              } catch (Exception e) {
                log.error("Error parsing record {}", kinesisRecord, e);
              }
            });

    try {
      /*
       * KCL assumes that the call to checkpoint means that all records have been
       * processed, records which are passed to the record processor.
       */
      processRecordsInput.checkpointer().checkpoint();

    } catch (Exception e) {
      log.error("Error during Processing of records", e);
    }
  }

  @Override
  public void leaseLost(LeaseLostInput leaseLostInput) {
    log.error("LeaseLostInput {}", leaseLostInput);
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
