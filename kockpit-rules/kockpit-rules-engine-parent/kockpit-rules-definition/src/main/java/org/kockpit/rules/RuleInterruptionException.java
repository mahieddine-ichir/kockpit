package org.kockpit.rules;

public class RuleInterruptionException extends WarningExecutionException {

  public RuleInterruptionException(String s) {
    super(s, true);
  }
}
