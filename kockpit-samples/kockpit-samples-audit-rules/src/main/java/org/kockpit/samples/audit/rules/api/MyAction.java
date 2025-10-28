package org.kockpit.samples.audit.rules.api;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.rules.seemless.Action;
import org.kockpit.rules.seemless.ContextResult;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MyAction {

    @Action
    @ContextResult("status")
    String computeStatus() {
        log.info("executeAction ...");
        return "OK";
    }
}
