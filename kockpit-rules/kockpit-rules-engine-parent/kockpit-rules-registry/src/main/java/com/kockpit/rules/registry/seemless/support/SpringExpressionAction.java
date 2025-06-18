package com.kockpit.rules.registry.seemless.support;

import com.kockpit.rules.Action;
import com.kockpit.rules.registry.seemless.json.ActionJson;
import com.kockpit.rules.registry.seemless.json.RuleJson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;

public class SpringExpressionAction implements Action {
  private final RuleJson ruleJson;
  private final ApplicationContext applicationContext;
  private final SpelParserConfiguration spelParserConfiguration;
  private final SpelExpressionParser expressionParser;
  private final BeanFactoryResolver beanResolver;
  private final List<Expression> expressions;

  public SpringExpressionAction(ApplicationContext applicationContext, RuleJson ruleJson) {
    this.ruleJson = ruleJson;
    this.applicationContext = applicationContext;

    this.spelParserConfiguration = new SpelParserConfiguration(true, true);
    this.expressionParser = new SpelExpressionParser(spelParserConfiguration);
    beanResolver = new BeanFactoryResolver(this.applicationContext);

    expressions = buildExpressions();
  }

  private List<Expression> buildExpressions() {
    return this.ruleJson.getActions().stream()
        .map(ActionJson::getSpel)
        .filter(StringUtils::isNoneBlank)
        .map(expressionParser::parseExpression)
        .toList();
  }

  @Override
  public void execute(Object context) throws Exception {
    StandardEvaluationContext evaluationContext = new StandardEvaluationContext(context);
    evaluationContext.setBeanResolver(beanResolver);
    expressions.forEach(expression -> expression.getValue(evaluationContext));
  }
}
