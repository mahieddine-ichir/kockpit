package com.kockpit.rules.codegen.plugin;

import com.kockpit.rules.codegen.plugin.model.Step;

public class StepClassNameFormatter extends BaseClassNameFormatter<Step> {

    @Override
    public String formatClassName(Step step) {
        String suffix = firstUpper(step.getType());
        return firstUpper(classNameSanitizer.sanitize(step)) + suffix;
    }
}
