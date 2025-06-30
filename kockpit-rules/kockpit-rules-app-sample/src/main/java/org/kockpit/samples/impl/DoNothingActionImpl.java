package org.kockpit.samples.impl;

import org.kockpit.rules.seemless.Action;
import org.kockpit.rules.seemless.ContextResult;
import org.kockpit.samples.rules.DoNothingAction;
import org.springframework.stereotype.Component;

@Component
public class DoNothingActionImpl implements DoNothingAction {

    @Action
    @ContextResult("output")
    String doNothing(String name) {
        return name;
    }
}
