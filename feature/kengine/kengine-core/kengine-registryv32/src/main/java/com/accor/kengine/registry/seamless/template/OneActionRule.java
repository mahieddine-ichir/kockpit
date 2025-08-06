package com.accor.kengine.registry.seamless.template;

import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.define;

import com.accor.kengine.DocumentationDetails;
import com.accor.kengine.RuleNode;
import com.accor.kengine.registry.seamless.RuleNodesBuilderSeamLessSupport;

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
