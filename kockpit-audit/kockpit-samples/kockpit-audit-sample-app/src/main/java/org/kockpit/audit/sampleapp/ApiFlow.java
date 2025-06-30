package org.kockpit.audit.sampleapp;

import org.kockpit.rules.seemless.ContextResult;
import org.kockpit.rules.seemless.Flow;

@Flow(
        id = "API_FLOW",
        documentation = "Api Flow",
        ruleClasses = {
                ApiRule.class
        }
)
public interface ApiFlow {

    @ContextResult("output")
    String perform(String name);
}
