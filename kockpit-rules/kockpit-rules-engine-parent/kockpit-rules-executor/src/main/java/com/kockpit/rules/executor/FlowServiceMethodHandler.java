package com.kockpit.rules.executor;

import com.kockpit.rules.action.RuleContext;
import com.kockpit.rules.execution.ExecutionResult;
import com.kockpit.rules.seemless.*;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.core.annotation.MergedAnnotations;
import org.thepavel.icomponent.handler.MethodHandler;
import org.thepavel.icomponent.metadata.MethodMetadata;
import org.thepavel.icomponent.metadata.ResolvedTypeMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.kockpit.rules.registry.seemless.NamingHelper.normalizeComponentName;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.*;

@Slf4j
public class FlowServiceMethodHandler implements MethodHandler {

  private final KEngineFlowRunnerImpl flowRunner;

  private final Map<MethodMetadata, FlowServiceCaller> methodMetadataCallerCache =
      new ConcurrentHashMap<>();

  public FlowServiceMethodHandler(KEngineFlowRunnerImpl flowRunner) {
    this.flowRunner = flowRunner;
  }

  interface AuditFlagResolver {
    boolean resolveAuditFlag(
        FlowServiceCaller flowServiceCaller, Object context, Object[] arguments);
  }

  interface NamedExecutionResolver {
    String resolveNamed(FlowServiceCaller flowServiceCaller, Object context, Object[] arguments);
  }

  static class StaticAuditFlagResolver implements AuditFlagResolver {

    static final StaticAuditFlagResolver INSTANCE_ENABLE = new StaticAuditFlagResolver(true);
    static final StaticAuditFlagResolver INSTANCE_DISABLE = new StaticAuditFlagResolver(false);

    private final boolean audit;

    StaticAuditFlagResolver(boolean audit) {
      this.audit = audit;
    }

    @Override
    public boolean resolveAuditFlag(
        FlowServiceCaller flowServiceCaller, Object context, Object[] arguments) {
      return audit;
    }
  }

  @Data
  static class ArgumentAuditFlagResolver implements AuditFlagResolver {

    private final int argumentIndex;

    ArgumentAuditFlagResolver(int argumentIndex) {
      this.argumentIndex = argumentIndex;
    }

    @Override
    public boolean resolveAuditFlag(
        FlowServiceCaller flowServiceCaller, Object context, Object[] arguments) {
      return Boolean.TRUE.equals(arguments[argumentIndex]);
    }
  }

  static class StaticNamedExecutionResolver implements NamedExecutionResolver {

    private final String named;

    StaticNamedExecutionResolver(String named) {
      this.named = named;
    }

    @Override
    public String resolveNamed(
        FlowServiceCaller flowServiceCaller, Object context, Object[] arguments) {
      return named;
    }
  }

  static class FlowIdNamedExecutionResolver implements NamedExecutionResolver {

    static final FlowIdNamedExecutionResolver INSTANCE = new FlowIdNamedExecutionResolver();

    @Override
    public String resolveNamed(
        FlowServiceCaller flowServiceCaller, Object context, Object[] arguments) {
      return flowServiceCaller.flowId;
    }
  }

  @Data
  static class ArgumentNamedExecutionResolver implements NamedExecutionResolver {

    private final int argumentIndex;

    ArgumentNamedExecutionResolver(int argumentIndex) {
      this.argumentIndex = argumentIndex;
    }

    @Override
    public String resolveNamed(
        FlowServiceCaller flowServiceCaller, Object context, Object[] arguments) {
      return "" + arguments[argumentIndex];
    }
  }

  @Data
  @Builder
  static class PropertyMetadata {
    private String name;
    private String sourceName;
    private Class<?> type;
    private Field field;
  }

  @Data
  @Builder
  static class CompositeReturnMetadata {
    private Class<?> type;
    private List<PropertyMetadata> propertyMetadataList;
  }

  @Data
  @Builder
  static class FlowServiceCaller {
    private MethodMetadata methodMetadata;
    private String flowId;
    private List<String> parameterNames;
    private boolean returnAValue;
    private ContextResult contextResult;
    private String returnPropertyName;
    private boolean returnTheFullContext;
    private AuditFlagResolver auditFlagResolver;
    private NamedExecutionResolver namedExecutionResolver;
    private boolean returnACompositeContext;
    private CompositeReturnMetadata compositeReturnMetadata;
  }

