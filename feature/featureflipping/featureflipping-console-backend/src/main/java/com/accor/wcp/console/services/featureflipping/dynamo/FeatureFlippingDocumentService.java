package com.accor.wcp.console.services.featureflipping.dynamo;

import com.accor.wcp.console.services.featureflipping.dto.PropertyDto;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument.PropertyDocument;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument.PropertyHistoryDocument;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingHistoryDocument;
import com.accor.wcp.console.services.featureflipping.dynamo.repository.FeatureFlippingDocumentRepository;
import com.accor.wcp.console.services.featureflipping.dynamo.repository.FeatureFlippingHistoryDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class FeatureFlippingDocumentService {
  private final FeatureFlippingDocumentRepository repository;
  private final FeatureFlippingHistoryDocumentRepository historyRepository;

  public Optional<FeatureFlippingDocument> getFeatureFlippingDocument(
      String domain, String env, String appId) {
    String id = computeId(domain, env, appId);
    return repository.findById(id);
  }

  public List<PropertyDocument> getFeatureFlippingProperties(String domain, String env, String appId) {
    Optional<FeatureFlippingDocument> document = this.getFeatureFlippingDocument(domain, env, appId);

    if (document.isPresent()) {
      return new ArrayList<>(this.getDocumentProperties(document.get()));
    } else {
      return Collections.emptyList();
    }
  }

  public void removeDocumentProperty(String domain, String env, String appId, String propertyKey) {
    FeatureFlippingDocument document = getOrCreateDocument(computeId(domain, env, appId));
    document.setLastUpdatedTimestamp(System.currentTimeMillis());
    document.getPropertyValues().remove(propertyKey);

    repository.save(document);
  }

  public void updateDocumentProperty(
      String domain, String env, String appId, String propertyKey, String value) {
    FeatureFlippingDocument document = getOrCreateDocument(computeId(domain, env, appId));
    document.setLastUpdatedTimestamp(System.currentTimeMillis());

    PropertyDocument property = getOrCreatePropertyDocument(document, propertyKey, value, null);
    document.getPropertyValues().put(propertyKey, property);

    repository.save(document);
  }

  public void updateDocumentProperties(
      String domain, String env, String appId, Collection<PropertyDto> inputProps, String username) {
    FeatureFlippingDocument document = getOrCreateDocument(computeId(domain, env, appId));
    List<PropertyDocument> updatedProperties = new ArrayList<>();

    for (PropertyDto inputProperty : inputProps) {
      String key = inputProperty.getKey();
      String value = inputProperty.getValue();
      PropertyDocument property = getOrCreatePropertyDocument(document, key, value, username);
      property.setComment(inputProperty.getComment());
      property.setExpiration(inputProperty.getExpiration());
      updatedProperties.add(property);
    }
    document.setPropertyValues(
        updatedProperties.stream().collect(toMap(PropertyDocument::getKey, identity())));

    repository.save(document);

  }

 public void flushHistory(String domain, String env, String appId) {
        FeatureFlippingDocument document = getOrCreateDocument(computeId(domain, env, appId));
        List<PropertyDocument> updatedProperties = new ArrayList<>();
        copyHistory(document);

        for (PropertyDocument oldProperty : document.getPropertyValues().values()) {
            String key = oldProperty.getKey();
            String value = oldProperty.getValue();
            PropertyDocument property = getOrCreatePropertyDocumentWithoutHistory(document, key, value);
            property.setComment(property.getComment());
            updatedProperties.add(property);
        }
        document.setPropertyValues(
                updatedProperties.stream().collect(toMap(PropertyDocument::getKey, identity())));

        repository.save(document);
    }

    private void copyHistory(FeatureFlippingDocument document) {
        List<FeatureFlippingHistoryDocument> newHistories = document.getPropertyValues().entrySet().stream()
                .map(entry -> entry.getValue().getHistory().stream()
                        .map(propertyHistoryDocument -> new FeatureFlippingHistoryDocument(computeHistoryId(document.getId(), entry.getKey()), System.currentTimeMillis(), propertyHistoryDocument))
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

    private FeatureFlippingDocument getOrCreateDocument(String id) {
        return repository
                .findById(id)
                .orElse(FeatureFlippingDocument.builder().id(id).propertyValues(new HashMap<>()).build());
    }

    private PropertyDocument getOrCreatePropertyDocument(
            FeatureFlippingDocument document, String property, String value, String username) {
        PropertyDocument propertyDocument = getPropertyDocument(document, property, value);
        addEntryInPropertyHistory(propertyDocument, value, username);
        propertyDocument.setValue(value);

        return propertyDocument;
    }

    private PropertyDocument getOrCreatePropertyDocumentWithoutHistory(
            FeatureFlippingDocument document, String property, String value) {
        PropertyDocument propertyDocument = getPropertyDocument(document, property, value);
        propertyDocument.setValue(value);
        propertyDocument.setHistory(Collections.emptyList());

        return propertyDocument;
    }

    private PropertyDocument getPropertyDocument(FeatureFlippingDocument document, String property, String value) {
        PropertyDocument propertyDocument =
                document
                        .getPropertyValues()
                        .computeIfAbsent(property, p -> PropertyDocument.builder().key(property).build());
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

    private Collection<PropertyDocument> getDocumentProperties(FeatureFlippingDocument document) {
        return Optional.ofNullable(document.getPropertyValues())
                .orElse(Collections.emptyMap())
                .values();
    }
}
