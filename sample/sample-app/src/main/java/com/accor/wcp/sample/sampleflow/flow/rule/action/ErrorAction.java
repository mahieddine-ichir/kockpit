package com.accor.wcp.sample.sampleflow.flow.rule.action;

import com.accor.kengine.Action;
import com.accor.kengine.DocumentationDetails;
import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.sample.sampleflow.flow.context.ErrorContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ErrorAction  implements Action<FlowContextContainer> {
    @Override
    public void execute(FlowContextContainer flowContext) throws Exception {
        ErrorContext context = flowContext.getContext(ErrorContext.class);
        if(context.getTypeError() == ErrorContext.TypeError.ACTION){
            List list = new ArrayList<>();
            list.get(5);
        }
    }

    @Override
    public DocumentationDetails getDetails() {
        return Action.super.getDetails();
    }
}
