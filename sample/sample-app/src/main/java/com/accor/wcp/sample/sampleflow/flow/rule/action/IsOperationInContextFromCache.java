package com.accor.wcp.sample.sampleflow.flow.rule.action;


import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.sample.sampleflow.flow.context.OperationContext;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

import static java.util.Objects.nonNull;

@Component
public class IsOperationInContextFromCache implements Predicate<FlowContextContainer> {

    @Override
    public boolean test(FlowContextContainer flowContext) {
        OperationContext context = flowContext.getContext(OperationContext.class);
        return nonNull(context.getResult());
    }
}
