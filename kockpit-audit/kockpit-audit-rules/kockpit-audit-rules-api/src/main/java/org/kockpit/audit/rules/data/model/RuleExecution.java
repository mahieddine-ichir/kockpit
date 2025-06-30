package org.kockpit.audit.rules.data.model;

import java.util.List;

public interface RuleExecution extends CommonExecution {

  long getId();

  String getCode();

  String getName();

  //    Execution getExecution();

  List<? extends ActionPredicateExecution> getActionPredicates();

  int getPosition();

  boolean isSkipped();
}
