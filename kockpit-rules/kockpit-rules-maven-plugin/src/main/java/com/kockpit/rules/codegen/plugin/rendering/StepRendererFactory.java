package com.kockpit.rules.codegen.plugin.rendering;

import com.kockpit.rules.codegen.plugin.model.Step;

public class StepRendererFactory {

    public StepRenderer getRenderer(Step step) {
        return switch (step.getType()) {
            case "predicate" -> new PredicateRenderer();
            case "action" -> new ActionRenderer();
            default -> throw new IllegalStateException("Unexpected value: " + step.getType());
        };
    }
}
