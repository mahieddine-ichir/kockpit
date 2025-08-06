package com.accor.wcp.console.services.core.communication.app2wcp;

import com.accor.wcp.audit.AuditorEventService;
import com.accor.wcp.audit.AuditorKeyValueService;
import com.accor.wcp.audit.AuditorService;
import com.accor.wcp.audit.IndexedKeyValue;
import com.accor.wcp.audit.module.web.WebAuditEvent;
import com.accor.wcp.audit.module.web.WebAuditReportData;
import com.accor.wcp.audit.module.web.request.HttpAuditedRequest;
import com.accor.wcp.audit.module.web.response.HttpAuditedResponse;
import com.accor.wcp.console.services.core.communication.app2wcp.model.ApplicationMessageNotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import software.amazon.kinesis.exceptions.InvalidStateException;
import software.amazon.kinesis.exceptions.ShutdownException;
import software.amazon.kinesis.lifecycle.events.*;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

import static com.accor.wcp.audit.IndexedKeyValue.of;

class App2WcpRecordProcessor implements ShardRecordProcessor {

  private static final Logger log = LoggerFactory.getLogger(App2WcpRecordProcessor.class);
  private final App2WcpNotificationService app2WcpNotificationService;
  private final ObjectMapper objectMapper;
  private final AuditorService auditorService;
  private final AuditorKeyValueService auditorKeyValueService;
  private final AuditorEventService auditorEventService;

  public App2WcpRecordProcessor(App2WcpNotificationService app2WcpNotificationService,
                                AuditorService auditorService,
                                AuditorKeyValueService auditorKeyValueService,
                                AuditorEventService auditorEventService) {
    this.app2WcpNotificationService = app2WcpNotificationService;
    this.auditorService = auditorService;
    this.auditorKeyValueService = auditorKeyValueService;
    this.auditorEventService = auditorEventService;
    objectMapper = new ObjectMapper();
  }

  @Override
  public void initialize(InitializationInput initializationInput) {}

  @Override
  public void processRecords(ProcessRecordsInput processRecordsInput) {
    try {
      /*
       * KCL assumes that the call to checkpoint means that all records have been
       * processed, records which are passed to the record processor.
       */
      processRecordsInput.checkpointer().checkpoint();

    } catch (Exception e) {
      log.error("Error during Processing of records", e);
    }

    // Data is read here from the Kinesis data stream
    processRecordsInput
        .records()
        .forEach(this::treatIncomingCommunicationMessage);
  }

  private void treatIncomingCommunicationMessage(KinesisClientRecord kinesisClientRecord) {
    long startAuditMs = System.currentTimeMillis();

    try {
      log.debug(
          "Processing App notification Record For Partition ConsoleServiceKey : {}, record: {}",
          kinesisClientRecord.partitionKey(),
              kinesisClientRecord);

      // Old notification? => skip it
      if (kinesisClientRecord
          .approximateArrivalTimestamp()
          .isBefore(Instant.now().minus(10, ChronoUnit.MINUTES))) {
        log.info(
            "Skipping old notification (age > 10 minutes). Record={}", kinesisClientRecord);
        return;
      }

      auditorService.startAudit();

      ApplicationMessageNotificationDto applicationMessageNotificationDto;
      WebAuditEvent webAuditEvent;
      HttpAuditedResponse httpAuditedResponse;
      try {
        ByteBuffer data = kinesisClientRecord.data();
        byte[] b = new byte[data.remaining()];
        data.get(b);

        httpAuditedResponse = HttpAuditedResponse.builder()
                .body("")
                .headers(new HttpHeaders())
                .status(0)
                .build();
        webAuditEvent = WebAuditEvent.builder()
                .httpAuditedRequest(HttpAuditedRequest.builder()
                        .body(new String(b))
                        .uri(kinesisClientRecord.partitionKey())
                        .principal("")
                        .params(new HashMap<>())
                        .headers(new HttpHeaders())
                        .method("CONSUME")
                        .build())
                .httpAuditedResponse(httpAuditedResponse)
                .build();
        auditorEventService.addAuditEvents(WebAuditReportData.TYPE, List.of(webAuditEvent));

        applicationMessageNotificationDto =
            objectMapper.readValue(b, ApplicationMessageNotificationDto.class);
      } catch (Exception e) {
        log.error("Error parsing record {}", kinesisClientRecord, e);
        return;
      }

      // Audit
      String origin = applicationMessageNotificationDto.getDomain() + "-" + applicationMessageNotificationDto.getEnv() + "-" + applicationMessageNotificationDto.getApplicationId();
      String requestUri = applicationMessageNotificationDto.getServiceId() + "/" + origin + "/" + applicationMessageNotificationDto.getInstanceId();
      auditorKeyValueService.addIndexedKeyValues(List.of(
              of("serviceId", applicationMessageNotificationDto.getServiceId()),
              of("applicationId", applicationMessageNotificationDto.getApplicationId()),
              of("domain", applicationMessageNotificationDto.getDomain()),
              of("env", applicationMessageNotificationDto.getEnv()),
              of("instanceId", applicationMessageNotificationDto.getInstanceId()),
              of("requestUri", requestUri),
              of("traceId", applicationMessageNotificationDto.getRequestMessageId()),
              of("origin", origin)
      ));

      try {
        log.debug("notification: {}", applicationMessageNotificationDto);
        app2WcpNotificationService.notify(applicationMessageNotificationDto);
      } catch (Exception e) {
        log.error("Error treating record {}", kinesisClientRecord, e);
      }
    } finally {
      if (auditorService.isAuditStarted()) {
        // Duration ?
        auditorKeyValueService.addIndexedKeyValues(List.of(
                of("duration", System.currentTimeMillis() - startAuditMs)));
        // Stop & push
        auditorService.stopAuditAndNotify();
      }
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
