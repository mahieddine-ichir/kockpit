package org.kockpit.audit.rules.data.dao;

import org.kockpit.audit.rules.data.model.Execution;
import java.util.UUID;

// FIXME - should be a dao or a service? => review
public interface ExecutionAuditDao {

  Execution insert(Execution execution);

  Execution insertWithTTl(Execution execution, int ttl);

  Execution load(UUID uuid);
}
