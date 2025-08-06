package com.accor.kengine.registry;

import com.accor.kengine.registry.model.specification.DetailsSpecification;

public class DetailsSpecificationImpl implements DetailsSpecification {

  private String code;

  private String name;

  private String description;

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