  @Override
  public Object handle(Object[] arguments, MethodMetadata methodMetadata) {
    // Static Metadata
    FlowServiceCaller flowServiceCaller = getFlowServiceCaller(methodMetadata);

    // Resolves
    Map<Object, Object> context = resolveContext(flowServiceCaller, arguments);
    String flowExecutionName =
        flowServiceCaller.namedExecutionResolver.resolveNamed(
            flowServiceCaller, context, arguments);
    boolean audit =
        flowServiceCaller.auditFlagResolver.resolveAuditFlag(flowServiceCaller, context, arguments);

    // Execution
    ExecutionResult executionResult =
        flowRunner.execute(flowServiceCaller.getFlowId(), context, flowExecutionName, audit);
    context.put(ExecutionResult.class, executionResult);
    context.put(
        KEngineExecutorHandleContextConstants.CONTEXT_FLOW_EXECUTION_RESULT_KEY, executionResult);

    // Return
    if (flowServiceCaller.returnAValue) {
      Object returnValue = resolveReturnValue(flowServiceCaller, context, executionResult);
      FlowHandlerExecutionContext flowHandlerExecutionContext =
          (FlowHandlerExecutionContext) context.get(FlowHandlerExecutionContext.class);
      flowHandlerExecutionContext.setResult(returnValue);
      return returnValue;
    } else {
      // Nothing to return
      return null;
    }
  }

  private Object resolveReturnValue(
      FlowServiceCaller flowServiceCaller, Object context, ExecutionResult executionResult) {
    // Nothing
    if (isNull(context)) {
      return null;
    }

    // A composite context return case
    if (flowServiceCaller.returnACompositeContext) {
      return resolveACompositeContext(flowServiceCaller, context);
    }

    // Full context return case
    if (flowServiceCaller.returnTheFullContext) {
      return context;
    }

    // Look for property in context
    return resolveReturnValueInContext(flowServiceCaller, context);
  }

  private Object resolveACompositeContext(FlowServiceCaller flowServiceCaller, Object context) {
    Class<?> klass = flowServiceCaller.compositeReturnMetadata.getType();
    try {
      final Object compositeContextToReturn =
          ConstructorUtils.invokeConstructor(klass, new Object[] {});
      flowServiceCaller
          .compositeReturnMetadata
          .getPropertyMetadataList()
          .forEach(
              propertyMetadata -> {
                Object propertyValue =
                    getPropertyValueFromContext(context, propertyMetadata.getSourceName());
                setPropertyValueTo(
                    compositeContextToReturn, propertyMetadata.getName(), propertyValue);
              });
      // Special case to put back composite result into context
      if (context instanceof Map map) {
        map.put(compositeContextToReturn.getClass(), compositeContextToReturn);
      }

      return compositeContextToReturn;
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException
        | InstantiationException e) {
      log.error("Error creating composite context of type: {}. Exception: {}", klass, e);
    }
    return null;
  }

  private static void setPropertyValueTo(Object to, String name, Object value) {
    try {
      BeanUtils.setProperty(to, name, value);
    } catch (IllegalAccessException | InvocationTargetException e) {
      log.warn("Error setting value: ({}={}) to : {}. Exception: {}", name, value, to, e);
    }
  }

  private static Object resolveReturnValueInContext(
      FlowServiceCaller flowServiceCaller, Object context) {
    String propertyNameInContext = flowServiceCaller.returnPropertyName;
    return getPropertyValueFromContext(context, propertyNameInContext);
  }

  private static Object getPropertyValueFromContext(Object context, String propertyNameInContext) {
    try {
      return PropertyUtils.getNestedProperty(context, propertyNameInContext);
    } catch (IllegalAccessException | InvocationTargetException e) {
      log.info(
          "Error getting value for result: {} in context: {}. Exception: {}",
          propertyNameInContext,
          context,
          e);
      return null;
    } catch (NoSuchMethodException e) {
      log.info(
          "Value not found for result: {} in context: {}. Exception: {}",
          propertyNameInContext,
          context,
          e);
      return null;
    }
  }

