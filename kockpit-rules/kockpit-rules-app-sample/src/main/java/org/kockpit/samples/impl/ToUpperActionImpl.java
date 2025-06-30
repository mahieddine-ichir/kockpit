package org.kockpit.samples.impl;

import org.kockpit.rules.seemless.Action;
import org.kockpit.rules.seemless.ContextResult;
import org.kockpit.samples.rules.ToUpperAction;
import org.springframework.stereotype.Component;

@Component
public class ToUpperActionImpl implements ToUpperAction {

    @Action
    @ContextResult("output")
    String upperCase(String name) {
        return name.toUpperCase();
    }
}
