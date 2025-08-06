package com.accor.kengine.registry.seamless.v32;

import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.define;
import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.perform;
import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.when;

import com.accor.kengine.RuleNode;
import com.accor.kengine.registry.seamless.RuleNodesBuilderSeamLessSupport;
import com.accor.kengine.registry.seamless.action.ActionWithAnnotation;
import com.accor.kengine.registry.seamless.action.StringAction;
import com.accor.kengine.seamless.Rule;
import java.util.function.Predicate;

@Rule()
public class Rule1 extends RuleNodesBuilderSeamLessSupport {

  @Override
  public RuleNode configure() {
    Predicate<Boolean> backendAvailable = Boolean::booleanValue;
    Predicate<Boolean> userDoesNotExist = Boolean::booleanValue;
    Predicate<Boolean> localCacheAvailable = Boolean::booleanValue;
    StringAction callBackend = new StringAction("OK1");
    StringAction getLocalData = new StringAction("OK1");
    ActionWithAnnotation actionWithAnnotation = new ActionWithAnnotation();
    StringAction throwAnUnavailableException = new StringAction("KO");

    return define("rule1", "Documentation")
        .when(backendAvailable)
        .then(
            when(userDoesNotExist)
                .then(
                    perform(actionWithAnnotation)
                        .when(backendAvailable)
                        .then(perform(getLocalData))
                        .otherwise(perform(callBackend)))
                .otherwise(when(localCacheAvailable).perform(getLocalData)))
        .otherwise(throwAnUnavailableException)
        .build();
  }
}
