package com.kockpit.rules.codegen.plugin.rendering;

import com.kockpit.rules.codegen.plugin.ClassNameSanitizer;
import com.kockpit.rules.codegen.plugin.StepClassNameFormatter;
import com.kockpit.rules.codegen.plugin.model.Step;

import java.util.Objects;

public abstract class BaseRenderer implements StepRenderer {

    protected final StepRendererFactory rendererFactory = new StepRendererFactory();

    protected final ClassNameSanitizer classNameSanitizer = new ClassNameSanitizer();

    protected final StepClassNameFormatter stepClassNameFormatter = new StepClassNameFormatter();

    protected final String getDocumentation(Step step) {
        String doc = step.getDescription();
        if (Objects.isNull(doc)) {
            doc = step.getName();
        }
        return doc;
    }

    protected String firstLower(String string) {
        return string.substring(0, 1).toLowerCase() + string.substring(1);
    }
}
