package org.kockpit.rules.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kockpit.rules.DetailHandler;
import org.kockpit.rules.RuleNode;
import org.kockpit.rules.registry.dao.RegistryDao;
import org.kockpit.rules.registry.model.Flow;
import org.kockpit.rules.registry.model.Registry;
import org.kockpit.rules.registry.model.Rule;
import org.kockpit.rules.registry.model.specification.FlowSpecification;
import org.kockpit.rules.registry.model.specification.RuleSpecification;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

import static java.util.Objects.isNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class RuleNodeRegistry<T> {

  private final DetailHandler detailHandler;

  private final RegistryDao registryDao;

  private final List<Flow> flows;

  private final List<RuleNodesBuilderSupport<T>> ruleNodesBuilderSupports;

  private final String registryName;

  @Getter
  private List<Rule<T>> rules;

  private Map<String, Rule<T>> ruleById;

  private Map<String, List<Rule<T>>> flowRulesByFlowId;

  private RegistryImpl currentRegistry;

  protected RuleNodeRegistry() {
    // Used internally
    // Fake constructor => do nothing
    this.detailHandler = null;
    this.registryDao = null;
    this.flows = null;
    this.ruleNodesBuilderSupports = null;
    this.registryName = null;
  }

  @Autowired
  public RuleNodeRegistry(
      Optional<DetailHandler> detailHandler,
      Optional<RegistryDao> registryDao,
      List<Flow> flows,
      List<RuleNodesBuilderSupport<T>> ruleNodesBuilderSupports,
      @Value("${kockpit.registry.name}") String registryName) {
    this.detailHandler = detailHandler.orElse(null);
    this.registryDao = registryDao.orElse(null);
    this.flows = flows;
    this.ruleNodesBuilderSupports = ruleNodesBuilderSupports;
    this.registryName = registryName;
    setup();
  }

  public void setup() {
    this.rules =
        this.ruleNodesBuilderSupports.stream()
            .map(
                ruleNodesBuilderSupport -> {
                  try {
                    RuleNode<T> ruleNode = ruleNodesBuilderSupport.configure();
                    return new RuleImpl<T>(
                        ruleNodesBuilderSupport.getId(), 0, ruleNode.getDetails(), ruleNode);
                  } catch (Exception e) {
                    throw new RuntimeException(e);
                  }
                })
            .collect(toList());

    ruleById = this.rules.stream().collect(toMap(Rule::getId, identity()));

    // Produce flows rules links
    flowRulesByFlowId = computeRulesByFlowId(flows);

    // Generate registry
    try {
      currentRegistry = computeCurrentRegistry();
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }

    // Save registry
    if (registryDao != null) {
      registryDao.insert(currentRegistry);
    }
  }

  private Map<String, List<Rule<T>>> computeRulesByFlowId(Collection<Flow> flows1) {
    return flows1.stream()
        .map(
            flow -> {
              List<Rule<T>> rules =
                  flow.getEntries().stream()
                      .map(
                          flowEntry -> {
                            String entryId = flowEntry.getEntryId();
                            Rule<T> rule = ruleById.get(entryId);
                            if (isNull(rule)) {
                              throw new IllegalArgumentException(
                                  "Rule with id: " + entryId + " not found !");
                            }
                            return rule;
                          })
                      .collect(toList());
              return new AbstractMap.SimpleEntry<>(flow.getId(), rules);
            })
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  protected RegistryImpl computeCurrentRegistry() throws JsonProcessingException {
    // Compute rule specifications
    List<? extends RuleSpecification> currentRuleSpecifications =
        getRules().stream()
            .map(tRule -> new RuleSpecificationImpl(tRule, detailHandler))
            .collect(toList());

    RegistryImpl registry;

    // Compute flow specifications
    Optional<List<Flow>> flows = getFlows();
    if (flows.isPresent()) {
      List<? extends FlowSpecification> flowSpecifications =
          flows.get().stream()
              .map(flow -> new FlowSpecificationImpl(flow, detailHandler))
              .collect(toList());
      registry = new RegistryImpl(registryName, currentRuleSpecifications, flowSpecifications);
    } else {
      registry = new RegistryImpl(registryName, currentRuleSpecifications, null);
    }

    // Compute hash
    String value = new ObjectMapper().writeValueAsString(registry);
    long hash = value.hashCode();

    // Return final registry with its id
    return new RegistryImpl(
        registryName, hash, registry.getRuleSpecifications(), registry.getFlowSpecifications());
  }

  public Optional<List<Flow>> getFlows() {
    return Optional.of(flows);
  }

  public List<Rule<T>> getRules(Flow flow) {
    return getRulesByFlowId(flow.getId());
  }

  public List<Rule<T>> getRulesByFlowId(String id) {
    return flowRulesByFlowId.get(id);
  }

  public Registry getCurrentRegistry() {
    return currentRegistry;
  }

  public Optional<? extends Registry> getRegistry(long registryId) {
    return registryDao.get(registryId);
  }
}
