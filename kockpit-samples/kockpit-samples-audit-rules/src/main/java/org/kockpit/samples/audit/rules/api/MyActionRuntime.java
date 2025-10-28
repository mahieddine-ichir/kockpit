package org.kockpit.samples.audit.rules.api;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.rules.seemless.Action;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MyActionRuntime {

    @Action
    void computeStatus() {
        boolean error = Math.random() < .5;
        if (error) {
            throw new RuntimeException("Action in error");
        }
    }
}
