package org.kockpit.rules.registry;

import org.kockpit.rules.DetailHandler;
import org.kockpit.rules.registry.model.specification.FlowSpecification;
import org.kockpit.rules.registry.model.Flow;
import org.kockpit.rules.registry.model.FlowEntry;

import java.util.List;

import static org.kockpit.rules.registry.DetailsHelper.computeName;
import static java.util.stream.Collectors.toList;

public class FlowSpecificationImpl implements FlowSpecification {
  private final String id;
  private final List<String> ruleSpecificationIds;
  private final String code;
  private final String name;
  private final String description;

  public FlowSpecificationImpl(Flow flow, DetailHandler detailHandler) {
    DetailsSpecificationImpl detailsSpecification = computeName(flow.getDetails(), detailHandler);
    this.id = flow.getId();
    this.name = detailsSpecification.getName();
    this.description = detailsSpecification.getDescription();
    this.code = detailsSpecification.getCode();

    ruleSpecificationIds = flow.getEntries().stream().map(FlowEntry::getEntryId).collect(toList());
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public List<String> getRuleSpecificationIds() {
    return ruleSpecificationIds;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }
}
