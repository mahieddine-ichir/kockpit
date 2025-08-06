package com.accor.kengine.seamless;

import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.perform;
import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.when;
import static com.accor.kengine.seamless.MultipleActionMethodsHelper.$;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.accor.kengine.RuleNode;
import com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder;
import org.junit.jupiter.api.Test;

class RuleNodeBuilderSeamLessRealTest {

  @Test
  void should_define_rule_with_dsl32() {
    Predicates predicates = new Predicates();
    Actions actions = new Actions();
    OneAction oneAction = new OneAction();
    OneAction lastlyAction = new OneAction();
    Actions $ = $(Actions.class);

    //    $(Actions.class).executeVoidMethod(null);
    //    Method m = Actions.class::executeVoidMethod;

    RuleNode ruleNode =
        StaticBuilder.define("BR_CODE", "BR Documentation")
            .when(predicates)
            .then(
                when(predicates)
                    .then(
                        perform(actions, () -> $.executeVoidMethod(null))
                            .when(predicates)
                            .then(
                                perform(actions, () -> $.stringGenerator(null), null)
                                    .when(predicates)
                                    .then(actions, () -> $.executeVoidMethod(null), null)
                                    .otherwise(oneAction))
                            .otherwise(perform(oneAction)))
                    .otherwise(when(predicates).perform(oneAction))
                    .lastly(perform(oneAction)))
            .otherwise(oneAction)
            .lastly(perform(oneAction))
            .end();

    assertNotNull(ruleNode);
  }

  static class OneAction {
    @Action
    void oneAction(Object data1) {
      // Fake execution
      compute();
    }

    private int compute() {
      return 0;
    }
  }

  static class Actions {
    @Action
    void executeVoidMethod(Object data1) {
      // Fake execution
    }

    @Action
    @ContextResult("result")
    String stringGenerator(Object data2) {
      return "" + data2;
    }
  }

  static class Predicates {

    @Predicate
    boolean testAlwaysTrue(Object data1) {
      return true;
    }
  }
}
