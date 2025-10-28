package org.kockpit.samples.audit.rules.api;

import lombok.RequiredArgsConstructor;
import org.kockpit.rules.DefaultDocumentationDetails;
import org.kockpit.rules.RuleNode;
import org.kockpit.rules.registry.seemless.RuleNodesBuilderSeamLessSupport;
import org.kockpit.rules.seemless.Rule;

import static org.kockpit.rules.seemless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.define;

@Rule
@RequiredArgsConstructor
public class MyRule extends RuleNodesBuilderSeamLessSupport {

    private final MyAction myAction;

    private final MyActionRuntime myActionRuntime;

    @Override
    public RuleNode<?> configure() {
        return define("myRule", "myRule description")
                .perform(myAction, new DefaultDocumentationDetails("myAction", "myAction doc"))
                .perform(myActionRuntime, new DefaultDocumentationDetails("myActionRuntime", "myAction runtime doc"))
                .end();
    }
}
