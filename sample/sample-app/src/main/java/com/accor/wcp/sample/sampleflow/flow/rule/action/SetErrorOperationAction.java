package com.accor.wcp.sample.sampleflow.flow.rule.action;

import com.accor.kengine.Action;
import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.sample.sampleflow.flow.context.OperationContext;
import org.springframework.stereotype.Component;

import javax.cache.CacheManager;

import static com.accor.wcp.sample.sampleflow.flow.context.SampleInfo.SAMPLES_INFO_CACHE;

@Component
public class SetErrorOperationAction implements Action<FlowContextContainer> {
    private final CacheManager cacheManager;

    public SetErrorOperationAction(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void execute(FlowContextContainer flowContext) throws Exception {
        OperationContext context = flowContext.getContext(OperationContext.class);
        String operation = context.getInputA()+" "+context.getInputOp()+" "+context.getInputB();
        cacheManager.getCache(SAMPLES_INFO_CACHE).put(operation, "Error: the operation symbol is wrong");
        context.setResult("Error: the operation symbol is wrong");
    }
}
