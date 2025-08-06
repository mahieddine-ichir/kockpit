package com.accor.kengine.registry.seamless.json;

import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.accor.kengine.Action;
import com.accor.kengine.RuleNode;
import com.accor.kengine.RulePredicate;
import com.accor.kengine.registry.seamless.support.SpringExpressionAction;
import com.accor.kengine.registry.seamless.support.SpringExpressionPredicate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.Data;
import org.springframework.context.ApplicationContext;

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
