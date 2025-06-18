package com.kockpit.rules;

public class DefaultDetailHandler implements DetailHandler {

  @Override
  public SimpleDetail handle(DocumentationDetails detail) {
    if (detail instanceof DocumentationDetails) {
      DocumentationDetails documentationDetails = detail;
      return new SimpleDetail(
          documentationDetails.getCode(),
          documentationDetails.getCode(),
          documentationDetails.getDocumentation());
    }
    return new SimpleDetail("" + detail, "" + detail);
  }
}
