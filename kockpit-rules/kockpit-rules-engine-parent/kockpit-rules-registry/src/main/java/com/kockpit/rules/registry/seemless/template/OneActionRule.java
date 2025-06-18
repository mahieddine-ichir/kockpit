package com.kockpit.rules.registry.seemless.template;

import com.kockpit.rules.DocumentationDetails;
import com.kockpit.rules.RuleNode;
import com.kockpit.rules.registry.seemless.RuleNodesBuilderSeamLessSupport;

import static com.kockpit.rules.seemless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.define;

/**
 * Simple structure for simple rule with only 1 action. Just extends it and implement your action in
 * the way you want.
 */
public abstract class OneActionRule extends RuleNodesBuilderSeamLessSupport {

  private final DocumentationDetails ruleDetails;
  private final DocumentationDetails actionDetails;
  private final Object action;

  protected OneActionRule(DocumentationDetails sameRuleAndActionDetails) {
    this.ruleDetails = sameRuleAndActionDetails;
    this.actionDetails = sameRuleAndActionDetails;
    this.action = this;
  }

  protected OneActionRule(DocumentationDetails sameRuleAndActionDetails, Object action) {
    this.ruleDetails = sameRuleAndActionDetails;
    this.actionDetails = sameRuleAndActionDetails;
    this.action = action;
  }

  protected OneActionRule(DocumentationDetails ruleDetails, DocumentationDetails actionDetails) {
    this.ruleDetails = ruleDetails;
    this.actionDetails = actionDetails;
    this.action = this;
  }

  @Override
  public RuleNode configure() {
    return define(ruleDetails).perform(action, actionDetails).end();
  }
}
