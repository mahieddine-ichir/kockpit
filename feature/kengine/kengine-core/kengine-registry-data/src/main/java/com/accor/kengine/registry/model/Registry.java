package com.accor.kengine.registry.model;

import com.accor.kengine.registry.model.specification.FlowSpecification;
import com.accor.kengine.registry.model.specification.RuleSpecification;
import java.util.List;

public interface Registry {

  String getName();

  long getId();

  List<? extends RuleSpecification> getRuleSpecifications();

  List<? extends FlowSpecification> getFlowSpecifications();
}
