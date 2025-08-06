package com.accor.wcp.sample.sampleflow;

import static com.accor.wcp.sample.sampleflow.AppFlowDocumentation.FLOW_CALLER_FLOW;
import static com.accor.wcp.sample.sampleflow.AppFlowDocumentation.FLOW_SIMPLE_FLOW;

import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.flow.FlowRunner;
import com.accor.wcp.sample.sampleflow.flow.context.SimpleContext;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class HelloFlowController {

  private final FlowRunner flowRunner;

  public HelloFlowController(@Lazy FlowRunner flowRunner) {
    this.flowRunner = flowRunner;
  }

  @GetMapping(value = "/flow/caller")
  public ResponseEntity<String> flowCaller() {
    FlowContextContainer contextContainer = new FlowContextContainer();

    SimpleContext simpleContext =
        SimpleContext.builder().input("caller").number(new Random().nextInt()).build();
    contextContainer.setContext(SimpleContext.class, simpleContext);

    flowRunner.execute(FLOW_CALLER_FLOW, contextContainer, "MainCaller");

    return ResponseEntity.ok(simpleContext.getResult());
  }

  @GetMapping(value = "/sayHello/{name}")
  public ResponseEntity<String> home(@PathVariable("name") @NotBlank String name) {
    return ResponseEntity.ok(execute(name));
  }

  @GetMapping(value = "/sayMultiHello/{name}/{times}")
  public ResponseEntity<List<String>> home(
      @PathVariable("name") @NotBlank String name, @PathVariable("times") int times) {
    List<String> results = new ArrayList<>(times);
    for (int i = 0; i < times; i++) {
      results.add(execute(name + "-" + i));
    }
    return ResponseEntity.ok(results);
  }

  private String execute(String name) {
    FlowContextContainer contextContainer = new FlowContextContainer();

    SimpleContext simpleContext =
        SimpleContext.builder().input(name).number(new Random().nextInt()).build();
    contextContainer.setContext(SimpleContext.class, simpleContext);

    flowRunner.execute(FLOW_SIMPLE_FLOW, contextContainer);

    return simpleContext.getResult();
  }
}
