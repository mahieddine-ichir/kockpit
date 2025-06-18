package com.kockpit.samples;

import com.kockpit.rules.seemless.ContextResult;
import com.kockpit.rules.seemless.Flow;
import com.kockpit.samples.rules.MyRuleRule;

@Flow(
        id = "MY_FLOW",
        documentation = "Hello World flow!",
        ruleClasses = {
                MyRuleRule.class
        }
)
public interface MyFlow {

    @ContextResult("upper")
    String execute(String name);
}
