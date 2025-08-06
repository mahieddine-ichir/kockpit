package com.accor.wcp.sample.sampleflow.flow.rule.action;

import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.sample.sampleflow.flow.context.OperationContext;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class IsValuesValid implements Predicate<FlowContextContainer> {
    @Override
    public boolean test(FlowContextContainer flowContext) {
        OperationContext context = flowContext.getContext(OperationContext.class);

        return isNumeric(context.getInputA()) && isNumeric(context.getInputB());
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
