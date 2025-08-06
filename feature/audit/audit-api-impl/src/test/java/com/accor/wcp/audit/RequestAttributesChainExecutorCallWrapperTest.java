package com.accor.wcp.audit;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class RequestAttributesChainExecutorCallWrapperTest {

    private RequestAttributesChainExecutorCallWrapper underTest = new RequestAttributesChainExecutorCallWrapper();

    @Test
    void should_inherit_request_attributes() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        WrapDelegateExecutorService wrapDelegateExecutorService = new WrapDelegateExecutorService(executorService, underTest);
        RequestAttributes attributes = Mockito.mock(RequestAttributes.class);
        when(attributes.getAttribute("att1", RequestAttributes.SCOPE_REQUEST)).thenReturn("value1");
        RequestContextHolder.setRequestAttributes(attributes);

        wrapDelegateExecutorService.execute(() -> {
            Object att1 = RequestContextHolder.getRequestAttributes().getAttribute("att1", RequestAttributes.SCOPE_REQUEST);
            assertNotNull(att1);
        });

    }

}