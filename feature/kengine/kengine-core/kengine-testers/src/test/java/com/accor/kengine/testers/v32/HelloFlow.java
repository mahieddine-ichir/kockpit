package com.accor.kengine.testers.v32;

import com.accor.kengine.seamless.ContextResult;
import com.accor.kengine.seamless.Flow;

@Flow(
    id = "hello",
    ruleClasses = {Rule1.class},
    documentation = "Hello sample flow")
public interface HelloFlow {

  @ContextResult("greetings")
  String sayHello(String name);
}
