package com.kockpit.rules.codegen.plugin;

import com.kockpit.rules.codegen.plugin.model.Step;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StepClassNameFormatterTest {

    @Test
    void formatClassName() {
        Step step = new Step();
        step.setType("predicate");
        step.setName("Hello");
        String s = new StepClassNameFormatter().formatClassName(step);

        Assertions.assertEquals("HelloPredicate", s);
    }

    @Test
    void formatClassName_name_lowercase() {
        Step step = new Step();
        step.setType("predicate");
        step.setName("hello");
        String s = new StepClassNameFormatter().formatClassName(step);

        Assertions.assertEquals("HelloPredicate", s);
    }

}