package org.kockpit.samples.audit.rules.api;

import org.kockpit.rules.seemless.ContextResult;
import org.kockpit.rules.seemless.Flow;

@Flow(
        id = "myFlow",
        documentation = "myFlow documentation",
        ruleClasses = {
                MyRule.class
        }
)
public interface MyFlow {

    @ContextResult("status")
    String status();
}
