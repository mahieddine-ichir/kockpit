package com.kockpit.rules.registry;

import com.kockpit.rules.DocumentationDetails;
import com.kockpit.rules.RuleNode;

public abstract class RuleNodesBuilderSupport<T> {

  private final String id;

  private DocumentationDetails details;

  protected RuleNodesBuilderSupport(DocumentationDetails rule) {
    this.id = rule.getCode();
    this.details = rule;
  }

  protected RuleNodesBuilderSupport() {
    id = this.getClass().getCanonicalName();
  }

  public abstract RuleNode<T> configure() throws Exception;

  public String getId() {
    return id;
  }

  public DocumentationDetails getDetails() {
    return details;
  }

  public void setDetails(DocumentationDetails details) {
    this.details = details;
  }

  @Override
  public String toString() {
    return "RuleNodesBuilderSupport{" + "id='" + id + '\'' + ", details=" + details + '}';
  }
}
