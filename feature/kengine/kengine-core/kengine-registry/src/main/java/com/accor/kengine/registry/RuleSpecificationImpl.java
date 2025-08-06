package com.accor.kengine.registry;

import static com.accor.kengine.registry.DetailsHelper.computeName;

import com.accor.kengine.*;
import com.accor.kengine.registry.model.Rule;
import com.accor.kengine.registry.model.specification.RuleSpecification;
import java.util.List;
import java.util.stream.Collectors;

public class RuleSpecificationImpl implements RuleSpecification {

  private String id;

  private String name;

  private DetailsSpecificationImpl details;

  private RuleSpecificationImpl ok;

  private RuleSpecificationImpl ko;

  private RuleSpecificationImpl lastly;

  private List<DetailsSpecificationImpl> actions;

  private List<DetailsSpecificationImpl> predicates;

  public RuleSpecificationImpl(Rule rule, DetailHandler detailHandler) {
    this(rule.getRuleNode(), detailHandler);
    this.details = computeName(rule.getDetails(), detailHandler);
    this.name = details.getName();
  }

  public RuleSpecificationImpl(RuleNode ruleNode, DetailHandler detailHandler) {
    this.details = computeName(ruleNode.getDetails(), detailHandler);
    this.name = details.getName();
    this.id = details.getName();
    if (ruleNode.getOk() != null) {
      this.ok = new RuleSpecificationImpl(ruleNode.getOk(), detailHandler);
    }
    if (ruleNode.getKo() != null) {
      this.ko = new RuleSpecificationImpl(ruleNode.getKo(), detailHandler);
    }
    if (ruleNode.getLastly() != null) {
      this.lastly = new RuleSpecificationImpl(ruleNode.getLastly(), detailHandler);
    }
    List<Action> actions = ruleNode.getActions();
    if (actions != null) {
      this.actions =
          actions.stream()
              .map(o -> computeName(o.getDetails(), detailHandler))
              .collect(Collectors.toList());
    }
    List<RulePredicate> predicates = ruleNode.getPredicates();
    if (predicates != null) {
      this.predicates =
          predicates.stream()
              .map(rulePredicate -> computeName(rulePredicate.getDetails(), detailHandler))
              .collect(Collectors.toList());
    }
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public DetailsSpecificationImpl getDetails() {
    return details;
  }

  @Override
  public RuleSpecificationImpl getOk() {
    return ok;
  }

  @Override
  public RuleSpecificationImpl getKo() {
    return ko;
  }

  @Override
  public RuleSpecificationImpl getLastly() {
    return lastly;
  }

  @Override
  public List<DetailsSpecificationImpl> getActions() {
    return actions;
  }

  @Override
  public List<DetailsSpecificationImpl> getPredicates() {
    return predicates;
  }
}
