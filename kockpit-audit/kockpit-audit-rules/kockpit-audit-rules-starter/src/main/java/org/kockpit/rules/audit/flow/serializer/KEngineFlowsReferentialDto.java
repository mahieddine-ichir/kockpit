package org.kockpit.rules.audit.flow.serializer;

import lombok.Builder;
import lombok.Data;
import org.kockpit.rules.registry.model.Registry;
import org.kockpit.rules.registry.model.specification.RuleSpecification;

import java.io.Serializable;
import java.util.Map;

/** Communication object to send KEngine Flows Referential data to console backend. */
@Data
@Builder
public class KEngineFlowsReferentialDto implements Serializable {
  private Registry registry;
  private Long registryId;
  private Map<String, RuleSpecification> referential;
}
