package com.kockpit.rules;

/** Skips to next rule */
public class RuleInterruptionException extends WarningExecutionException {

  public RuleInterruptionException(String s) {
    super(s, true);
  }
}