  private Map<Object, Object> resolveContext(
      FlowServiceCaller flowServiceCaller, Object[] arguments) {
    Map<Object, Object> context = new HashMap<>();

    // Create context initial data from arguments
    for (int i = 0; i < flowServiceCaller.getParameterNames().size(); i++) {
      String name = flowServiceCaller.getParameterNames().get(i);
      context.put(name, arguments[i]);
    }

    // Automatic put FlowHandlerInputs into context for external usages
    FlowHandlerExecutionContext flowHandlerExecutionContext =
        FlowHandlerExecutionContext.builder()
            .arguments(arguments)
            .methodMetadata(flowServiceCaller.getMethodMetadata())
            .build();
    context.put(FlowHandlerExecutionContext.class, flowHandlerExecutionContext);

    // Inception context in context
    context.put("ruleContext", new RuleContextImpl(context));
    return context;
  }

  FlowServiceCaller getFlowServiceCaller(MethodMetadata methodMetadata) {
    return methodMetadataCallerCache.computeIfAbsent(
        methodMetadata, this::computeFlowServiceCaller);
  }

  private FlowServiceCaller computeFlowServiceCaller(MethodMetadata methodMetadata) {
    String flowId = computeFlowId(methodMetadata);
    List<String> parameterNames = computeParameterNames(methodMetadata);
    ContextResult contextResult = computeContextResult(methodMetadata);
    String returnPropertyName = computeReturnPropertyName(methodMetadata, contextResult);
    boolean hasReturnValue = computeHasReturnValue(methodMetadata);
    CompositeReturnMetadata compositeReturnMetadata =
        computeReturnACompositeContext(methodMetadata);
    boolean returnTheFullContext = computeReturnTheFullContext(contextResult, returnPropertyName);
    AuditFlagResolver auditFlagResolver = computeAuditFlagResolver(methodMetadata);
    NamedExecutionResolver namedExecutionResolver = computeNamedExecutionResolver(methodMetadata);
    return FlowServiceCaller.builder()
        .methodMetadata(methodMetadata)
        .flowId(flowId)
        .parameterNames(parameterNames)
        .contextResult(contextResult)
        .returnPropertyName(returnPropertyName)
        .returnAValue(hasReturnValue)
        .returnTheFullContext(returnTheFullContext)
        .auditFlagResolver(auditFlagResolver)
        .namedExecutionResolver(namedExecutionResolver)
        .returnACompositeContext(nonNull(compositeReturnMetadata))
        .compositeReturnMetadata(compositeReturnMetadata)
        .build();
  }

  private CompositeReturnMetadata computeReturnACompositeContext(MethodMetadata methodMetadata) {
    Class<?> returnType = methodMetadata.getSourceMethod().getReturnType();
    ContextResult annotation = returnType.getAnnotation(ContextResult.class);
    if (isNull(annotation)) {
      return null;
    }

    // Read properties to return
    List<PropertyMetadata> propertyMetadataList =
        Stream.of(returnType.getSuperclass().getDeclaredFields(), returnType.getDeclaredFields())
            .flatMap(Arrays::stream)
            .map(this::computePropertyMetadata)
            .toList();

    return CompositeReturnMetadata.builder()
        .type(returnType)
        .propertyMetadataList(propertyMetadataList)
        .build();
  }

  private PropertyMetadata computePropertyMetadata(Field field) {
    String name = field.getName();
    String sourceName = field.getName();
    ContextResult annotation = field.getAnnotation(ContextResult.class);
    if (nonNull(annotation)) {
      annotation = MergedAnnotations.from(annotation).get(ContextResult.class).synthesize();
      String propertyName = annotation.propertyName();
      if (isNoneEmpty(propertyName)) {
        sourceName = propertyName;
      }
    }
    return PropertyMetadata.builder()
        .name(name)
        .sourceName(sourceName)
        .type(field.getType())
        .field(field)
        .build();
  }

  private NamedExecutionResolver computeNamedExecutionResolver(MethodMetadata methodMetadata) {
    // Default case (flow execution = flow id)
    MergedAnnotations annotations = methodMetadata.getAnnotations();
    if (!annotations.isPresent(NamedFlowExecution.class)) {
      return FlowIdNamedExecutionResolver.INSTANCE;
    }

    NamedFlowExecution namedFlowExecution = annotations.get(NamedFlowExecution.class).synthesize();
    // Flow Id
    if (namedFlowExecution.useFlowId()) {
      return FlowIdNamedExecutionResolver.INSTANCE;
    }

    // Property name (by argument)
    String parameterName = namedFlowExecution.parameterName();
    if (isNotBlank(parameterName)) {
      Integer index = findArgumentNameIndex(methodMetadata, parameterName);
      if (nonNull(index)) {
        return new ArgumentNamedExecutionResolver(index);
      }
    }

    // Fixed name
    String staticName = namedFlowExecution.name();
    if (isNotBlank(staticName)) {
      return new StaticNamedExecutionResolver(staticName);
    }

    // Default case
    return FlowIdNamedExecutionResolver.INSTANCE;
  }

