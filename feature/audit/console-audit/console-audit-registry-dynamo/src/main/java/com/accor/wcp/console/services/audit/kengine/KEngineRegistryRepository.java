package com.accor.wcp.console.services.audit.kengine;

import static java.util.Objects.isNull;

import com.accor.wcp.console.services.audit.kengine.dynamodb.KEngineRegistryDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KEngineRegistryRepository {

  private final KEngineRegistryDocumentRepository kEngineRegistryDocumentRepository;
  private final Map<String, KEngineRegistryDocument> documentMapCache = new HashMap<>();
  private ObjectMapper objectMapper;

  @PostConstruct
  void init() {
    objectMapper = new ObjectMapper();
  }

  public KEngineRegistryDocument getKEngineRegistryDocument(String referentialId) {
    // Already exists (and loaded)
    if (documentMapCache.containsKey(referentialId)) {
      return documentMapCache.get(referentialId);
    }

    // Put in memory
    Optional<KEngineRegistryDocument> found =
        kEngineRegistryDocumentRepository.findById(referentialId);
    if (found.isPresent()) {
      KEngineRegistryDocument kEngineRegistryDocument = found.get();
      documentMapCache.put(referentialId, kEngineRegistryDocument);
      return kEngineRegistryDocument;
    }

    return null;
  }

  public Map<String, Object> getReferential(String referentialId) throws IOException {
    KEngineRegistryDocument document = getKEngineRegistryDocument(referentialId);
    if (isNull(document)) {
      return null;
    }
    return objectMapper.readValue(document.getJsonValue(), Map.class);
  }
}
