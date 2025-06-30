package org.kockpit.rules;

import java.util.function.Predicate;

public interface PredicateDefinition<T> {

  Predicate<T> getPredicate();
}
