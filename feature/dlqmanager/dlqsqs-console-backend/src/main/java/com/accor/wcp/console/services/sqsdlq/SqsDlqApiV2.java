package com.accor.wcp.console.services.sqsdlq;

import static java.util.Objects.isNull;

import com.accor.wcp.console.services.sqsdlq.dynamo.SqsDocumentServiceV2;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentRetry;
import com.accor.wcp.console.services.sqsdlq.model.Count;
import com.accor.wcp.console.services.sqsdlq.model.PartitionKey;
import com.accor.wcp.console.services.sqsdlq.model.RetriesDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsDlqSettingsDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsMessageDtoV2;
import com.accor.wcp.console.services.sqsdlq.model.SqsMessagesDtoV2;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = "${console.services.path-prefix}/sqsdlq",
    produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Validated
class SqsDlqApiV2 {
  private final SqsAwsServiceV2 sqsAwsServiceV2;
  private final SqsDocumentServiceV2 sqsDocumentServiceV2;
  private final MigrationService migrationService;
  private final SqsDlqSettingsService sqsDlqSettingsService;

  @GetMapping(value = "/{applicationId}/{queueName}/messages")
  public SqsMessagesDtoV2 get(
      @PathVariable(value = "domain") String domain,
      @PathVariable(value = "env") String env,
      @PathVariable(value = "applicationId") String applicationId,
      @PathVariable(value = "queueName") String queueName,
      @RequestParam(name = "lastSortKey", required = false) String lastSortKey,
      @RequestParam(value = "status", required = false) List<String> status) {
    PartitionKey partitionKey =
        PartitionKey.builder().domain(domain).env(env).queueName(queueName).build();
    return sqsDocumentServiceV2.find(partitionKey, lastSortKey, status);
  }

  @GetMapping(value = "/{applicationId}/{queueName}/{id}/message")
  public SqsMessageDtoV2 getOne(
      @PathVariable(value = "domain") String domain,
      @PathVariable(value = "env") String env,
      @PathVariable(value = "applicationId") String applicationId,
      @PathVariable(value = "queueName") String queueName,
      @PathVariable(value = "id") String id)
      throws NotFoundException {
    PartitionKey partitionKey =
        PartitionKey.builder().domain(domain).env(env).queueName(queueName).build();
    return sqsDocumentServiceV2.fetchOne(partitionKey, id);
  }

  @GetMapping(value = "/{applicationId}/{queueName}/count")
  public Count count(
      @PathVariable(value = "domain") String domain,
      @PathVariable(value = "env") String env,
      @PathVariable(value = "applicationId") String applicationId,
      @PathVariable(value = "queueName") String queueName) {
    PartitionKey partitionKey =
        PartitionKey.builder().domain(domain).env(env).queueName(queueName).build();
    return sqsDocumentServiceV2.getCount(partitionKey);
  }

  @PutMapping(value = "/{applicationId}/{queueName}/{id}/message")
  public void update(
      @PathVariable(value = "domain") String domain,
      @PathVariable(value = "env") String env,
      @PathVariable(value = "applicationId") String applicationId,
      @PathVariable(value = "queueName") String queueName,
      @PathVariable(value = "id") String id,
      @Valid @RequestBody SqsMessageDtoV2 message)
      throws NotFoundException {
    message.setId(id);
    PartitionKey partitionKey =
        PartitionKey.builder().domain(domain).env(env).queueName(queueName).build();
    sqsDocumentServiceV2.update(partitionKey, message);
  }

  @DeleteMapping(value = "/{applicationId}/{queueName}/messages")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable(value = "domain") String domain,
      @PathVariable(value = "env") String env,
      @PathVariable(value = "applicationId") String applicationId,
      @PathVariable(value = "queueName") String queueName,
      @RequestBody(required = false) List<String> ids) {
    PartitionKey partitionKey =
        PartitionKey.builder().domain(domain).env(env).queueName(queueName).build();
    sqsDocumentServiceV2.delete(partitionKey, ids);
  }

  @PostMapping(value = "/{applicationId}/{queueName}/retries")
  public ResponseEntity<List<SqsDocumentRetry>> retries(
      @PathVariable(value = "domain") String domain,
      @PathVariable(value = "env") String env,
      @PathVariable(value = "applicationId") String applicationId,
      @PathVariable(value = "queueName") String queueName,
      @Valid @RequestBody(required = false) RetriesDto retries)
      throws NotFoundException {
    PartitionKey partitionKey =
        PartitionKey.builder().domain(domain).env(env).queueName(queueName).build();
    SqsDlqSettingsDto settings = sqsDlqSettingsService.getSqsDlqSettingsDto(partitionKey);
    if (isNull(retries)) {
      sqsAwsServiceV2.sendAllRetryMessage(partitionKey, settings);
      return ResponseEntity.noContent().build();
    }
    List<SqsDocumentRetry> result =
        sqsAwsServiceV2.sendMultipleRetryMessage(
            partitionKey,
            Optional.of(retries).map(RetriesDto::getRetries).orElse(Collections.emptyList()),
            settings);
    return ResponseEntity.ok(result);
  }

  @PostMapping(value = "/migration")
  public void migration(
      @PathVariable(value = "domain") String domain, @PathVariable(value = "env") String env) {
    migrationService.migrate();
  }
}
