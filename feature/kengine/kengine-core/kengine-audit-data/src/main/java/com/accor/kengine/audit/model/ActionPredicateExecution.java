package com.accor.kengine.audit.model;

public interface ActionPredicateExecution extends CommonExecution {

  long getId();

  TypeAP getTypeAP();

  String getCode();

  String getName();

  int getPosition();

  //    RuleExecution getRuleExecution();

}
