package com.accor.wcp.console.services.sqsdlq.dynamo;

import static java.util.Collections.emptyList;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.accor.wcp.console.services.sqsdlq.NotFoundException;
import com.accor.wcp.console.services.sqsdlq.SqsDlqSettingsService;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.AttributeDocument;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsAttributeType;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocument;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentRetry;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentStatus;
import com.accor.wcp.console.services.sqsdlq.mapper.SqsMessageMapper;
import com.accor.wcp.console.services.sqsdlq.model.SqsDlqSettingsDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsMessageDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsMessagesDto;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsDocumentService {

  private final SqsDocumentRepository sqsDocumentRepository;
  private final SqsMessageMapper sqsMessageMapper;
  private final SqsDlqSettingsService sqsDlqSettingsService;

  public SqsMessagesDto findByQueueNameAndStatus(
      String domain, String env, String applicationId, String queueName, List<String> status) {

    log.debug("Get messages : {} {} {} {}", domain, env, applicationId, queueName);

    List<SqsDocument> sqsDocuments =
        sqsDocumentRepository.findAllByExpression(queueName, domain, env, status);

    List<SqsMessageDto> lstSqsMessageDto =
        sqsDocuments.stream().map(sqsMessageMapper::fromDocument).collect(Collectors.toList());

    Map<SqsDocumentStatus, Long> nbMessagesByStatus =
        sqsDocuments.stream()
            .collect(Collectors.groupingBy(SqsDocument::getStatus, Collectors.counting()));

    return SqsMessagesDto.builder()
        .messages(lstSqsMessageDto)
        .nbMessagesByStatus(nbMessagesByStatus)
        .build();
  }

  public SqsMessageDto findById(
      String domain, String env, String applicationId, String queueName, String id)
      throws NotFoundException {
    log.debug("Get message by Id : {} {} {} {} {}", domain, env, applicationId, queueName, id);

    Optional<SqsDocument> sqsDocument = sqsDocumentRepository.findById(id);

    if (sqsDocument.isEmpty()) {
      throw new NotFoundException("Message id not found : " + queueName + " - " + id);
    } else {
      SqsMessageDto result = sqsMessageMapper.fromDocument(sqsDocument.get());
      return result;
    }
  }

  public void update(
      String domain,
      String env,
      String applicationId,
      String queueName,
      String id,
      SqsMessageDto sqsMessageDto)
      throws NotFoundException {
    log.debug("Update message : {} {} {} {} {}", domain, env, applicationId, queueName, id);

    Optional<SqsDocument> sqsDocument = sqsDocumentRepository.findById(id);

    if (sqsDocument.isEmpty()) {
      throw new NotFoundException("Message id not found : " + queueName + " - " + id);
    } else {
      SqsDlqSettingsDto settings =
          sqsDlqSettingsService.getSqsDlqSettingsDto(env, queueName, applicationId);
      sqsDocumentRepository.update(
          sqsMessageMapper.toDocument(sqsMessageDto, id, queueName, settings.getTtl()));
    }
  }

  public SqsDocumentRetry addIdRetryAttributeAndSaveRetry(
      String domain,
      String env,
      String applicationId,
      String queueName,
      String parentId,
      SqsDocumentRetry sqsDocumentRetry)
      throws NotFoundException {
    Optional<SqsDocument> sqsDocumentOpt = sqsDocumentRepository.findById(parentId);

    if (sqsDocumentOpt.isEmpty()) {
      throw new NotFoundException("Message id not found : " + parentId);
    } else {
      sqsDocumentRetry
          .getAttributes()
          .add(
              AttributeDocument.builder()
                  .name("WCP_RETRY_MESSAGE_ID")
                  .type(SqsAttributeType.STRING)
                  .value(UUID.randomUUID().toString())
                  .build());

      SqsDocument sqsDocument = sqsDocumentOpt.get();
      sqsDocument.getRetries().add(sqsDocumentRetry);
      sqsDocumentRepository.update(sqsDocument);

      sqsDocumentRetry
          .getAttributes()
          .add(
              AttributeDocument.builder()
                  .name("WCP_RETRY_MESSAGE_PARENT_ID")
                  .type(SqsAttributeType.STRING)
                  .value(parentId)
                  .build());
    }
    return sqsDocumentRetry;
  }

  public void deleteByIds(
      String domain, String env, String applicationId, String queueName, List<String> ids) {
    log.debug("Delete messages : {} {} {} {}", domain, env, applicationId, queueName);
    if (isEmpty(ids)) {
      return;
    }
    ids.forEach(sqsDocumentRepository::deleteById);
  }

  public void deleteAll(String domain, String env, String applicationId, String queueName) {
    log.debug("Delete all messages : {} {} {} {}", domain, env, applicationId, queueName);
    sqsDocumentRepository.findAllByExpression(queueName, domain, env, emptyList()).stream()
        .map(SqsDocument::getId)
        .forEach(sqsDocumentRepository::deleteById);
  }
}
