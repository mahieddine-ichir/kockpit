package com.kockpit.rules.registry.model;

import com.kockpit.rules.DocumentationDetails;
import com.kockpit.rules.RuleNode;

public interface Rule<T> {

  String getId();

  int getOrder();

  DocumentationDetails getDetails();

  RuleNode<T> getRuleNode();
}
