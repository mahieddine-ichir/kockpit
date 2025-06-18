package com.kockpit.rules.codegen.plugin.rendering;

import com.kockpit.rules.codegen.plugin.model.Step;

public class ActionRenderer extends BaseRenderer {

    @Override
    public String render(Step action) {
        String sanitized = firstLower(stepClassNameFormatter.formatClassName(action));
        return "perform(%s, %sDocumentation)".formatted(firstLower(sanitized), sanitized);
    }
}
