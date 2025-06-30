package org.kockpit.rules.audit.flow.serializer;

import lombok.Getter;
import lombok.Setter;
import org.kockpit.rules.KengineLog;
import org.kockpit.rules.registry.model.specification.RuleSpecification;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class ExecutionEDTDTO extends AbstractEDTDTO {
  private String executionUUID;
  private Long registryId;
  private List<RuleEDTDTO> executionRules;
  private Map<String, List<RuleDetailEDTDTO>> rules;
  private Map<String, RuleSpecification> referential;
  private List<KengineLog> logs;
  private String fullRegistryReferentialId;

}
