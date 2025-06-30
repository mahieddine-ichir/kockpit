package org.kockpit.rules.registry.model.specification;

import java.util.List;

public interface RuleSpecification {

  String getId();

  String getName();

  DetailsSpecification getDetails();

  RuleSpecification getOk();

  RuleSpecification getKo();

  RuleSpecification getLastly();

  List<? extends DetailsSpecification> getActions();

  List<? extends DetailsSpecification> getPredicates();
}
