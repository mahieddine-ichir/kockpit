package com.kockpit.rules.codegen.plugin;

import com.kockpit.rules.codegen.plugin.model.RuleDefinition;

public class RuleClassNameFormatter extends BaseClassNameFormatter<RuleDefinition> {

    @Override
    public String formatClassName(RuleDefinition rule) {
        String sanitized = classNameSanitizer.sanitize(rule);
        return firstUpper(sanitized) + "Rule";
    }
}
