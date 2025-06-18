package org.kockpit.audit.api;

/**
 * Activator interface for audit module which wants to be part of audit service lifecycle. You may
 * want to initialize data / prepare internal services during service initialization. You man want
 * to release resources, clear data when service is shutting down.
 */
public interface AuditModuleActivator {

  /** Called during audit service initialization. */
  void initialize();

  /** Called during audit service shutdown phase. */
  void stop();
}
