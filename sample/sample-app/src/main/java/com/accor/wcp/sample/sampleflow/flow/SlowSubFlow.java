package com.accor.wcp.sample.sampleflow.flow;

import com.accor.kengine.registry.model.Flow;
import com.accor.kengine.registry.model.FlowEntry;
import com.accor.wcp.flow.DefaultFlowEntry;
import com.accor.wcp.sample.sampleflow.AppFlowDocumentation;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.accor.wcp.sample.sampleflow.AppFlowDocumentation.FLOW_SUBFLOW_SLOW;
import static com.accor.wcp.sample.sampleflow.flow.rule.BusinessRule.BR_SLOW_RULE;

@Component
public class SlowSubFlow implements Flow {

    @Override
    public String getId() {
        return getDetails().name();
    }

    @Override
    public AppFlowDocumentation getDetails() {
        return FLOW_SUBFLOW_SLOW;
    }

    @Override
    public List<FlowEntry> getEntries() {
        return Arrays.asList(
                new DefaultFlowEntry(BR_SLOW_RULE.name())
        );
    }
}
