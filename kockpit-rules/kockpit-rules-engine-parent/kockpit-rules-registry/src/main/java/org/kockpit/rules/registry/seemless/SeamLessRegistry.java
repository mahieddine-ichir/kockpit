package org.kockpit.rules.registry.seemless;

import org.kockpit.rules.DetailHandler;
import org.kockpit.rules.DocumentationDetails;
import org.kockpit.rules.RuleNode;
import org.kockpit.rules.seemless.Flow;
import org.kockpit.rules.seemless.Rule;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.rules.registry.*;
import org.kockpit.rules.registry.RegistryJson;
import org.kockpit.rules.registry.dao.RegistryDao;
import org.kockpit.rules.registry.model.FlowEntry;
import org.kockpit.rules.registry.model.Registry;
import org.kockpit.rules.registry.model.specification.FlowSpecification;
import org.kockpit.rules.registry.model.specification.RuleSpecification;
import org.kockpit.rules.registry.seemless.json.FlowJson;
import org.kockpit.rules.registry.seemless.json.KEngineJSon;
import org.kockpit.rules.registry.seemless.json.RuleJson;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotatedTypeMetadata;
import tools.jackson.core.JacksonException;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.kockpit.rules.registry.seemless.NamingHelper.normalizeComponentName;
import static org.springframework.util.ObjectUtils.isEmpty;

/** This class extends {@link RuleNodeRegistry} for backward compatibility. */
@Slf4j
public class SeamLessRegistry extends RuleNodeRegistry {

  private List<RuleNodesBuilderSupport> ruleNodesBuilderSupports;

  private List<org.kockpit.rules.registry.model.Flow> oldCompatibilityFlows;

  @Autowired(required = false)
  private DetailHandler detailHandler;

  @Autowired(required = false)
  private RegistryDao registryDao;

  private ApplicationContext applicationContext;

  @Value("${kengine.registry.name:}")
  private String registryName;

  @Value("${kengine.registry.flows.json:}")
  private String jsonFile;

  private List<RuleImpl> rules;

  private Map<String, org.kockpit.rules.registry.model.Rule> ruleById;

  private Map<String, List<org.kockpit.rules.registry.model.Rule>> flowRulesByFlowId;

  private RegistryImpl currentRegistry;
  private List<org.kockpit.rules.registry.model.Flow> flows;
  private Map<Class, RuleImpl> ruleByClass;

