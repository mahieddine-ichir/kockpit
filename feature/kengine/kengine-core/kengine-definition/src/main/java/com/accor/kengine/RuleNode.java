package com.accor.kengine;

import java.util.List;
import java.util.function.Predicate;
import lombok.Data;

@Data
public class RuleNode<T> {

  private DocumentationDetails details;

  private List<RulePredicate<T>> predicates;

  private RuleNode<T> ok;

  private RuleNode<T> ko;

  private RuleNode<T> lastly;

  private List<Action<T>> actions;

  private Predicate<T> eligibilityPredicate;

  public RuleNode(
      DocumentationDetails details,
      List<RulePredicate<T>> predicates,
      RuleNode<T> ok,
      RuleNode<T> ko,
      RuleNode<T> lastly,
      List<Action<T>> actions,
      Predicate<T> eligibilityPredicate) {
    this.details = details;
    this.predicates = predicates;
    this.ok = ok;
    this.ko = ko;
    this.lastly = lastly;
    this.actions = actions;
    this.eligibilityPredicate = eligibilityPredicate;
  }
}
