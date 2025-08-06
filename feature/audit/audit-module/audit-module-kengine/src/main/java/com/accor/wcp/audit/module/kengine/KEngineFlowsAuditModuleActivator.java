package com.accor.wcp.audit.module.kengine;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.accor.kengine.registry.RegistryImpl;
import com.accor.kengine.registry.RuleNodeRegistry;
import com.accor.kengine.registry.model.Registry;
import com.accor.kengine.registry.model.specification.RuleSpecification;
import com.accor.wcp.audit.AuditModuleActivator;
import com.accor.wcp.audit.module.kengine.flow.serializer.KEngineFlowsReferentialDto;
import com.accor.wcp.sdk.application.communication.App2WCPConsoleCommunicationService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class KEngineFlowsAuditModuleActivator implements AuditModuleActivator {
  private final RuleNodeRegistry ruleNodeRegistry;
  private final App2WCPConsoleCommunicationService app2WCPConsoleCommunicationService;

  @Override
  public void initialize() {
    KEngineFlowsReferentialDto referentialDto = serializeGlobalReferential();
    app2WCPConsoleCommunicationService.notify("audit", referentialDto);
  }

  @Override
  public void stop() {}

  KEngineFlowsReferentialDto serializeGlobalReferential() {
    Registry currentRegistry = ruleNodeRegistry.getCurrentRegistry();

    // Create new instance (copy) of Registry to skip computed registryId
    RegistryImpl registryForMessage =
        new RegistryImpl(
            currentRegistry.getName(),
            currentRegistry.getRuleSpecifications(),
            currentRegistry.getFlowSpecifications());
    Map<String, RuleSpecification> referentialRuleSpecifications =
        currentRegistry.getRuleSpecifications().stream()
            .collect(toMap(RuleSpecification::getName, identity()));

    return KEngineFlowsReferentialDto.builder()
        .registry(registryForMessage)
        .registryId(currentRegistry.getId())
        .referential(referentialRuleSpecifications)
        .build();
  }
}
