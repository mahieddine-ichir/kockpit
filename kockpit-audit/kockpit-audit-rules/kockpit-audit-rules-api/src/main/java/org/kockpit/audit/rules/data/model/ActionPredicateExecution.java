package org.kockpit.audit.rules.data.model;

public interface ActionPredicateExecution extends CommonExecution {

  long getId();

  TypeAP getTypeAP();

  String getCode();

  String getName();

  int getPosition();

  //    RuleExecution getRuleExecution();

}
