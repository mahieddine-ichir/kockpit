package org.kockpit.rules;

import lombok.Getter;

import java.util.function.Predicate;

@Getter
public class RulePredicate<T> {
  private Predicate<T> predicate;
  private final DocumentationDetails details;

  public RulePredicate(DocumentationDetails details) {
    this.details = details;
  }

  public RulePredicate(Predicate<T> predicate) {
    this.predicate = predicate;
    this.details = null;
  }

  public RulePredicate(Predicate<T> predicate, DocumentationDetails details) {
    this.predicate = predicate;
    this.details = details;
  }

}
