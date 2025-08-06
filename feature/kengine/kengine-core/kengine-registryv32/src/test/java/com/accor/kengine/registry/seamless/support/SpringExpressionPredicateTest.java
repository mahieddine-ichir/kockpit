package com.accor.kengine.registry.seamless.support;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.accor.kengine.registry.seamless.json.PredicateJson;
import com.accor.kengine.registry.seamless.json.RuleJson;
import com.accor.kengine.registry.seamless.support.SpringExpressionPredicateTest.SpringTestConfiguration;
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
class SpringExpressionPredicateTest {

  static class PredicateImplementations {
    static boolean alwaysTrueExecuted;
    static boolean functionOfParamExecuted;

    public boolean alwaysTrue() {
      alwaysTrueExecuted = true;
      return true;
    }

    public boolean functionOfParam(String bool) {
      functionOfParamExecuted = true;
      return Boolean.parseBoolean(bool);
    }
  }

  @Configuration
  static class SpringTestConfiguration {
    @Bean
    PredicateImplementations predicates() {
      return new PredicateImplementations();
    }
  }

  @Autowired private ApplicationContext applicationContext;

  @Test
  void should_execute_normally() {
    RuleJson ruleJson = new RuleJson();
    ruleJson.setPredicates(
        Arrays.asList(
            PredicateJson.builder().spel("@predicates.alwaysTrue()").build(),
            PredicateJson.builder().spel("@predicates.functionOfParam(#root['bool'])").build()));
    PredicateImplementations.alwaysTrueExecuted = false;
    PredicateImplementations.functionOfParamExecuted = false;

    // When
    SpringExpressionPredicate underTest =
        new SpringExpressionPredicate(applicationContext, ruleJson);
    HashMap<Object, Object> context = new HashMap<>();
    context.put("bool", "true");
    boolean test1 = underTest.getPredicate().test(context);
    context.put("bool", "false");
    boolean test2 = underTest.getPredicate().test(context);

    // Then
    assertThat(PredicateImplementations.alwaysTrueExecuted).isTrue();
    assertThat(PredicateImplementations.functionOfParamExecuted).isTrue();
    assertThat(test1).isTrue();
    assertThat(test2).isFalse();
  }
}
