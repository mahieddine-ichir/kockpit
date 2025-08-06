package com.accor.wcp.sample.sampleflow.flow.rule.action;

import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.sample.sampleflow.flow.context.ErrorContext;
import com.accor.wcp.sample.sampleflow.flow.context.OperationContext;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class ErrorPredicate implements Predicate<FlowContextContainer> {

    @Override
    public boolean test(FlowContextContainer flowContext) {
        ErrorContext context = flowContext.getContext(ErrorContext.class);
        if(context.getTypeError() == ErrorContext.TypeError.PREDICATE){
            return 4/0 > 9;
        }
        return true;
    }
}
