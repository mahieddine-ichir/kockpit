package com.accor.wcp.sample.sampleflow.flow;

import com.accor.kengine.registry.model.Flow;
import com.accor.kengine.registry.model.FlowEntry;
import com.accor.wcp.flow.DefaultFlowEntry;
import com.accor.wcp.sample.sampleflow.AppFlowDocumentation;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.accor.wcp.sample.sampleflow.AppFlowDocumentation.FLOW_CALLER_FLOW;
import static com.accor.wcp.sample.sampleflow.flow.rule.BusinessRule.BR_CALLER_RULE;
import static com.accor.wcp.sample.sampleflow.flow.rule.BusinessRule.BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE;

@Component
public class CallerFlow implements Flow {

    @Override
    public String getId() {
        return getDetails().name();
    }

    @Override
    public AppFlowDocumentation getDetails() {
        return FLOW_CALLER_FLOW;
    }

    @Override
    public List<FlowEntry> getEntries() {
        return Arrays.asList(
                new DefaultFlowEntry(BR_CALLER_RULE.name()),
                new DefaultFlowEntry(BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE.name())
        );
    }
}
