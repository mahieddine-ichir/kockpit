package com.accor.wcp.audit.module.kengine.flow.serializer;

import com.accor.kengine.KengineLog;
import com.accor.kengine.registry.model.specification.RuleSpecification;
import java.util.List;
import java.util.Map;

public class ExecutionEDTDTO extends AbstractEDTDTO {
  private String executionUUID;
  private Long registryId;
  private List<RuleEDTDTO> executionRules;
  private Map<String, List<RuleDetailEDTDTO>> rules;
  private Map<String, RuleSpecification> referential;
  private List<KengineLog> logs;
  private String fullRegistryReferentialId;

  public String getExecutionUUID() {
    return executionUUID;
  }

  public void setExecutionUUID(String executionUUID) {
    this.executionUUID = executionUUID;
  }

  public List<RuleEDTDTO> getExecutionRules() {
    return executionRules;
  }

  public void setExecutionRules(List<RuleEDTDTO> executionRules) {
    this.executionRules = executionRules;
  }

  public Map<String, List<RuleDetailEDTDTO>> getRules() {
    return rules;
  }

  public void setRules(Map<String, List<RuleDetailEDTDTO>> rules) {
    this.rules = rules;
  }

  public Map<String, RuleSpecification> getReferential() {
    return referential;
  }

  public void setReferential(Map<String, RuleSpecification> referential) {
    this.referential = referential;
  }

  public Long getRegistryId() {
    return registryId;
  }

  public void setRegistryId(Long registryId) {
    this.registryId = registryId;
  }

  public List<KengineLog> getLogs() {
    return logs;
  }

  public void setLogs(List<KengineLog> logs) {
    this.logs = logs;
  }

  public void setFullRegistryReferentialId(String fullRegistryReferentialId) {
    this.fullRegistryReferentialId = fullRegistryReferentialId;
  }

  public String getFullRegistryReferentialId() {
    return fullRegistryReferentialId;
  }
}
