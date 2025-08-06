package com.accor.wcp.sample.kengine.hello;

import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.perform;
import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.when;
import static java.util.Objects.nonNull;

import com.accor.kengine.RuleNode;
import com.accor.kengine.registry.seamless.RuleNodesBuilderSeamLessSupport;
import com.accor.kengine.seamless.Action;
import com.accor.kengine.seamless.ContextResult;
import com.accor.kengine.seamless.Predicate;
import com.accor.kengine.seamless.Rule;
import java.security.SecureRandom;
import lombok.AllArgsConstructor;

@Rule("hello1")
@AllArgsConstructor
class HelloRule extends RuleNodesBuilderSeamLessSupport {

  @Override
  public RuleNode configure() {
    HasName hasName = new HasName();
    HelloName sayHello = new HelloName();
    HelloDefault helloDefault = new HelloDefault();
    RandomHello randomHello = new RandomHello();
    return when(hasName)
        .then(perform(sayHello))
        .otherwise(perform(helloDefault)
            .when(hasName)
            .then(perform(sayHello))
        )
        .lastly(perform(randomHello))
        .end();
  }

  static class HasName {
    @Predicate(value = "PRE_HAS_NAME", documentation = "Has a name in inputs ?")
    boolean checkName(String name) {
      return nonNull(name);
    }
  }

  static class HelloName {
    @Action(value = "ACT_HELLO_NAME", documentation = "Compute greetings for given name")
    @ContextResult("sayHelloToWorld")
    String greetings(String name) {
      return "Hey " + name;
    }
  }

  static class HelloDefault {
    @Action(code = "ACT_HELLO_DEFAULT", documentation = "Compute default greetings without name")
    @ContextResult("sayHelloToWorld")
    String justSayHello() {
      return "Hello";
    }
  }

  static class RandomHello {
    SecureRandom random = new SecureRandom();

    @Action(code = "ACT_HELLO_RANDOM", documentation = "Compute random greetings")
    @ContextResult("randomGreetings")
    String randomGreetings() {
      return "Hello " + random.nextFloat();
    }
  }
}
