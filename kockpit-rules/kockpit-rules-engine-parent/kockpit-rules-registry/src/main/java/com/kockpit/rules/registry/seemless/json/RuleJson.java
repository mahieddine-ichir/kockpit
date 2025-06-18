package com.kockpit.rules.registry.seemless.json;

import com.kockpit.rules.Action;
import com.kockpit.rules.RuleNode;
import com.kockpit.rules.RulePredicate;
import com.kockpit.rules.registry.seemless.support.SpringExpressionAction;
import com.kockpit.rules.registry.seemless.support.SpringExpressionPredicate;
import lombok.Data;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@Data
public class RuleJson {
  private static int incrementalId;
  private String id;
  private String name;
  private DetailsJson details;
  private List<PredicateJson> predicates;
  private List<ActionJson> actions;
  private RuleJson ok;
  private RuleJson ko;

  public RuleJson() {
    id = "rulejson_" + incrementalId++;
  }

  public RuleNode getRuleNode(ApplicationContext applicationContext) {
    List<RulePredicate> generatePredicates = new ArrayList<>();
    if (!isEmpty(predicates)) {
      generatePredicates.add(new SpringExpressionPredicate(applicationContext, this));
    }
    RuleNode generateOk = null;
    if (nonNull(ok)) {
      generateOk = ok.getRuleNode(applicationContext);
    }
    RuleNode generateKo = null;
    if (nonNull(ko)) {
      generateKo = ko.getRuleNode(applicationContext);
    }
    List<Action> generateActions = new ArrayList<>();
    if (!isEmpty(actions)) {
      generateActions.add(new SpringExpressionAction(applicationContext, this));
    }
    RuleNode generateLastly = null;
    Predicate generateEligibilityPredicate = o -> Boolean.TRUE;
    return new RuleNode(
        details,
        generatePredicates,
        generateOk,
        generateKo,
        generateLastly,
        generateActions,
        generateEligibilityPredicate);
  }

  public String getId() {
    return nonNull(details) ? details.getCode() : id;
  }
}
