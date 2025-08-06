package com.accor.wcp.sample.sampleflow.flow.rule;

import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.sample.sampleflow.flow.context.SimpleContext;
import com.accor.wcp.sample.sampleflow.flow.rule.action.SimpleAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SimpleRuleTest extends AbstractRuleTest<SimpleRule> {

    @BeforeEach
    public void init() {
        SimpleAction simpleAction = new SimpleAction();
        underTest =
                new SimpleRule(simpleAction);
    }

    @Test
    void should_say_hello() {
        FlowContextContainer flowContext = new FlowContextContainer();
        SimpleContext simpleContext = SimpleContext.builder().build();
        simpleContext.setInput("Samples");
        flowContext.setContext(SimpleContext.class, simpleContext);

        executeFlow(flowContext);

        assertEquals("Hello Samples !", flowContext.getContext(SimpleContext.class).getResult());
//        verify(isAxaQuotationEnabledPredicate, times(1)).test(flowContext);
    }

}
