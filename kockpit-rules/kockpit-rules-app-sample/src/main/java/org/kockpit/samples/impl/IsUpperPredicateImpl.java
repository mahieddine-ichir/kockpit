package org.kockpit.samples.impl;

import org.kockpit.rules.seemless.Predicate;
import org.kockpit.samples.rules.Is_upperPredicate;
import org.springframework.stereotype.Component;

@Component
public class IsUpperPredicateImpl implements Is_upperPredicate {

    @Predicate
    boolean isUpper(boolean upper) {
        return upper;
    }
}
