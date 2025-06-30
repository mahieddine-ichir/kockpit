package org.kockpit.rules;

public interface DocumentationDetails {

  default String getCode() {
    if (this instanceof Enum<?> _enum) return _enum.name();
    else throw new IllegalArgumentException("Not implemented yet!");
  }

  String getDocumentation();
}
