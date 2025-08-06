package com.accor.wcp.sample.sampleflow.flow.rule.action;

import com.accor.kengine.Action;
import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.sample.sampleflow.flow.context.OperationContext;
import org.springframework.stereotype.Component;

import javax.cache.CacheManager;

import static com.accor.wcp.sample.sampleflow.flow.context.SampleInfo.SAMPLES_INFO_CACHE;

@Component
public class DoOperationResult implements Action<FlowContextContainer> {
    private final CacheManager cacheManager;

    public DoOperationResult(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void execute(FlowContextContainer flowContext) throws Exception {
        OperationContext context = flowContext.getContext(OperationContext.class);
        Double a = Double.parseDouble(context.getInputA());
        Double b = Double.parseDouble(context.getInputB());
        String operation = context.getInputA()+" "+context.getInputOp()+" "+context.getInputB();
        Double result = null;
        switch (context.getInputOp())
        {
            case "+":
                result = a+b;
                break;

            case "-":
                result= a-b;
                break;

            case "*":
                result =a*b;
                break;

        }
        cacheManager.getCache(SAMPLES_INFO_CACHE).put(operation, operation+" = "+ result );
        context.setResult(operation+" = "+result);

    }
}
