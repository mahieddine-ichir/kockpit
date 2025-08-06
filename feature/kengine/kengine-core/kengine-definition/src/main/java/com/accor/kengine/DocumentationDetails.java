package com.accor.kengine;

public interface DocumentationDetails {

  default String getCode() {
    if (this instanceof Enum<?>) return ((Enum) this).name();
    else throw new IllegalArgumentException("Not implemented yet!");
  }

  String getDocumentation();
}
