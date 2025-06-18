package com.kockpit.rules;

import com.kockpit.rules.action.ActionWithDocumentationWrapper;
import com.kockpit.rules.action.ExitSilentlyAction;
import com.kockpit.rules.action.ExitWithErrorAction;
import com.kockpit.rules.predicate.AlwaysTruePredicate;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class RuleNodeBuilder<T> {
  private DocumentationDetails details;
  private RuleNodeBuilder<T> parent;

  private List<RulePredicate<T>> predicates = new LinkedList<>();

  private RuleNodeBuilder<T> ok;
  private RuleNodeBuilder<T> ko;
  private RuleNodeBuilder<T> lastly;

  private List<Action<T>> actions = new LinkedList<>();

  private Predicate<T> eligibilityPredicate;

  public RuleNodeBuilder() {
    this(new DefaultDocumentationDetails("ROOT", null));
  }

  public RuleNodeBuilder(DocumentationDetails details) {
    this.details = details;
    this.eligibilityPredicate = new AlwaysTruePredicate<>();
  }

  public RuleNodeBuilder(Predicate<T> eligibilityPredicate, DocumentationDetails details) {
    this.eligibilityPredicate = eligibilityPredicate;
    this.details = details;
  }

  public RuleNodeBuilder(RuleNodeBuilder<T> parentBuilder, String branchName) {
    this.parent = parentBuilder;
    this.details = new DefaultDocumentationDetails(parentBuilder.details + branchName, null);
    this.eligibilityPredicate = new AlwaysTruePredicate<>();
  }

  public RuleNodeBuilder<T> predicates(List<RulePredicate<T>> predicates) {
    this.predicates = predicates;
    return this;
  }

  public RuleNodeBuilder<T> predicate(Predicate<T> predicate, DocumentationDetails details) {
    return predicate(new RulePredicate<>(predicate, details));
  }

  public RuleNodeBuilder<T> ifp(Predicate<T> predicate, DocumentationDetails details) {
    return predicate(new RulePredicate<>(predicate, details));
  }

  /** Use {@link #predicate(Predicate, DocumentationDetails)} instead. */
  @Deprecated
  public RuleNodeBuilder<T> predicate(RulePredicate<T> predicate) {
    this.predicates.add(predicate);
    return this;
  }

  public RuleNodeBuilder<T> ok() {
    this.ok = new RuleNodeBuilder(this, "_ok");
    return ok;
  }

  public RuleNodeBuilder<T> ko() {
    this.ko = new RuleNodeBuilder(this, "_ko");
    return ko;
  }

  public RuleNodeBuilder<T> lastly() {
    this.lastly = new RuleNodeBuilder(this, "_lastly");
    return lastly;
  }

  public RuleNodeBuilder<T> actions(List<Action<T>> actions) {
    this.actions = actions;
    return this;
  }

  private RuleNodeBuilder<T> internalAction(Consumer<T> action, DocumentationDetails details) {
    this.actions.add(
        new Action<T>() {
          @Override
          public void execute(T context) throws Exception {
            action.accept(context);
          }

          @Override
          public DocumentationDetails getDetails() {
            return details;
          }
        });
    return this;
  }

  public RuleNodeBuilder<T> action(Consumer<T> action, DocumentationDetails details) {
    return this.internalAction(action, details);
  }

  public RuleNodeBuilder<T> action(Action<T> action, DocumentationDetails details) {
    this.actions.add(new ActionWithDocumentationWrapper<>(action, details));
    return this;
  }

  public RuleNodeBuilder<T> done() {
    return this.parent;
  }

  public RuleNodeBuilder<T> details(DocumentationDetails details) {
    this.details = details;
    return this;
  }

  public RuleNode<T> createRuleNode() {
    RuleNode<T> ruleNodeOk = null;
    if (ok != null) {
      ruleNodeOk = ok.createRuleNode();
    }
    RuleNode<T> ruleNodeKo = null;
    if (ko != null) {
      ruleNodeKo = ko.createRuleNode();
    }
    RuleNode<T> ruleNodeLastly = null;
    if (lastly != null) {
      ruleNodeLastly = lastly.createRuleNode();
    }
    return new RuleNode<>(
        details, predicates, ruleNodeOk, ruleNodeKo, ruleNodeLastly, actions, eligibilityPredicate);
  }

  public RuleNodeBuilder<T> exit(String exitSilentlyMessage, DocumentationDetails details) {
    this.actions.add(new ExitSilentlyAction<>(exitSilentlyMessage, details));
    return this;
  }

  public RuleNodeBuilder<T> exit(
      Function<T, Exception> dynamicException, DocumentationDetails details) {
    this.actions.add(new ExitWithErrorAction<>(dynamicException, details));
    return this;
  }

  public RuleNodeBuilder<T> exit(Exception exception, DocumentationDetails details) {
    this.actions.add(new ExitWithErrorAction<>(t -> exception, exception.getMessage(), details));
    return this;
  }

  public RuleNodeBuilder<T> exit(
      Exception exception, String exitErrorMessage, DocumentationDetails details) {
    this.actions.add(new ExitWithErrorAction<>(t -> exception, exitErrorMessage, details));
    return this;
  }
}
