package com.accor.kengine.registry.seamless.template;

import com.accor.kengine.DocumentationDetails;

public record DefaultDocumentationDetails(String code, String documentation)
    implements DocumentationDetails {
  public static DocumentationDetails details(String code, String documentation) {
    return new com.accor.kengine.DefaultDocumentationDetails(code, documentation);
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
