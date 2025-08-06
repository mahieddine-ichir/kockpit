package com.accor.kengine.registry.seamless.action;

import com.accor.kengine.seamless.Action;

public class ActionWithAnnotation {
  @Action(documentation = "Documentation for the simple execution")
  public void simpleExecution() {
    System.out.println("Simple execution");
  }
}
