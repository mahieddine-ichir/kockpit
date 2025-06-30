package org.kockpit.rules;

public class DefaultDetailHandler implements DetailHandler {

  @Override
  public SimpleDetail handle(DocumentationDetails detail) {
    if (detail == null) return new SimpleDetail(null, null, null);
    return new SimpleDetail(
            detail.getCode(),
            detail.getCode(),
            detail.getDocumentation());
  }
}
