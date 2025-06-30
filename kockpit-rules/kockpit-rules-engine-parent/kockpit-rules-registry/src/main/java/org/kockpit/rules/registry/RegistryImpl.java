package org.kockpit.rules.registry;

import org.kockpit.rules.registry.model.Registry;
import org.kockpit.rules.registry.model.specification.FlowSpecification;
import org.kockpit.rules.registry.model.specification.RuleSpecification;

import java.util.List;

public class RegistryImpl implements Registry {

  private String name;

  private long id;

  private final List<? extends RuleSpecification> ruleSpecifications;

  private List<? extends FlowSpecification> flowSpecifications;

  public RegistryImpl(String name, long id, List<? extends RuleSpecification> ruleSpecifications) {
    this.name = name;
    this.id = id;
    this.ruleSpecifications = ruleSpecifications;
  }

  public RegistryImpl(
      String name,
      List<? extends RuleSpecification> ruleSpecifications,
      List<? extends FlowSpecification> flowSpecifications) {
    this.ruleSpecifications = ruleSpecifications;
    this.flowSpecifications = flowSpecifications;
  }

  public RegistryImpl(
      String name,
      long id,
      List<? extends RuleSpecification> ruleSpecifications,
      List<? extends FlowSpecification> flowSpecifications) {
    this.id = id;
    this.ruleSpecifications = ruleSpecifications;
    this.flowSpecifications = flowSpecifications;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public List<? extends RuleSpecification> getRuleSpecifications() {
    return ruleSpecifications;
  }

  @Override
  public List<? extends FlowSpecification> getFlowSpecifications() {
    return flowSpecifications;
  }
}
