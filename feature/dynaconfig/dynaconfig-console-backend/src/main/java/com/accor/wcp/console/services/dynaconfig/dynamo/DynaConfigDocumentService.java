package com.accor.wcp.console.services.dynaconfig.dynamo;

import com.accor.wcp.console.services.dynaconfig.dto.PropertyDto;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigDocument;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigDocument.PropertyDocument;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigDocument.PropertyHistoryDocument;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigHistoryDocument;
import com.accor.wcp.console.services.dynaconfig.dynamo.repository.DynaConfigDocumentRepository;
import com.accor.wcp.console.services.dynaconfig.dynamo.repository.DynaConfigHistoryDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class DynaConfigDocumentService {
  private final DynaConfigDocumentRepository repository;
  private final DynaConfigHistoryDocumentRepository historyRepository;

  public Optional<DynaConfigDocument> getDynaConfigDocument(
      String domain, String env, String appId) {
    String id = computeId(domain, env, appId);
    return repository.findById(id);
  }

  public List<PropertyDocument> getDynaConfigProperties(String domain, String env, String appId) {
    Optional<DynaConfigDocument> document = this.getDynaConfigDocument(domain, env, appId);

    if (document.isPresent()) {
      return new ArrayList<>(this.getDocumentProperties(document.get()));
    } else {
      return Collections.emptyList();
    }
  }

  public void removeDocumentProperty(String domain, String env, String appId, String propertyKey) {
    DynaConfigDocument document = getOrCreateDocument(computeId(domain, env, appId));
    document.setLastUpdatedTimestamp(System.currentTimeMillis());
    document.getPropertyValues().remove(propertyKey);

    repository.save(document);
  }

  public void updateDocumentProperty(
      String domain, String env, String appId, String propertyKey, String value) {
    DynaConfigDocument document = getOrCreateDocument(computeId(domain, env, appId));
    document.setLastUpdatedTimestamp(System.currentTimeMillis());

    PropertyDocument property = getOrCreatePropertyDocument(document, propertyKey, value, null);
    document.getPropertyValues().put(propertyKey, property);

    repository.save(document);
  }

  public void updateDocumentProperties(
      String domain, String env, String appId, Collection<PropertyDto> inputProps, String username) {
    DynaConfigDocument document = getOrCreateDocument(computeId(domain, env, appId));
    List<PropertyDocument> updatedProperties = new ArrayList<>();

    for (PropertyDto inputProperty : inputProps) {
      String key = inputProperty.getName();
      String value = inputProperty.getValue();
      PropertyDocument property = getOrCreatePropertyDocument(document, key, value, username);
      property.setComment(inputProperty.getComment());
      updatedProperties.add(property);
    }
    document.setPropertyValues(
        updatedProperties.stream().collect(toMap(PropertyDocument::getName, identity())));

    repository.save(document);

  }

 public void flushHistory(String domain, String env, String appId) {
        DynaConfigDocument document = getOrCreateDocument(computeId(domain, env, appId));
        List<PropertyDocument> updatedProperties = new ArrayList<>();
        copyHistory(document);

        for (PropertyDocument oldProperty : document.getPropertyValues().values()) {
            String key = oldProperty.getName();
            String value = oldProperty.getValue();
            PropertyDocument property = getOrCreatePropertyDocumentWithoutHistory(document, key, value);
            property.setComment(property.getComment());
            updatedProperties.add(property);
        }
        document.setPropertyValues(
                updatedProperties.stream().collect(toMap(PropertyDocument::getName, identity())));

        repository.save(document);
    }

    private void copyHistory(DynaConfigDocument document) {
        List<DynaConfigHistoryDocument> newHistories = document.getPropertyValues().entrySet().stream()
                .map(entry -> entry.getValue().getHistory().stream()
                        .map(propertyHistoryDocument -> new DynaConfigHistoryDocument(computeHistoryId(document.getId(), entry.getKey()), System.currentTimeMillis(), propertyHistoryDocument))
                        .toList())
                .flatMap(List::stream)
                .toList();

        newHistories.forEach(historyRepository::save);
    }

    private String computeHistoryId(String documentKey, String configKey) {
        return documentKey + "-" + configKey;
    }

    private String computeId(String domain, String env, String applicationId) {
        return domain + "-" + env + "-" + applicationId;
    }

    private DynaConfigDocument getOrCreateDocument(String id) {
        return repository
                .findById(id)
                .orElse(DynaConfigDocument.builder().id(id).propertyValues(new HashMap<>()).build());
    }

    private PropertyDocument getOrCreatePropertyDocument(
            DynaConfigDocument document, String property, String value, String username) {
        PropertyDocument propertyDocument = getPropertyDocument(document, property, value);
        addEntryInPropertyHistory(propertyDocument, value, username);
        propertyDocument.setValue(value);

        return propertyDocument;
    }

    private PropertyDocument getOrCreatePropertyDocumentWithoutHistory(
            DynaConfigDocument document, String property, String value) {
        PropertyDocument propertyDocument = getPropertyDocument(document, property, value);
        propertyDocument.setValue(value);
        propertyDocument.setHistory(Collections.emptyList());

        return propertyDocument;
    }

    private PropertyDocument getPropertyDocument(DynaConfigDocument document, String property, String value) {
        PropertyDocument propertyDocument =
                document
                        .getPropertyValues()
                        .computeIfAbsent(property, p -> PropertyDocument.builder().name(property).build());
        propertyDocument.setLastUpdatedTimestamp(System.currentTimeMillis());
        return propertyDocument;
    }

    private void addEntryInPropertyHistory(PropertyDocument propertyDocument, String updatedValue, String username) {
        if (!Objects.equals(updatedValue, propertyDocument.getValue())) {
            List<PropertyHistoryDocument> history =
                    Optional.ofNullable(propertyDocument.getHistory()).orElse(new ArrayList<>());
            history.add(
                    PropertyHistoryDocument.builder()
                            .valueBeforeChange(propertyDocument.getValue())
                            .valueAfterChange(updatedValue)
                            .timestamp(System.currentTimeMillis())
                            .username(username)
                            .build());
            propertyDocument.setHistory(history);
        }
    }

    private Collection<PropertyDocument> getDocumentProperties(DynaConfigDocument document) {
        return Optional.ofNullable(document.getPropertyValues())
                .orElse(Collections.emptyMap())
                .values();
    }
}
