package com.accor.kengine;

public class SkipAction implements Action<Boolean> {

  @Override
  public void execute(Boolean context) {
    throw new SkipRuleException("Skipped");
  }

  @Override
  public DocumentationDetails getDetails() {
    return new DefaultDocumentationDetails(this.getClass().getSimpleName(), null);
  }
}
