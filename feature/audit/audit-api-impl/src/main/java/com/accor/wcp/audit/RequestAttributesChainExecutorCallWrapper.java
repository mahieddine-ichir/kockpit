package com.accor.wcp.audit;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

public class RequestAttributesChainExecutorCallWrapper implements ChainExecutorCallWrapper {

    @Override
    public void initContext(Map<Object, Object> context) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        context.put(RequestAttributes.class, requestAttributes);
    }

    @Override
    public void beforeExecution(Map<Object, Object> context) {
        RequestContextHolder.setRequestAttributes((RequestAttributes) context.get(RequestAttributes.class));
    }

    @Override
    public void releaseAfterExecution(Map<Object, Object> context) {
        RequestContextHolder.resetRequestAttributes();
    }
}
