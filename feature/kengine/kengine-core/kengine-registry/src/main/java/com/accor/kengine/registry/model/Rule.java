package com.accor.kengine.registry.model;

import com.accor.kengine.DocumentationDetails;
import com.accor.kengine.RuleNode;

public interface Rule<T> {

  String getId();

  int getOrder();

  DocumentationDetails getDetails();

  RuleNode<T> getRuleNode();
}
