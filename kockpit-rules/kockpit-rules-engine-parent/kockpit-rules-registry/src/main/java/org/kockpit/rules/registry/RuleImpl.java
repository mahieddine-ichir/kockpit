package org.kockpit.rules.registry;

import org.kockpit.rules.DocumentationDetails;
import org.kockpit.rules.RuleNode;
import lombok.Getter;
import org.kockpit.rules.registry.model.Rule;

public class RuleImpl<T> implements Rule<T> {

  private final String id;

  private final int order;

  private DocumentationDetails details;

  private final RuleNode<T> ruleNode;

  @Getter
  private Class sourceClass;

  public RuleImpl(String id, int order, DocumentationDetails details, RuleNode ruleNode) {
    this.id = id;
    this.order = order;
    this.details = details;
    this.ruleNode = ruleNode;
  }

  public RuleImpl(
      String id, int order, DocumentationDetails details, RuleNode ruleNode, Class sourceClass) {
    this.id = id;
    this.order = order;
    this.details = details;
    this.ruleNode = ruleNode;
    this.sourceClass = sourceClass;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public int getOrder() {
    return order;
  }

  @Override
  public DocumentationDetails getDetails() {
    return details;
  }

  @Override
  public RuleNode<T> getRuleNode() {
    return ruleNode;
  }

    @Override
  public String toString() {
    return "Rule{"
        + "id='"
        + id
        + '\''
        + ", order="
        + order
        + ", details="
        + details
        + ", ruleNode="
        + ruleNode
        + '}';
  }
}
