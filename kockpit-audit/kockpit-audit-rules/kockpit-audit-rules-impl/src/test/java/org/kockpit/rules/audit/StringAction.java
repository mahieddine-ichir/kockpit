package org.kockpit.rules.audit;

import org.kockpit.rules.Action;
import org.kockpit.rules.DefaultDocumentationDetails;
import org.kockpit.rules.DocumentationDetails;

public class StringAction implements Action<Boolean> {

  private final String value;

  public StringAction(String value) {
    this.value = value;
  }

  @Override
  public void execute(Boolean context) {}

  @Override
  public String toString() {
    return "StringAction{" + "value='" + value + '\'' + '}';
  }

  @Override
  public DocumentationDetails getDetails() {
    return new DefaultDocumentationDetails(this.getClass().getSimpleName(), null);
  }
}
