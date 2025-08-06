package com.accor.kengine.seamless;

import com.accor.kengine.DefaultDocumentationDetails;
import com.accor.kengine.DocumentationDetails;

public class ConditionalRuleNodeSeamLessBuilder
    extends RuleNodeSeamLessBuilder<ConditionalRuleNodeSeamLessBuilder> {

  ConditionalRuleNodeSeamLessBuilder() {}

  ConditionalRuleNodeSeamLessBuilder(DocumentationDetails details) {
    super(details);
  }

  public static final class StaticBuilder {

    private StaticBuilder() {
      // No usage
    }

    public static ConditionalRuleNodeSeamLessBuilder define(String code, String documentation) {
      return define(new DefaultDocumentationDetails(code, documentation));
    }

    public static ConditionalRuleNodeSeamLessBuilder define(DocumentationDetails details) {
      ConditionalRuleNodeSeamLessBuilder conditionalRuleNodeSeamLessBuilder =
          new ConditionalRuleNodeSeamLessBuilder();
      conditionalRuleNodeSeamLessBuilder.setDetails(details);
      return conditionalRuleNodeSeamLessBuilder;
    }

    private static ConditionalRuleNodeSeamLessBuilder action(
        DocumentationDetails nodeDetails,
        Object action,
        Runnable apply,
        DocumentationDetails actionDetails) {
      ConditionalRuleNodeSeamLessBuilder node = new ConditionalRuleNodeSeamLessBuilder(nodeDetails);
      node.action(action, apply, actionDetails);
      node.setDetails(actionDetails);
      return node;
    }

    private static ConditionalRuleNodeSeamLessBuilder action(
        DocumentationDetails nodeDetails, Object action, DocumentationDetails actionDetails) {
      ConditionalRuleNodeSeamLessBuilder node = new ConditionalRuleNodeSeamLessBuilder(nodeDetails);
      node.action(action, actionDetails);
      return node;
    }

    public static ConditionalRuleNodeSeamLessBuilder perform(
        Object action, DocumentationDetails details) {
      return action(details, action, details);
    }

    public static ConditionalRuleNodeSeamLessBuilder perform(
        Object action, Runnable apply, DocumentationDetails details) {
      return action(details, action, apply, details);
    }

    public static ConditionalRuleNodeSeamLessBuilder perform(Object action, Runnable apply) {
      return action(null, action, apply, null);
    }

    public static ConditionalRuleNodeSeamLessBuilder perform(Object action) {
      return action(null, action, null);
    }

    public static ConditionalRuleNodeSeamLessBuilder when(
        Object predicate, DocumentationDetails details) {
      ConditionalRuleNodeSeamLessBuilder node = new ConditionalRuleNodeSeamLessBuilder();
      node.predicate(predicate, details);
      return node;
    }

    public static ConditionalRuleNodeSeamLessBuilder when(Object predicate) {
      return when(predicate, null);
    }
  }

  public ConditionalRuleNodeSeamLessBuilder when(Object predicate, DocumentationDetails details) {
    return this.internalWhen(predicate, details);
  }

  public ConditionalRuleNodeSeamLessBuilder when(Object predicate) {
    return this.internalWhen(predicate, null);
  }

  ConditionalRuleNodeSeamLessBuilder internalWhen(Object predicate, DocumentationDetails details) {
    this.predicate(predicate, details);
    return this;
  }

  public ConditionalRuleNodeSeamLessBuilder then(RuleNodeSeamLessBuilder<?> ok) {
    ok.setParent(this);
    ok.branch = "_then";
    this.ok = ok;
    return this;
  }

  public ConditionalRuleNodeSeamLessBuilder then(Object action) {
    return then(action, (DocumentationDetails) null);
  }

  public ConditionalRuleNodeSeamLessBuilder then(Object action, Runnable apply) {
    return then(StaticBuilder.perform(action, apply));
  }

  public ConditionalRuleNodeSeamLessBuilder then(
      Object action, Runnable apply, DocumentationDetails details) {
    return then(StaticBuilder.perform(action, apply, details));
  }

  public ConditionalRuleNodeSeamLessBuilder then(Object action, DocumentationDetails details) {
    return then(StaticBuilder.perform(action, details));
  }

  public ConditionalRuleNodeSeamLessBuilder perform(Object action, DocumentationDetails details) {
    this.action(action, details);
    return this;
  }

  public ConditionalRuleNodeSeamLessBuilder perform(Object action) {
    this.action(action, null);
    return this;
  }

  private ConditionalRuleNodeSeamLessBuilder internalOtherwise(RuleNodeSeamLessBuilder<?> ko) {
    ko.setParent(this);
    ko.branch = "_else";
    this.ko = ko;
    return this;
  }

  public ConditionalRuleNodeSeamLessBuilder otherwise(RuleNodeSeamLessBuilder<?> ko) {
    ConditionalRuleNodeSeamLessBuilder otherwise = internalOtherwise(ko);
    ko.branch = "_otherwise";
    return otherwise;
  }

  public ConditionalRuleNodeSeamLessBuilder otherwise(Object action) {
    return otherwise(action, null);
  }

  public ConditionalRuleNodeSeamLessBuilder otherwise(Object action, DocumentationDetails details) {
    ConditionalRuleNodeSeamLessBuilder node = StaticBuilder.action(null, action, details);
    return otherwise(node);
  }
}
