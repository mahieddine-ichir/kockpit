package com.accor.kengine.registry.seamless.support;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.accor.kengine.registry.seamless.json.ActionJson;
import com.accor.kengine.registry.seamless.json.RuleJson;
import com.accor.kengine.registry.seamless.support.SpringExpressionActionTest.SpringTestConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringTestConfiguration.class})
class SpringExpressionActionTest {

  static class ActionImplementations {
    static boolean sayHelloExecuted;
    static boolean sayHelloToExecuted;

    public String sayHello() {
      sayHelloExecuted = true;
      return "Hello !";
    }

    public String sayHelloTo(String name) {
      sayHelloToExecuted = true;
      return "Hello " + name;
    }
  }

  @Configuration
  static class SpringTestConfiguration {
    @Bean
    ActionImplementations actions() {
      return new ActionImplementations();
    }
  }

  @Autowired private ApplicationContext applicationContext;

  @Test
  void should_execute_normally() throws Exception {
    RuleJson ruleJson = new RuleJson();
    ruleJson.setActions(
        Arrays.asList(
            ActionJson.builder().spel("@actions.sayHello()").build(),
            ActionJson.builder().spel("@actions.sayHelloTo(#root['name'])").build()));
    ActionImplementations.sayHelloExecuted = false;
    ActionImplementations.sayHelloToExecuted = false;

    // When
    SpringExpressionAction underTest = new SpringExpressionAction(applicationContext, ruleJson);
    HashMap<Object, Object> context = new HashMap<>();
    context.put("name", "Cyril");
    underTest.execute(context);

    // Then
    assertThat(ActionImplementations.sayHelloExecuted).isTrue();
    assertThat(ActionImplementations.sayHelloToExecuted).isTrue();
  }
}
