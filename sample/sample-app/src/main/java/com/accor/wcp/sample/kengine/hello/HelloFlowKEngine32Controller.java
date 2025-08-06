package com.accor.wcp.sample.kengine.hello;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@AllArgsConstructor
public class HelloFlowKEngine32Controller {

  private final HelloFlow helloFlow;

  @GetMapping(value = "/kengine/v32/hello")
  public ResponseEntity<String> hello() {
    String result = helloFlow.sayHelloToWorld(null);
    return ResponseEntity.ok(result);
  }

  @GetMapping(value = "/kengine/v32/hello-random/{name}")
  public ResponseEntity<String> helloRandom(@PathVariable("name") @NotBlank String name) {
    String result = helloFlow.sayHelloToWorldRandomly(name);
    return ResponseEntity.ok(result);
  }

  @GetMapping(
      value = "/kengine/v32/hello-composite/{name}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<HelloResultComposite> helloComposite(
      @PathVariable("name") @NotBlank String name) {
    HelloResultComposite result = helloFlow.helloWithCompositeContextResult(name);
    return ResponseEntity.ok(result);
  }

  @GetMapping(value = "/kengine/v32/hello/{name}")
  public ResponseEntity<String> helloName(@PathVariable("name") @NotBlank String name) {
    return ResponseEntity.ok(helloFlow.sayHelloToWorld(name));
  }

  @GetMapping(value = "/kengine/v32/hello-void")
  public ResponseEntity<String> helloNoResult() {
    helloFlow.helloWithNoResult();
    return ResponseEntity.ok("NoData");
  }

  @GetMapping(value = "/kengine/v32/hello-void-noaudit")
  public ResponseEntity<String> helloNoResultAndNoAudit() {
    helloFlow.helloWithNoResultAndNoAudit();
    return ResponseEntity.ok("NoData and no audit");
  }

  @GetMapping(value = "/kengine/v32/hello-fullcontext", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> helloWithFullContextResult(
      @RequestParam(required = false, defaultValue = "false") boolean audit) {
    Map<String, Object> context = helloFlow.helloWithFullContextResult(audit);
    return ResponseEntity.ok(context);
  }
}
