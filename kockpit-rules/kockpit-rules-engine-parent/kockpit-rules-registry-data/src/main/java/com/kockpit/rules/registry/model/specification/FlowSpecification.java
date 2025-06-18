package com.kockpit.rules.registry.model.specification;

import java.util.List;

public interface FlowSpecification extends DetailsSpecification {

  String getId();

  /** @return ordered rule specification IDs of the flow */
  List<String> getRuleSpecificationIds();
}
