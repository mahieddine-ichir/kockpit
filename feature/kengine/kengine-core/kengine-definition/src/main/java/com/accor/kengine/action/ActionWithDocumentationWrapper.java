package com.accor.kengine.action;

import com.accor.kengine.Action;
import com.accor.kengine.DocumentationDetails;

public class ActionWithDocumentationWrapper<T> implements Action<T> {
  private Action<T> action;
  private DocumentationDetails documentation;

  public ActionWithDocumentationWrapper(Action<T> action, DocumentationDetails documentation) {
    this.action = action;
    this.documentation = documentation;
  }

  @Override
  public void execute(T context) throws Exception {
    action.execute(context);
  }

  @Override
  public DocumentationDetails getDetails() {
    return documentation;
  }
}
