package org.kockpit.audit.stream.api;

import java.util.List;
import java.util.function.Consumer;

/**
 * accept must be implemented by classes to consume received audits.
 */
public interface AuditConsumer extends Consumer<List<byte[]>> {
}
