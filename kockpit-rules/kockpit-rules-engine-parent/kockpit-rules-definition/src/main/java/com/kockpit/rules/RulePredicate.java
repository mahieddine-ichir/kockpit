package com.kockpit.rules;

import java.util.function.Predicate;

public class RulePredicate<T> {
  private Predicate<T> predicate;
  private DocumentationDetails details;

  public RulePredicate(DocumentationDetails details) {
    this.details = details;
  }

  @Deprecated
  public RulePredicate(Predicate<T> predicate) {
    this.predicate = predicate;
    this.details = null;
  }

  public RulePredicate(Predicate<T> predicate, DocumentationDetails details) {
    this.predicate = predicate;
    this.details = details;
  }

  public Predicate<T> getPredicate() {
    return predicate;
  }

  public DocumentationDetails getDetails() {
    return details;
  }
}
