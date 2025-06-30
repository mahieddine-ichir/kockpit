package org.kockpit.rules.registry;

import org.kockpit.rules.DocumentationDetails;
import org.kockpit.rules.RuleNode;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class RuleNodesBuilderSupport<T> {

  private final String id;

  @Setter
  private DocumentationDetails details;

  protected RuleNodesBuilderSupport(DocumentationDetails rule) {
    this.id = rule.getCode();
    this.details = rule;
  }

  protected RuleNodesBuilderSupport() {
    id = this.getClass().getCanonicalName();
  }

  public abstract RuleNode<T> configure() throws Exception;

  @Override
  public String toString() {
    return "RuleNodesBuilderSupport{" + "id='" + id + '\'' + ", details=" + details + '}';
  }
}
