package com.accor.wcp.sample.sampleflow.flow.rule;

import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeBuilder;
import com.accor.kengine.registry.RuleNodesBuilderSupport;
import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.sample.sampleflow.flow.rule.action.ErrorAction;
import com.accor.wcp.sample.sampleflow.flow.rule.action.ErrorPredicate;
import org.springframework.stereotype.Component;

import static com.accor.wcp.sample.sampleflow.flow.rule.BusinessRule.BR_ERROR_RULE;
import static com.accor.wcp.sample.sampleflow.flow.rule.action.AppAction.ACT_ERROR_ACTION;
import static com.accor.wcp.sample.sampleflow.flow.rule.action.AppAction.PRE_ERROR_PREDICATE;

@Component
public class ErrorRule extends RuleNodesBuilderSupport<FlowContextContainer> {

    private ErrorAction errorAction;
    private ErrorPredicate errorPredicate;
    public ErrorRule(ErrorAction errorAction, ErrorPredicate errorPredicate){
        super(BR_ERROR_RULE);
        this.errorAction = errorAction;
        this.errorPredicate = errorPredicate;
    }

    @Override
    public RuleNode<FlowContextContainer> configure() throws Exception {
        RuleNodeBuilder<FlowContextContainer> builder = new RuleNodeBuilder<>(BR_ERROR_RULE);
        return builder
                .action(errorAction, ACT_ERROR_ACTION)
                .predicate(errorPredicate, PRE_ERROR_PREDICATE)
                    .ok()
                    .done()
                    .ko()
                    .done()
                .createRuleNode();
    }
}
