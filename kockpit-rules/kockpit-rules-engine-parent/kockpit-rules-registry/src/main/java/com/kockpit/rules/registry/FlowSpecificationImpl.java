package com.kockpit.rules.registry;

import com.kockpit.rules.DetailHandler;
import com.kockpit.rules.registry.model.specification.FlowSpecification;
import com.kockpit.rules.registry.model.Flow;
import com.kockpit.rules.registry.model.FlowEntry;

import java.util.List;

import static com.kockpit.rules.registry.DetailsHelper.computeName;
import static java.util.stream.Collectors.toList;

public class FlowSpecificationImpl implements FlowSpecification {
  private String id;
  private List<String> ruleSpecificationIds;
  private String code;
  private String name;
  private String description;

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
