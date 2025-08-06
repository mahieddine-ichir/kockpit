package com.accor.kengine;

/** Skips to next rule */
public class RuleInterruptionException extends WarningExecutionException {

  public RuleInterruptionException(String s) {
    super(s, true);
  }
}
