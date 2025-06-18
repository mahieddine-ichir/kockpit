package com.kockpit.samples.impl;

import com.kockpit.rules.seemless.Action;
import com.kockpit.samples.rules.My_action21Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class My_action21ActionImpl implements My_action21Action {

    @Action
    public void doSomething() {
        log.info("My_action21ActionImpl.doSomething()");
    }
}
