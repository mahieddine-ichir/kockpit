package org.kockpit.audit.api;

public class AuditNotStartedException extends RuntimeException {

  public AuditNotStartedException() {
    super("Audit not started, report not initalized");
  }
}
