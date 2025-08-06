package com.accor.kengine.yaml;

import com.accor.kengine.*;
import com.accor.kengine.registry.RuleNodesBuilderSupport;
import com.accor.kengine.registry.model.Flow;
import com.accor.kengine.registry.model.FlowEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "kengine.yaml.flow.enable", havingValue = "true")
public class YamlFlowAutoConfiguration implements BeanFactoryAware {

  private BeanFactory beanFactory;
  private final List<Action> actions;
  private final List<RulePredicate> rulePredicates;
  private YamlFlowMapperCustomizer yamlFlowMapperCustomizer;
  private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

  @Value("${kengine.yaml.flow.location}")
  private String location;

  public YamlFlowAutoConfiguration(
      List<Action> actions,
      List<RulePredicate> rulePredicates,
      Optional<YamlFlowMapperCustomizer> yamlFlowMapperCustomizer) {
    this.actions = actions;
    this.rulePredicates = rulePredicates;
    yamlFlowMapperCustomizer.ifPresent(customizer -> this.yamlFlowMapperCustomizer = customizer);
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }

  @PostConstruct
  public void generateFlows() throws IOException {
    PathMatchingResourcePatternResolver loader = new PathMatchingResourcePatternResolver();
    Resource[] resources = loader.getResources("classpath:" + location + "*.yaml");
    List<YamlFlow> yamlFlows =
        Arrays.stream(resources).map(this::resourceAsInputStream).map(this::getYamlFlow).toList();

    yamlFlows.stream()
        .map(YamlFlow::getRules)
        .flatMap(Collection::stream)
        .map(this::buildRuleNode)
        .forEach(this::registerRuleNodesBuilderSupportBean);
    yamlFlows.stream().map(this::buildFlow).forEach(this::registerFlowBean);
  }

  private void registerFlowBean(Flow flow) {
    ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
    log.info("Registered flow: " + flow.getId());
    configurableBeanFactory.registerSingleton(flow.getId(), flow);
  }

  private void registerRuleNodesBuilderSupportBean(
      RuleNodesBuilderSupport ruleNodesBuilderSupport) {
    ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
    log.info("Registered rule: " + ruleNodesBuilderSupport.getId());
    configurableBeanFactory.registerSingleton(
        ruleNodesBuilderSupport.getId(), ruleNodesBuilderSupport);
  }

  private RuleNodesBuilderSupport buildRuleNode(YamlRule yamlRule) {
    DefaultDocumentationDetails defaultDocumentationDetails =
        new DefaultDocumentationDetails(yamlRule.getName(), yamlRule.getDetails());
    return new RuleNodesBuilderSupport(defaultDocumentationDetails) {
      @Override
      public RuleNode configure() {
        RuleNodeBuilder root = new RuleNodeBuilder<>(defaultDocumentationDetails).ok();
        root = getRuleNode(yamlRule, root);
        return root.done().createRuleNode();
      }
    };
  }

  private Flow buildFlow(YamlFlow yamlFlow) {
    return new Flow() {
      @Override
      public String getId() {
        return yamlFlow.getName();
      }

      @Override
      public DocumentationDetails getDetails() {
        return new DefaultDocumentationDetails(yamlFlow.getName(), yamlFlow.getDetails());
      }

      @Override
      public List<FlowEntry> getEntries() {
        return yamlFlow.getRules().stream()
            .map(YamlRule::getName)
            .map(ruleName -> (FlowEntry) () -> ruleName)
            .collect(Collectors.toList());
      }
    };
  }

  private YamlFlow getYamlFlow(InputStream is) {
    try {
      if (yamlFlowMapperCustomizer != null) {
        Object x = mapper.readValue(is, yamlFlowMapperCustomizer.getTypeParameterClass());
        return yamlFlowMapperCustomizer.generateFlow(x);
      }
      return mapper.readValue(is, YamlFlow.class);
    } catch (IOException e) {
      throw new IllegalArgumentException("Invalid format for file", e);
    }
  }

  private InputStream resourceAsInputStream(Resource resource) {
    try {
      return resource.getInputStream();
    } catch (IOException e) {
      throw new IllegalArgumentException("No file found");
    }
  }

  private RuleNodeBuilder getRuleNode(YamlRule rule, RuleNodeBuilder node) {
    node = addAction(rule.getActions(), node);
    node = addPredicate(rule.getPredicate(), node);
    return node;
  }

  private RuleNodeBuilder addAction(List<String> actions, RuleNodeBuilder node) {
    return node.actions(getActions(actions));
  }

  private RuleNodeBuilder addPredicate(YamlPredicate predicate, RuleNodeBuilder node) {
    if (predicate != null) {
      List<RulePredicate> predicates =
          predicate.getNames().stream()
              .map(this::getPredicateFromRegistry)
              .collect(Collectors.toList());
      RuleNodeBuilder ruleNodeBuilder = node.predicates(predicates);
      if (predicate.getOk() != null) {
        ruleNodeBuilder = ruleNodeBuilder.ok().actions(getActions(predicate.getOk().getActions()));
        ruleNodeBuilder = addPredicate(predicate.getOk().getPredicate(), ruleNodeBuilder);
        ruleNodeBuilder = ruleNodeBuilder.done();
      }
      if (predicate.getKo() != null) {
        ruleNodeBuilder = ruleNodeBuilder.ko().actions(getActions(predicate.getKo().getActions()));
        ruleNodeBuilder = addPredicate(predicate.getKo().getPredicate(), ruleNodeBuilder);
        ruleNodeBuilder = ruleNodeBuilder.done();
      }
      if (predicate.getLastly() != null) {
        ruleNodeBuilder =
            ruleNodeBuilder.lastly().actions(getActions(predicate.getLastly().getActions()));
        ruleNodeBuilder = addPredicate(predicate.getLastly().getPredicate(), ruleNodeBuilder);
        ruleNodeBuilder = ruleNodeBuilder.done();
      }
      node = ruleNodeBuilder;
    }
    return node;
  }

  private Action getActionFromRegistry(String name) {
    return actions.stream()
        .filter(action -> name.equalsIgnoreCase(action.getDetails().toString()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No action named " + name));
  }

  private RulePredicate getPredicateFromRegistry(String name) {
    return rulePredicates.stream()
        .filter(predicate -> name.equalsIgnoreCase(predicate.getDetails().toString()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No action named " + name));
  }

  private List<Action> getActions(List<String> actionNames) {
    return Optional.ofNullable(actionNames)
        .map(Collection::stream)
        .orElse(Stream.empty())
        .map(this::getActionFromRegistry)
        .collect(Collectors.toList());
  }
}
