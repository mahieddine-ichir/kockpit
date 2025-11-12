package org.kockpit.audit.openapi;

import static java.util.Objects.nonNull;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditorKeyValueService;
import org.kockpit.audit.api.IndexedKeyValue;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
class OperationIdSetterInterceptor implements HandlerInterceptor {
    private final AuditorKeyValueService auditorKeyValueService;

  private final Map<Method, Operation> handlerMethodOperationCacheMap = new ConcurrentHashMap<>();

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (handler instanceof HandlerMethod handlerMethod) {
      // Cache method => operation annotation (as it is static)
      Operation operation =
          handlerMethodOperationCacheMap.computeIfAbsent(
              handlerMethod.getMethod(), h -> handlerMethod.getMethodAnnotation(Operation.class));
      if (nonNull(operation)) {
        logOperationId(operation.operationId());
      }
    }
    return true;
  }

  private void logOperationId(String logOperationId) {
      if (nonNull(auditorKeyValueService)) {
          auditorKeyValueService.addIndexedKeyValues(List.of(IndexedKeyValue.of("operationId", logOperationId)));
      }
    MDC.put("operationId", logOperationId);
  }
}
