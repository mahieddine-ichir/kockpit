package com.accor.kengine.audit.dao;

import com.accor.kengine.audit.model.Execution;
import java.util.UUID;

// FIXME - should be a dao or a service? => review
public interface ExecutionAuditDao {

  Execution insert(Execution execution);

  Execution insertWithTTl(Execution execution, int ttl);

  Execution load(UUID uuid);
}
