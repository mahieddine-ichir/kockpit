package org.kockpit.audit.rules.data.model;

import java.util.List;

public interface Execution extends CommonExecution {

  String getExecutionUuid();

  List<? extends RuleExecution> getRules();

  Long getRegistryId();

  List<? extends ExecutionLog> getExecutionLogs();
}
