package org.kockpit.rules.registry.model;

import org.kockpit.rules.registry.model.specification.FlowSpecification;
import org.kockpit.rules.registry.model.specification.RuleSpecification;
import java.util.List;

public interface Registry {

  String getName();

  long getId();

  List<? extends RuleSpecification> getRuleSpecifications();

  List<? extends FlowSpecification> getFlowSpecifications();
}
