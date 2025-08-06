package com.accor.kengine.test.rule;

import com.accor.kengine.Action;
import com.accor.kengine.DefaultDocumentationDetails;
import com.accor.kengine.DocumentationDetails;

public class StringAction implements Action<Boolean> {

  private String value;

  public StringAction(String value) {
    this.value = value;
  }

  @Override
  public void execute(Boolean context) throws Exception {}

  @Override
  public String toString() {
    return "StringAction{" + "value='" + value + '\'' + '}';
  }

  @Override
  public DocumentationDetails getDetails() {
    return new DefaultDocumentationDetails(this.getClass().getSimpleName(), null);
  }
}
