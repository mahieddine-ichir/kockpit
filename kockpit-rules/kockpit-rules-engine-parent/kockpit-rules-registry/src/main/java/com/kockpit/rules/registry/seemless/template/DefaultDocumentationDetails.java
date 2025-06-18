package com.kockpit.rules.registry.seemless.template;

import com.kockpit.rules.DocumentationDetails;

public record DefaultDocumentationDetails(String code, String documentation)
    implements DocumentationDetails {
  public static DocumentationDetails details(String code, String documentation) {
    return new com.kockpit.rules.DefaultDocumentationDetails(code, documentation);
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getDocumentation() {
    return documentation;
  }
}