  public SeamLessRegistry(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Deprecated
  public SeamLessRegistry(
      ApplicationContext applicationContext,
      List<RuleNodesBuilderSupport> ruleNodesBuilderSupports,
      List<org.kockpit.rules.registry.model.Flow> flows) {
    this.applicationContext = applicationContext;
    this.ruleNodesBuilderSupports = ruleNodesBuilderSupports;
    this.oldCompatibilityFlows = flows;
  }

  @PostConstruct
  void init() {
    this.setup();
  }

  @SneakyThrows
  @Deprecated
  public void setup() {
    loadRules();

    loadFLows();

    loadBetaJson();

    // Compute and initialize internal KEngine objects
    compute(flows);
  }

  private void loadBetaJson() throws IOException {
    // BETA KEngine JSON (POC: don't use it in production)
    if (!isEmpty(jsonFile)) {
      loadKEngineJson();
    }
  }

  private void loadFLows() {
    // Get flows from annotation
    Map<String, Object> flowsMap = applicationContext.getBeansWithAnnotation(Flow.class);

    // Convert to old objects
    flows = flowsMap.entrySet().stream().map(this::convertToFlowModel).toList();

    // Old compatibility
    if (nonNull(oldCompatibilityFlows)) {
      oldCompatibilityFlows.removeAll(flowsMap.values());
      flows = new ArrayList<>(flows);
      flows.addAll(oldCompatibilityFlows);
    }
  }

  private void loadRules() {
    // Get rules from annotation
    Map<String, Object> rulesMap = applicationContext.getBeansWithAnnotation(Rule.class);

    rules = rulesMap.entrySet().stream().map(this::convertToRuleImpl).toList();

    // Old compatibility for rules
    if (nonNull(this.ruleNodesBuilderSupports)) {
      this.ruleNodesBuilderSupports.removeAll(rulesMap.values());
      List<RuleImpl> oldCompatibilityRules =
          this.ruleNodesBuilderSupports.stream()
              .map(SeamLessRegistry::getOldCompatibilityRule)
              .toList();
      rules = new ArrayList<>(rules);
      rules.addAll(oldCompatibilityRules);
    }

    computeRulesBys();
  }

  private void computeRulesBys() {
    ruleById =
        rules.stream().collect(toMap(org.kockpit.rules.registry.model.Rule::getId, identity()));

    ruleByClass =
        rules.stream()
            .filter(rule -> nonNull(rule.getSourceClass()))
            .collect(toMap(RuleImpl::getSourceClass, identity()));
  }

  @Deprecated
  private static RuleImpl getOldCompatibilityRule(RuleNodesBuilderSupport ruleNodesBuilderSupport) {
    try {
      RuleNode ruleNode = ruleNodesBuilderSupport.configure();
      return new RuleImpl(
          ruleNodesBuilderSupport.getId(),
          0,
          ruleNode.getDetails(),
          ruleNode,
          ruleNodesBuilderSupport.getClass());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Beta version of KEngine JSON.
   *
   * @throws IOException
   */
  private void loadKEngineJson() throws IOException {
    KEngineJSon kEngineJSon = new KEngineJSon(getClass().getResourceAsStream(jsonFile));
    List<FlowJson> flowJsons = kEngineJSon.getFlowJsons();

    // Create flows
    flows = flowJsons.stream().map(this::convertToFlowModel).toList();

    // Create rules
    rules =
        flowJsons.stream()
            .map(FlowJson::getReferentials)
            .flatMap(List::stream)
            .map(this::convertToRuleImpl)
            .toList();

    computeRulesBys();
  }

  private void compute(Collection<org.kockpit.rules.registry.model.Flow> flows) {
    // Produce flows rules links
    flowRulesByFlowId = newComputeRulesByFlowId(flows);

    // Generate registry
    try {
      currentRegistry = computeCurrentRegistry();
    } catch (JacksonException e) {
      throw new IllegalStateException(e);
    }

    // Save registry
    saveRegistry();
  }

  @Deprecated
  private void saveRegistry() {
    if (isNull(registryDao)) {
      return;
    }
    registryDao.insert(currentRegistry);
  }

  private RuleImpl convertToRuleImpl(RuleJson ruleJson) {
    String id = ruleJson.getId();
    DocumentationDetails details = new SeamLessDetails(id, ruleJson.getName());
    RuleNode ruleNode = ruleJson.getRuleNode(applicationContext);
    return new RuleImpl(id, 0, details, ruleNode);
  }

  private RuleImpl convertToRuleImpl(Entry<String, Object> ruleAnnotation) {
    Rule declaredAnnotation = readRuleAnnotation(ruleAnnotation);

    String beanName = ruleAnnotation.getKey();
    Object bean = ruleAnnotation.getValue();
    String id = null;
    String documentation = null;
    if (nonNull(declaredAnnotation)) {
      id = declaredAnnotation.value();
      documentation = declaredAnnotation.documentation();
    }
    if (isEmpty(id)) {
      id = beanName;
    }
    DocumentationDetails details = new SeamLessDetails(id, documentation);

    // TODO review that
    RuleNode ruleNode;
    if (bean instanceof RuleNode) {
      ruleNode = (RuleNode) bean;
      if (nonNull(ruleNode.getDetails())) {
        details = ruleNode.getDetails();
        id = details.getCode();
      }
    } else if (bean instanceof RuleNodesBuilderSupport) {
      RuleNodesBuilderSupport support = (RuleNodesBuilderSupport) bean;
      try {
        ruleNode = support.configure();
      } catch (Exception e) {
        log.error("Can not create rule node for ruleNodesBuilderSupport: {}", support, e);
        throw new RuntimeException(e);
      }
    } else if (bean instanceof RuleNodesBuilderSeamLessSupport) {
      RuleNodesBuilderSeamLessSupport support = (RuleNodesBuilderSeamLessSupport) bean;
      try {
        ruleNode = support.configure();
        if (nonNull(ruleNode.getDetails())) {
          details = ruleNode.getDetails();
          id = ruleNode.getDetails().getCode();
        }
      } catch (Exception e) {
        log.error("Can not create rule node for ruleNodesBuilderSeamLessSupport: {}", support, e);
        throw new RuntimeException(e);
      }
    } else {
      log.error("No support for bean class : {}", bean.getClass());
      throw new RuntimeException("No support for bean class : " + bean.getClass());
    }

    if (isNull(ruleNode.getDetails())) {
      ruleNode.setDetails(details);
    }

    return new RuleImpl(id, 0, details, ruleNode, bean.getClass());
  }

  private Rule readRuleAnnotation(Entry<String, Object> ruleAnnotation) {
    Object bean = ruleAnnotation.getValue();
    Rule declaredAnnotation = bean.getClass().getDeclaredAnnotation(Rule.class);

    if (isNull(declaredAnnotation)) {
      String beanName = ruleAnnotation.getKey();
      BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) applicationContext;
      try {
        BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanName);
        Object source = beanDefinition.getSource();
        if (nonNull(source) && source instanceof AnnotatedTypeMetadata annotatedTypeMetadata) {
          MergedAnnotation<Rule> ruleMergedAnnotation =
              annotatedTypeMetadata.getAnnotations().get(Rule.class);
          declaredAnnotation = ruleMergedAnnotation.synthesize();
        }
      } catch (NoSuchBeanDefinitionException e) {
        // Nothing to do
      }
    }
    return declaredAnnotation;
  }

  private org.kockpit.rules.registry.model.Flow convertToFlowModel(
      Entry<String, Object> beanNameAndBean) {
    Flow declaredAnnotation = readFlowAnnotation(beanNameAndBean);
    return new SeamLessFlow(beanNameAndBean.getKey(), declaredAnnotation, ruleByClass);
  }

  private Flow readFlowAnnotation(Entry<String, Object> flowAnnotation) {
    Flow declaredAnnotation =
        flowAnnotation.getValue().getClass().getDeclaredAnnotation(Flow.class);
    if (isNull(declaredAnnotation)) {
      // Try to get it from bean definition registry
      BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) applicationContext;
      BeanDefinition beanDefinition =
          beanDefinitionRegistry.getBeanDefinition(flowAnnotation.getKey());
      if (beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
        MergedAnnotations annotations = annotatedBeanDefinition.getMetadata().getAnnotations();
        if (annotations.isPresent(Flow.class)) {
          declaredAnnotation = annotations.get(Flow.class).synthesize();
        }
      }
    }
    return declaredAnnotation;
  }

