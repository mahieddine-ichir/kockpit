package org.kockpit.rules.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditModuleActivator;
import org.kockpit.rules.audit.flow.serializer.KEngineFlowsReferentialDto;
import org.kockpit.rules.registry.RegistryImpl;
import org.kockpit.rules.registry.RuleNodeRegistry;
import org.kockpit.rules.registry.model.Registry;
import org.kockpit.rules.registry.model.specification.RuleSpecification;

import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j
public class KEngineFlowsAuditModuleActivator implements AuditModuleActivator {
  private final RuleNodeRegistry ruleNodeRegistry;
  // todo private final App2WCPConsoleCommunicationService app2WCPConsoleCommunicationService;

  @Override
  public void initialize() {
    KEngineFlowsReferentialDto referentialDto = serializeGlobalReferential();
    // app2WCPConsoleCommunicationService.notify("audit", referentialDto);
    log.warn("Should notif via commucation service App2WCPConsoleCommunicationService");
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
