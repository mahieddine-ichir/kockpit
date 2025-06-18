package com.kockpit.samples.impl;

import com.kockpit.rules.seemless.Predicate;
import com.kockpit.samples.rules.My_predicate2Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class My_predicate2ActionImpl implements My_predicate2Predicate {

    @Predicate
    boolean doSomething() {
        log.info("My_predicate2ActionImpl.doSomething()");
        return true;
    }
}
