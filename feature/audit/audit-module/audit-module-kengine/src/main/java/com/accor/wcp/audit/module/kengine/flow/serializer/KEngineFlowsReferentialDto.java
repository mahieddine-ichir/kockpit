package com.accor.wcp.audit.module.kengine.flow.serializer;

import com.accor.kengine.registry.model.Registry;
import com.accor.kengine.registry.model.specification.RuleSpecification;
import java.io.Serializable;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** Communication object to send KEngine Flows Referential data to console backend. */
@Data
@Builder
public class KEngineFlowsReferentialDto implements Serializable {
  private Registry registry;
  private Long registryId;
  private Map<String, RuleSpecification> referential;
}
