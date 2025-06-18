package com.kockpit.rules.codegen.plugin.rendering;

import com.kockpit.rules.codegen.plugin.model.Step;

import java.util.stream.Collectors;

public class PredicateRenderer extends BaseRenderer {

    @Override
    public String render(Step predicate) {
        String sanitized = firstLower(stepClassNameFormatter.formatClassName(predicate));
        StringBuilder when = new StringBuilder();
        when.append("when(%s, %sDocumentation)".formatted(sanitized, sanitized));
        if (predicate.get_true() != null && !predicate.get_true().isEmpty()) {
            when.append("\r\t.then(");
            when.append(predicate.get_true().stream()
                    .map(step -> rendererFactory.getRenderer(step).render(step))
                    .collect(Collectors.joining(".")));
            when.append(")");
        }

        if (predicate.get_false() != null && !predicate.get_false().isEmpty()) {
            when.append("\r\t.otherwise(");
            when.append(predicate.get_false().stream()
                    .map(step -> rendererFactory.getRenderer(step).render(step))
                    .collect(Collectors.joining(".")));
            when.append(")");
        }
        return when.toString();
    }
}
