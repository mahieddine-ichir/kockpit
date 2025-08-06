package com.accor.wcp.sample.sampleflow;

import static com.accor.wcp.sample.sampleflow.AppFlowDocumentation.FLOW_OPERATION_FLOW;
import static com.accor.wcp.sample.sampleflow.flow.context.SampleInfo.SAMPLES_INFO_CACHE;

import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.flow.FlowRunner;
import com.accor.wcp.sample.sampleflow.flow.context.OperationContext;
import jakarta.validation.constraints.NotBlank;
import javax.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class OperationFlowController {
  private final FlowRunner flowRunner;

  private CacheManager cacheManager;

  public OperationFlowController(@Lazy FlowRunner flowRunner, CacheManager cacheManager) {

    this.flowRunner = flowRunner;
    this.cacheManager = cacheManager;
  }

  @GetMapping(value = "/operation/{op}/{a}/{b}")
  public ResponseEntity<String> home(
      @PathVariable("op") @NotBlank String op,
      @PathVariable("a") String a,
      @PathVariable("b") String b) {
    return ResponseEntity.ok(execute(op, a, b));
  }

  @GetMapping(value = "/cacheMiss")
  public ResponseEntity<String> cacheMiss() {
    cacheManager.getCache(SAMPLES_INFO_CACHE).get("cacheMiss");
    return ResponseEntity.ok("cache miss");
  }

  private String execute(String op, String a, String b) {
    FlowContextContainer contextContainer = new FlowContextContainer();

    OperationContext operationContext =
        OperationContext.builder().inputOp(op).inputA(a).inputB(b).build();
    contextContainer.setContext(OperationContext.class, operationContext);

    flowRunner.execute(FLOW_OPERATION_FLOW, contextContainer);

    return operationContext.getResult();
  }
}
