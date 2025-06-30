package org.kockpit.rules.registry;

import org.kockpit.rules.registry.model.specification.DetailsSpecification;

public class DetailsSpecificationImpl implements DetailsSpecification {

  private final String code;

  private final String name;

  private final String description;

  public DetailsSpecificationImpl(String code, String name, String description) {
    this.code = code;
    this.name = name;
    this.description = description;
  }

  public DetailsSpecificationImpl(String details) {
    this.description = details;
    this.code = details;
    this.name = details;
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
