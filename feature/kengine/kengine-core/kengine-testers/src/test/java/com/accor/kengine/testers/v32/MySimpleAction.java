package com.accor.kengine.testers.v32;

import com.accor.kengine.seamless.Action;
import com.accor.kengine.seamless.ContextResult;
import org.springframework.stereotype.Component;

@Component
class MySimpleAction {
  @Action
  @ContextResult("greetings")
  String helloWorld(String name) {
    String greetings = "Hello world " + name;
    System.out.println(greetings);
    return greetings;
  }
}