  private org.kockpit.rules.registry.model.Flow convertToFlowModel(FlowJson flowJson) {
    return new SeamLessFlow(flowJson);
  }

  private Map<String, List<org.kockpit.rules.registry.model.Rule>> newComputeRulesByFlowId(
      Collection<org.kockpit.rules.registry.model.Flow> flows1) {
    return flows1.stream()
        .map(this::getRulesListByFlow)
        .collect(toMap(Entry::getKey, Entry::getValue));
  }

  private SimpleEntry<String, List<org.kockpit.rules.registry.model.Rule>> getRulesListByFlow(
      org.kockpit.rules.registry.model.Flow flow) {
    List<org.kockpit.rules.registry.model.Rule> rules =
        flow.getEntries().stream().map(this::findRule).toList();
    return new SimpleEntry<>(flow.getId(), rules);
  }

  private org.kockpit.rules.registry.model.Rule findRule(FlowEntry flowEntry) {
    String entryId = flowEntry.getEntryId();
    org.kockpit.rules.registry.model.Rule rule = ruleById.get(entryId);
    if (isNull(rule)) {
      throw new IllegalArgumentException("Rule with id: " + entryId + " not found !");
    }
    return rule;
  }

  @Override
  protected RegistryImpl computeCurrentRegistry() throws JacksonException {
    // Compute rule specifications
    List<? extends RuleSpecification> currentRuleSpecifications =
        getRules().stream().map(tRule -> new RuleSpecificationImpl(tRule, detailHandler)).toList();

    RegistryImpl registry;

    // Compute flow specifications
    Optional<List<org.kockpit.rules.registry.model.Flow>> flows = getFlows();
    if (flows.isPresent()) {
      List<? extends FlowSpecification> flowSpecifications =
          flows.get().stream().map(flow -> new FlowSpecificationImpl(flow, detailHandler)).toList();
      registry = new RegistryImpl(registryName, currentRuleSpecifications, flowSpecifications);
    } else {
      registry = new RegistryImpl(registryName, currentRuleSpecifications, null);
    }

    // Compute hash
    String value = RegistryJson.mapper().writeValueAsString(registry);
    long hash = value.hashCode();

    // Return final registry with its id
    return new RegistryImpl(
        registryName, hash, registry.getRuleSpecifications(), registry.getFlowSpecifications());
  }

