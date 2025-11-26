package org.kockpit.audit.openapi;


import io.swagger.v3.oas.annotations.Operation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class OperationIdSetterInterceptorTest {

  @Operation(operationId = "operationId1")
  public void fakeMethodWithAnnotation() {}

  public void fakeMethodWithoutAnnotation() {}

  @BeforeEach
  void reset() {
    MDC.clear();
  }

  @Test
  void should_prehandle_operationid() throws NoSuchMethodException {
    // Given
    OperationIdSetterInterceptor underTest = new OperationIdSetterInterceptor(null);

    HandlerMethod methodWithAnnotationHandler =
        new HandlerMethod(new Object(), getClass().getMethod("fakeMethodWithAnnotation"));
    HandlerMethod methodWithoutAnnotationHandler =
        new HandlerMethod(new Object(), getClass().getMethod("fakeMethodWithoutAnnotation"));

    // When
    underTest.preHandle(null, null, methodWithAnnotationHandler);

    // Then
    assertThat(MDC.get("operationId")).isEqualTo("operationId1");
    MDC.clear();

    // When
    underTest.preHandle(null, null, methodWithoutAnnotationHandler);

    // Then
    assertThat(MDC.get("operationId")).isNull();

    // When
    underTest.preHandle(null, null, new Object());

    // Then
    assertThat(MDC.get("operationId")).isNull();
  }

  @Test
  void should_prehandle_without() throws NoSuchMethodException {
    // Given
    OperationIdSetterInterceptor underTest = new OperationIdSetterInterceptor(null);

    HandlerMethod methodWithoutAnnotationHandler =
        new HandlerMethod(new Object(), getClass().getMethod("fakeMethodWithoutAnnotation"));

    // When
    underTest.preHandle(null, null, methodWithoutAnnotationHandler);

    // Then
    assertThat(MDC.get("operationId")).isNull();
  }

  @Test
  void should_prehandle_not_methodhandler() throws NoSuchMethodException {
    // Given
    OperationIdSetterInterceptor underTest = new OperationIdSetterInterceptor(null);

    // When
    underTest.preHandle(null, null, new Object());

    // Then
    assertThat(MDC.get("operationId")).isNull();
  }
}
