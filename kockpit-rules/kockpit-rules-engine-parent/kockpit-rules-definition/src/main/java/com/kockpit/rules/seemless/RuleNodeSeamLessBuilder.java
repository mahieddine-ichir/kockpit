package com.kockpit.rules.seemless;

import com.kockpit.rules.DefaultDocumentationDetails;
import com.kockpit.rules.DocumentationDetails;
import com.kockpit.rules.RuleNode;
import com.kockpit.rules.RulePredicate;
import com.kockpit.rules.predicate.AlwaysTruePredicate;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Objects.isNull;

public class RuleNodeSeamLessBuilder<T extends RuleNodeSeamLessBuilder> {

  protected DocumentationDetails details;

  protected RuleNodeSeamLessBuilder parent;

  protected List<RulePredicate> predicates = new LinkedList<>();

  protected RuleNodeSeamLessBuilder ok;
  protected RuleNodeSeamLessBuilder ko;
  protected RuleNodeSeamLessBuilder lastly;

  protected List<com.kockpit.rules.Action> actions = new LinkedList<>();

  protected Predicate eligibilityPredicate;

  protected String branch;

  protected RuleNodeSeamLessBuilder() {
    this(null);
  }

  protected RuleNodeSeamLessBuilder(DocumentationDetails details) {
    this.details = details;
    this.eligibilityPredicate = new AlwaysTruePredicate();
  }

  protected RuleNodeSeamLessBuilder predicate(Object predicate, DocumentationDetails details) {
    PredicateSeamLessWrapper wrapper = new PredicateSeamLessWrapper(predicate, details);
    return predicate(new RulePredicate(wrapper, wrapper.getDocumentation()));
  }

  @Deprecated
  private T predicate(RulePredicate predicate) {
    this.predicates.add(predicate);
    return (T) this;
  }

  public T lastly(RuleNodeSeamLessBuilder<? extends RuleNodeSeamLessBuilder> lastly) {
    this.lastly = lastly;
    return (T) this;
  }

  protected T action(Object action, Runnable apply, DocumentationDetails details) {
    this.actions.add(new ActionSeamLessWrapper(action, apply, details));
    return (T) this;
  }

  protected T action(Object action, DocumentationDetails details) {
    this.actions.add(new ActionSeamLessWrapper(action, details));
    return (T) this;
  }

  public RuleNode build() {
    return end();
  }

  public RuleNode end() {
    return createRuleNode();
  }

  protected RuleNode createRuleNode() {
    RuleNode ruleNodeOk = null;
    String code;
    if (isNull(details)) {
      code = "ROOT";
    } else {
      code = details.getCode();
    }
    if (ok != null) {
      ok.details = new DefaultDocumentationDetails(code + ok.branch, ok.branch);
      ruleNodeOk = ok.createRuleNode();
    }
    RuleNode ruleNodeKo = null;
    if (ko != null) {
      ko.details = new DefaultDocumentationDetails(code + ko.branch, ko.branch);
      ruleNodeKo = ko.createRuleNode();
    }
    RuleNode ruleNodeLastly = null;
    if (lastly != null) {
      lastly.details = new DefaultDocumentationDetails(code + "_lastly", "lastly");
      ruleNodeLastly = lastly.createRuleNode();
    }
    return new RuleNode(
        details, predicates, ruleNodeOk, ruleNodeKo, ruleNodeLastly, actions, eligibilityPredicate);
  }

  protected void setDetails(DocumentationDetails details) {
    this.details = details;
  }

  protected void setParent(RuleNodeSeamLessBuilder parent) {
    this.parent = parent;
  }
}
