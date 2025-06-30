package org.kockpit.rules.registry.seemless.support;

import org.kockpit.rules.RulePredicate;
import org.kockpit.rules.registry.seemless.json.PredicateJson;
import org.kockpit.rules.registry.seemless.json.RuleJson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class SpringExpressionPredicate extends RulePredicate<Object> {
  private final RuleJson ruleJson;
  private final SpelExpressionParser expressionParser;
  private final BeanFactoryResolver beanResolver;
  private final List<Expression> expressions;

  public SpringExpressionPredicate(ApplicationContext applicationContext, RuleJson ruleJson) {
    super(ruleJson.getPredicates().get(0).getDetails());
    this.ruleJson = ruleJson;

    SpelParserConfiguration spelParserConfiguration = new SpelParserConfiguration(true, true);
    this.expressionParser = new SpelExpressionParser(spelParserConfiguration);
    beanResolver = new BeanFactoryResolver(applicationContext);

    expressions = buildExpressions();
  }

  private List<Expression> buildExpressions() {
    return this.ruleJson.getPredicates().stream()
        .map(PredicateJson::getSpel)
        .filter(StringUtils::isNoneBlank)
        .map(expressionParser::parseExpression)
        .toList();
  }

  @Override
  public Predicate<Object> getPredicate() {
    return o -> {
      StandardEvaluationContext evaluationContext = new StandardEvaluationContext(o);
      evaluationContext.setBeanResolver(beanResolver);
      List<Boolean> results =
          expressions.stream()
              .map(expression -> expression.getValue(evaluationContext, Boolean.class))
              .filter(Objects::nonNull)
              .toList();
      return results.stream().filter(Boolean.FALSE::equals).findFirst().orElse(Boolean.TRUE);
    };
  }
}
