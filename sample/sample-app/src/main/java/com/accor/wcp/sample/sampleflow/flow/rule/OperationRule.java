package com.accor.wcp.sample.sampleflow.flow.rule;

import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeBuilder;
import com.accor.kengine.registry.RuleNodesBuilderSupport;
import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.sample.sampleflow.flow.rule.action.*;
import org.springframework.stereotype.Component;

import static com.accor.wcp.sample.sampleflow.flow.rule.BusinessRule.BR_OPERATION_RULE;
import static com.accor.wcp.sample.sampleflow.flow.rule.action.AppAction.*;

@Component
public class OperationRule extends RuleNodesBuilderSupport<FlowContextContainer> {
    private final IsOperationInContextFromCache isOperationInContextFromCache;
    private final LoadResultFromCacheAction loadResultFromCacheAction;
    private final IsOperationValid isOperationValid;
    private final IsValuesValid isValuesValid;
    private final DoOperationResult doOperationResult;
    private final SetErrorOperationAction setErrorOperationAction;
    private final SetErrorValuesAction setErrorValuesAction;

    public OperationRule(IsOperationInContextFromCache isOperationInContextFromCache, LoadResultFromCacheAction loadResultFromCacheAction, IsOperationValid isOperationValid, IsValuesValid isValuesValid, DoOperationResult doOperationResult, SetErrorOperationAction setErrorOperationAction, SetErrorValuesAction setErrorValuesAction) {
        super(BR_OPERATION_RULE);
        this.isOperationInContextFromCache = isOperationInContextFromCache;
        this.loadResultFromCacheAction = loadResultFromCacheAction;
        this.isOperationValid = isOperationValid;
        this.isValuesValid = isValuesValid;
        this.doOperationResult = doOperationResult;
        this.setErrorOperationAction = setErrorOperationAction;
        this.setErrorValuesAction = setErrorValuesAction;
    }

    @Override
    public RuleNode<FlowContextContainer> configure() throws Exception {
        RuleNodeBuilder<FlowContextContainer> builder = new RuleNodeBuilder<>(BR_OPERATION_RULE);
        return builder
                .action(loadResultFromCacheAction, ACT_LOAD_FROM_CACHE_ACTION)
                .predicate(isOperationInContextFromCache, PRE_OPERATION_IN_CACHE_PREDICATE)
                    .ko()
                        .predicate(isOperationValid, PRE_IS_VALID_OPERATION_PREDICATE)
                            .ok()
                                .predicate(isValuesValid, PRE_IS_VALID_VALUES_PREDICATE)
                                    .ok()
                                        .action(doOperationResult,ACT_DO_OPERATION_ACTION)
                                    .done()
                                    .ko()
                                        .action(setErrorValuesAction, ACT_SET_ERROR_VALUES_ACTION)
                                    .done()
                            .done()
                            .ko()
                                .action(setErrorOperationAction, ACT_SET_ERROR_OPERATION_ACTION)
                            .done()
                    .done()
                .createRuleNode();
    }
}
