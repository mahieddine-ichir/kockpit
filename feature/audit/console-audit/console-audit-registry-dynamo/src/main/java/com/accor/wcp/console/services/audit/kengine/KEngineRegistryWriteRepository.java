package com.accor.wcp.console.services.audit.kengine;

import static com.accor.wcp.console.services.audit.kengine.KEngineRegistryDocumentRepository.generateFullReferentialId;
import static java.util.Objects.nonNull;

import com.accor.wcp.console.services.audit.kengine.dynamodb.KEngineRegistryDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class KEngineRegistryWriteRepository {

  private final KEngineRegistryDocumentRepository kEngineRegistryDocumentRepository;

  private final Map<String, KEngineRegistryDocument> documentMapCache = new HashMap<>();

  private final ObjectMapper objectMapper;

  KEngineRegistryWriteRepository(
      KEngineRegistryDocumentRepository kEngineRegistryDocumentRepository) {
    this.kEngineRegistryDocumentRepository = kEngineRegistryDocumentRepository;
    objectMapper = new ObjectMapper();
  }

  public String getReferentialId(
      String domain, String env, String applicationId, Map<String, Object> referential) {
    return getReferentialId(domain, env, applicationId, referential, null);
  }

  public String getReferentialId(
      String domain,
      String env,
      String applicationId,
      Map<String, Object> referential,
      Map<String, Object> registry) {
    String json;
    try {
      if (nonNull(registry)) {
        json = objectMapper.writeValueAsString(registry);
      } else {
        json = objectMapper.writeValueAsString(referential);
      }
    } catch (JsonProcessingException e) {
      return null;
    }

    // Compute hash
    int hashCode = json.hashCode();
    String referentialId = generateFullReferentialId(domain, env, applicationId, hashCode);

    // Already exists (and loaded)
    if (documentMapCache.containsKey(referentialId)) {
      return referentialId;
    }

    // Check existing
    if (kEngineRegistryDocumentRepository.findById(referentialId).isEmpty()) {
      // Save it
      KEngineRegistryDocument document =
          KEngineRegistryDocument.builder()
              .id(referentialId)
              .creationTimestamp(System.currentTimeMillis())
              .jsonValue(json)
              .build();
      kEngineRegistryDocumentRepository.update(document);
      documentMapCache.put(referentialId, document);
    } else {
      // Save existing in local cache
      documentMapCache.put(referentialId, null);
    }

    return referentialId;
  }
}
