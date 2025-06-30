package org.kockpit.rules;

import java.util.List;

public interface RuleNodeDefinition<T> {

  DetailsDefinition getDetails();

  List<? extends PredicateDefinition<T>> getPredicates();

  RuleNodeDefinition<T> getOk();

  RuleNodeDefinition<T> getKo();

  RuleNodeDefinition<T> getLastly();

  List<Action<T>> getActions();

  PredicateDefinition<T> getEligibilityPredicate();
}
