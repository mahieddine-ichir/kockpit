package com.kockpit.samples.impl;

import com.kockpit.rules.seemless.Predicate;
import com.kockpit.samples.rules.My_predicatePredicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class My_predicatePredicateImpl implements My_predicatePredicate {

    @Predicate
    boolean doSomething() {
        log.info("My_predicatePredicateImpl.doSomething()");
        return false;
    }
}