  private Integer findArgumentNameIndex(MethodMetadata methodMetadata, String parameterName) {
    Parameter[] parameters = methodMetadata.getSourceMethod().getParameters();
    List<Parameter> parameterList = Arrays.stream(parameters).toList();
    return parameterList.stream()
        .filter(p -> parameterName.equals(p.getName()))
        .findFirst()
        .map(parameterList::indexOf)
        .orElse(null);
  }

  private AuditFlagResolver computeAuditFlagResolver(MethodMetadata methodMetadata) {
    // Default case (audit is enable)
    MergedAnnotations annotations = methodMetadata.getAnnotations();
    if (!annotations.isPresent(AuditFlowExecution.class)) {
      return StaticAuditFlagResolver.INSTANCE_ENABLE;
    }

    AuditFlowExecution auditFlowExecution =
        methodMetadata.getAnnotations().get(AuditFlowExecution.class).synthesize();
    // Argument
    String argumentName = auditFlowExecution.parameterName();
    if (isNotBlank(argumentName)) {
      Integer index = findArgumentNameIndex(methodMetadata, argumentName);
      if (nonNull(index)) {
        return new ArgumentAuditFlagResolver(index);
      }
    }

    // Fixed
    if (!auditFlowExecution.audit()) {
      return StaticAuditFlagResolver.INSTANCE_DISABLE;
    }

    return StaticAuditFlagResolver.INSTANCE_ENABLE;
  }

  private boolean computeReturnTheFullContext(
      ContextResult contextResult, String returnPropertyName) {
    // No property defined with annotation (= blank) so return full context (as a Map)
    return nonNull(contextResult) && returnPropertyName.isBlank();
  }

  private boolean computeHasReturnValue(MethodMetadata methodMetadata) {
    return !"void".equals(methodMetadata.getReturnTypeMetadata().getResolvedType().getTypeName());
  }

  private String computeReturnPropertyName(
      MethodMetadata methodMetadata, ContextResult contextResult) {
    String returnPropertyName;
    if (nonNull(contextResult)) {
      returnPropertyName = contextResult.value();
    } else {
      returnPropertyName = methodMetadata.getSourceMethod().getName();
    }
    return returnPropertyName;
  }

  private ContextResult computeContextResult(MethodMetadata methodMetadata) {
    ResolvedTypeMetadata returnTypeMetadata = methodMetadata.getReturnTypeMetadata();
    MergedAnnotations annotations = returnTypeMetadata.getAnnotations();
    if (annotations.isPresent(ContextResult.class)) {
      return annotations.get(ContextResult.class).synthesize();
    }
    return null;
  }

  private List<String> computeParameterNames(MethodMetadata methodMetadata) {
    return Arrays.stream(methodMetadata.getSourceMethod().getParameters())
        .map(this::computeParameterName)
        .toList();
  }

  private String computeParameterName(Parameter parameter) {
    MergedAnnotations mergedAnnotations = MergedAnnotations.from(parameter);
    if (mergedAnnotations.isPresent(ContextParameter.class)) {
      ContextParameter contextParameter =
          mergedAnnotations.get(ContextParameter.class).synthesize();
      if (isNotBlank(contextParameter.value())) {
        return contextParameter.value();
      }
    }
    return parameter.getName();
  }

  private String computeFlowId(MethodMetadata methodMetadata) {
    Class<?> declaringClass = methodMetadata.getSourceClassMetadata().getSourceClass();
    Annotation[] annotations = declaringClass.getAnnotations();
    Flow flow =
        Arrays.stream(annotations)
            .filter(Flow.class::isInstance)
            .findFirst()
            .map(Flow.class::cast)
            .map(flow1 -> MergedAnnotations.from(flow1).get(Flow.class).synthesize())
            .orElseThrow();
    if (isBlank(flow.id())) {
      return normalizeComponentName(declaringClass.getSimpleName());
    }
    return flow.id();
  }
}
