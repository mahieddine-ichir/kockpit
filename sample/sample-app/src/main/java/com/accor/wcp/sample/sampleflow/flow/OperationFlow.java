package com.accor.wcp.sample.sampleflow.flow;

import com.accor.kengine.DocumentationDetails;
import com.accor.kengine.registry.model.Flow;
import com.accor.kengine.registry.model.FlowEntry;
import com.accor.wcp.flow.DefaultFlowEntry;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.accor.wcp.sample.sampleflow.AppFlowDocumentation.FLOW_OPERATION_FLOW;
import static com.accor.wcp.sample.sampleflow.flow.rule.BusinessRule.BR_OPERATION_RULE;

@Component
public class OperationFlow implements Flow {
    @Override
    public String getId() {
        return FLOW_OPERATION_FLOW.name();
    }

    @Override
    public DocumentationDetails getDetails() {
        return FLOW_OPERATION_FLOW;
    }

    @Override
    public List<FlowEntry> getEntries() {
        return Arrays.asList(
                new DefaultFlowEntry(BR_OPERATION_RULE.name())
        );
    }
}
