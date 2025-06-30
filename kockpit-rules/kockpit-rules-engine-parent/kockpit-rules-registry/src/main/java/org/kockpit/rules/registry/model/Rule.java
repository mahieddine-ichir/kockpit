package org.kockpit.rules.registry.model;

import org.kockpit.rules.DocumentationDetails;
import org.kockpit.rules.RuleNode;

public interface Rule<T> {

  String getId();

  int getOrder();

  DocumentationDetails getDetails();

  RuleNode<T> getRuleNode();
}
