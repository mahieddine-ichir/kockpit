package com.accor.wcp.sample.sampleflow;

import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.flow.FlowRunner;
import com.accor.wcp.sample.sampleflow.flow.context.ErrorContext;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Lazy;

import static com.accor.wcp.sample.sampleflow.AppFlowDocumentation.FLOW_ERROR_FLOW;

@RestController
@Validated
public class ErrorFlowController {
    private final FlowRunner flowRunner;

    public ErrorFlowController(@Lazy FlowRunner flowRunner) {
        this.flowRunner = flowRunner;
    }

    @GetMapping(value = "/errorAction")
    public ResponseEntity<String> errorAction() {
        return ResponseEntity.ok(execute(ErrorContext.TypeError.ACTION));
    }

    @GetMapping(value = "/errorPredicate")
    public ResponseEntity<String> errorPredicate() {
        return ResponseEntity.ok(execute(ErrorContext.TypeError.PREDICATE));
    }

    private String execute(ErrorContext.TypeError typeError) {
        FlowContextContainer contextContainer = new FlowContextContainer();

        ErrorContext errorContext= ErrorContext.builder().typeError(typeError).build();
        contextContainer.setContext(ErrorContext.class, errorContext);

        flowRunner.execute(FLOW_ERROR_FLOW, contextContainer);

        return "error flow";
    }

}
