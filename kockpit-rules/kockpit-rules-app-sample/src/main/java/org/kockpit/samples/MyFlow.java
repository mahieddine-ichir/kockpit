package org.kockpit.samples;

import org.kockpit.rules.seemless.ContextResult;
import org.kockpit.rules.seemless.Flow;
import org.kockpit.samples.rules.MyRuleRule;

@Flow(
        id = "MY_FLOW",
        documentation = "Hello World flow!",
        ruleClasses = {
                MyRuleRule.class
        }
)
public interface MyFlow {

    @ContextResult("output")
    String execute(String name, boolean upper);
}
