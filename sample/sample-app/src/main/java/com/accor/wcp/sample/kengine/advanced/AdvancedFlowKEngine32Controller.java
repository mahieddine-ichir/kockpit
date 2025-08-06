package com.accor.wcp.sample.kengine.advanced;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@AllArgsConstructor
public class AdvancedFlowKEngine32Controller {

  private final AdvancedFlow advancedFlow;

  @GetMapping(value = "/kengine/v32/advanced", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<EnrichedUser> loadEnrichedUser() {
    EnrichedUser enrichedUser = advancedFlow.enrichedUser(UUID.randomUUID(), UUID.randomUUID());
    return ResponseEntity.ok(enrichedUser);
  }

}
