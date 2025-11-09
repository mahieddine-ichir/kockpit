package org.kockpit.audit.sampleapp;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.rules.RuleNode;
import org.kockpit.rules.registry.seemless.RuleNodesBuilderSeamLessSupport;
import org.kockpit.rules.seemless.Action;
import org.kockpit.rules.seemless.ContextResult;
import org.kockpit.rules.seemless.Predicate;
import org.kockpit.rules.seemless.Rule;

import static org.kockpit.rules.seemless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.define;
import static org.kockpit.rules.seemless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.perform;

@Rule
@Slf4j
public class ApiRule extends RuleNodesBuilderSeamLessSupport {



    @Override
    public RuleNode configure() throws Exception {
        return define("Api_Rule", "Basic Api Rule")
                .when(new PredicateImpl())
                .then(perform(new UpperCase()))
                .otherwise(perform(new IdentityAction()))
                .end();
    }

    static class PredicateImpl {
        @Predicate
        boolean isJohn(String name) {
            return name.equals("john");
        }
    }

    static class UpperCase {
        @Action
        @ContextResult("output")
        String upper(String name) {
            return name.toUpperCase();
        }
    }

    static class IdentityAction {
        @Action
        @ContextResult("output")
        String upper(String name) {
            return name;
        }
    }
}
