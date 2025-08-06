package com.accor.wcp.audit;

public class AuditNotStartedException extends RuntimeException {

  public AuditNotStartedException() {
    super("Audit not started, report not initialized");
  }
}
