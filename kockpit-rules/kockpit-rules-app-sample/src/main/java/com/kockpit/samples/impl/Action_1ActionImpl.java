package com.kockpit.samples.impl;

import com.kockpit.rules.seemless.Action;
import com.kockpit.samples.rules.Action_1Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Action_1ActionImpl implements Action_1Action {

    @Action
    void doSomething() {
        log.info("Action_1ActionImpl.doSomething()");
    }
}
