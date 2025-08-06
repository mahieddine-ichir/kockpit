package com.accor.kengine.registry;

import com.accor.kengine.DocumentationDetails;
import com.accor.kengine.RuleNode;
import com.accor.kengine.registry.model.Rule;

public class RuleImpl<T> implements Rule<T> {

  private String id;

  private int order;

  private DocumentationDetails details;

  private RuleNode<T> ruleNode;

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

  public Class getSourceClass() {
    return sourceClass;
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