  @Deprecated
  @Override
  public Optional<List<org.kockpit.rules.registry.model.Flow>> getFlows() {
    return Optional.of(this.flows);
  }

  @Deprecated
  @Override
  public List<org.kockpit.rules.registry.model.Rule> getRules() {
    return new ArrayList<>(rules);
  }

  public List<org.kockpit.rules.registry.model.Rule> getRulesByFlowId(String id) {
    return flowRulesByFlowId.get(id);
  }

  @Override
  public Registry getCurrentRegistry() {
    return currentRegistry;
  }

  @Deprecated
  @Override
  public Optional<? extends Registry> getRegistry(long registryId) {
    return registryDao.get(registryId);
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  static class SeamLessDetails implements DocumentationDetails {
    private String code;
    private String documentation;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  static class SeamLessFlowEntry implements FlowEntry {
    private String entryId;
  }
}

@Data
@Slf4j
class SeamLessFlow implements org.kockpit.rules.registry.model.Flow {
  private final String id;
  private final DocumentationDetails details;
  private final List<SeamLessRegistry.SeamLessFlowEntry> entries;

  public SeamLessFlow(String beanName, Flow annotation, Map<Class, RuleImpl> ruleByClass) {
    if (isEmpty(annotation.id())) {
      id = beanName;
    } else {
      id = annotation.id();
    }

    details = new SeamLessRegistry.SeamLessDetails(id, annotation.documentation());

    if (isEmpty(annotation.ruleClasses())) {
      entries =
          Arrays.stream(annotation.ruleIds()).map(SeamLessRegistry.SeamLessFlowEntry::new).toList();
    } else {
      entries =
          Arrays.stream(annotation.ruleClasses())
              .map(ruleByClass::get)
              .map(RuleImpl::getId)
              .map(SeamLessRegistry.SeamLessFlowEntry::new)
              .toList();
    }
  }

  public SeamLessFlow(FlowJson flowJson) {
    id = flowJson.getId();
    details = new SeamLessRegistry.SeamLessDetails(id, flowJson.getName());
    entries =
        flowJson.getReferentials().stream()
            .map(RuleJson::getId)
            .map(SeamLessRegistry.SeamLessFlowEntry::new)
            .toList();
  }

  private String getRuleId(Class aClass) {
    Class<Rule> annotationClass = Rule.class;
    Rule declaredAnnotation = (Rule) aClass.getDeclaredAnnotation(annotationClass);
    if (isNull(declaredAnnotation)) {
      log.error("Given rule class: {} is not annotated with @Rule.", aClass);
      throw new IllegalArgumentException("Rule class " + aClass + " is not annotated with @Rule");
    }
    String ruleId = declaredAnnotation.value();
    if (isEmpty(ruleId)) {
      ruleId = normalizeComponentName(aClass.getSimpleName());
    }
    return ruleId;
  }

  public List<FlowEntry> getEntries() {
    return new ArrayList<>(entries);
  }
}
