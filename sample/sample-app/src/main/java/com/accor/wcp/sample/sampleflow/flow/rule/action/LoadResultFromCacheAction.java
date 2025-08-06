package com.accor.wcp.sample.sampleflow.flow.rule.action;

import com.accor.kengine.Action;
import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.sample.sampleflow.flow.context.OperationContext;
import org.springframework.stereotype.Component;

import javax.cache.CacheManager;

import static com.accor.wcp.sample.sampleflow.flow.context.SampleInfo.SAMPLES_INFO_CACHE;
import static java.util.Objects.isNull;

@Component
public class LoadResultFromCacheAction implements Action<FlowContextContainer> {

    private final CacheManager cacheManager;

    public LoadResultFromCacheAction(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void execute(FlowContextContainer flowContext) throws Exception {
        OperationContext context = flowContext.getContext(OperationContext.class);
        String result = (String) cacheManager.getCache(SAMPLES_INFO_CACHE).get(context.getInputA()+ " "+context.getInputOp()+" "+context.getInputB());
        context.setResult(isNull(result) ? null : result +" from cache");
    }
}
