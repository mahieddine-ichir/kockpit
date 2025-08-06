package com.accor.wcp.sample.audit;

import static com.accor.wcp.sample.sampleflow.AppFlowDocumentation.FLOW_SIMPLE_FLOW;

import com.accor.wcp.audit.AuditedDelegateExecutorService;
import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.flow.FlowRunner;
import com.accor.wcp.sample.sampleflow.flow.context.SimpleContext;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ParallelFlowAuditController {

  private final ExecutorService executorService;

  private final FlowRunner flowRunner;

  public ParallelFlowAuditController(@Lazy FlowRunner flowRunner) {
    this.flowRunner = flowRunner;
    executorService = new AuditedDelegateExecutorService(Executors.newFixedThreadPool(6));
  }

  @GetMapping(value = "/audit/multithread/sayMultiHello/{name}/{times}")
  public ResponseEntity<List<String>> home(
      @PathVariable("name") @NotBlank String name, @PathVariable("times") int times) {

    List<Future<String>> futures =
        IntStream.range(0, times).mapToObj(i -> this.futureExecute(name, i)).toList();
    List<String> results = futures.stream().map(this::getResult).toList();

    return ResponseEntity.ok(results);
  }

  private String getResult(Future<String> stringFuture) {
    try {
      return stringFuture.get();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private Future<String> futureExecute(String name, int i) {
    return executorService.submit(
        () -> {
          FlowContextContainer contextContainer = new FlowContextContainer();

          String nameAndThread = name + "-" + Thread.currentThread().getName();

          SimpleContext simpleContext =
              SimpleContext.builder().input(nameAndThread).number(i).build();
          contextContainer.setContext(SimpleContext.class, simpleContext);

          flowRunner.execute(FLOW_SIMPLE_FLOW, contextContainer);

          return simpleContext.getResult();
        });
  }
}
