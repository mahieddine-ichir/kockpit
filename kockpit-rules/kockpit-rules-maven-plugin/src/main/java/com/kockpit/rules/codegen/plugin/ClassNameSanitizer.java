package com.kockpit.rules.codegen.plugin;

import com.kockpit.rules.codegen.plugin.model.RuleDefinition;
import com.kockpit.rules.codegen.plugin.model.Step;

public class ClassNameSanitizer {

    String sanitize(Step step) {
        return this.sanitize(step.getName());
    }

    String sanitize(RuleDefinition step) {
        return this.sanitize(step.getName());
    }

    private String sanitize(String stepName) {
        return stepName.replaceAll("[-|\\s]", "_");
    }
}
