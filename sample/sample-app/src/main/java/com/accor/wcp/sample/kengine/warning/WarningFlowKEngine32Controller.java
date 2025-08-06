package com.accor.wcp.sample.kengine.warning;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@AllArgsConstructor
public class WarningFlowKEngine32Controller {

  private final WarningFlow warningFlow;

  @GetMapping(value = "/kengine/v32/warning")
  public ResponseEntity<Boolean> exampleWarningFlow() {
    FlowResult flowResult = warningFlow.flowWithWarning();
    return ResponseEntity.ok(flowResult.getFlowExecutionResult().isWarning());
  }
}
