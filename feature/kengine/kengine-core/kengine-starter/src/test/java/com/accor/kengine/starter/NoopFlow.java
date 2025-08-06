package com.accor.kengine.starter;

import com.accor.kengine.seamless.ContextResult;
import com.accor.kengine.seamless.Flow;

@Flow(id = "NoopFlow", documentation = "Noop flow")
public interface NoopFlow {

  @ContextResult()
  String nothingToDo(String name);
}
