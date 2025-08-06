package com.accor.kengine.seamless;

import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.perform;
import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.when;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.accor.kengine.RuleNode;
import org.junit.jupiter.api.Test;

class RuleNodeBuilderSeamLessTest {

  @Test
  void should_define_rule_with_dsl32() {
    Object action1 = "action1";
    Object backendAvailable = "predicate-backendAvailable";
    Object userDoesNotExist = "predicate-userDoesNotExist";
    Object actionWithAnnotation = "actionWithAnnotation";
    Object getLocalData = "getLocalData";
    Object callBackend = "callBackend";
    Object localCacheAvailable = "predicate-localCacheAvailable";
    Object throwAnUnavailableException = "throwAnUnavailableException";

    RuleNode ruleNode =
        when(backendAvailable)
            .then(
                when(userDoesNotExist)
                    .then(
                        perform(actionWithAnnotation)
                            .when(backendAvailable)
                            .then(
                                perform(getLocalData)
                                    .when(backendAvailable)
                                    .then(action1)
                                    .otherwise(action1))
                            .otherwise(perform(callBackend)))
                    .otherwise(when(localCacheAvailable).perform(getLocalData)))
            .otherwise(throwAnUnavailableException)
            .build();

    assertNotNull(ruleNode);
  }
}
