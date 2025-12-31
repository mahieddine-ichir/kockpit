package org.kockpit.audit.stream.api;

import org.kockpit.audit.stream.api.model.AuditReport;

import java.util.function.Consumer;

/**
 * accept must be implemented by classes to consume received audits.
 */
public interface AuditConsumer extends Consumer<AuditReport> {
}
