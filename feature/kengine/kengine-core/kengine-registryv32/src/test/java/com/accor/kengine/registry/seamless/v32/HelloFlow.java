package com.accor.kengine.registry.seamless.v32;

import com.accor.kengine.seamless.Flow;

@Flow(
    ruleIds = {"rule1", "rule2", OldCompatibilityRule.BR_OLD_EXAMPLE},
    documentation = "Hello sample flow")
public class HelloFlow {}
