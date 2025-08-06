package com.accor.wcp.flow;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlowContextContainerTest {

  FlowContextContainer underTest;

  @BeforeEach
  void init() {
    underTest = new FlowContextContainer();
  }

  @Test
  void should_return_stored_context() {
    Object context = new Object();
    underTest.setContext(Object.class, context);

    assertThat(underTest.getContext(Object.class)).isEqualTo(context);
  }

  @Test
  void should_return_null_when_stored_context_does_not_exist() {
    assertThat(underTest.getContext(Object.class)).isNull();
  }

  @Test
  void should_return_optional_empty_when_stored_context_does_not_exist() {
    assertThat(underTest.getOptionalContext(Object.class)).isEmpty();
  }

  @Test
  void should_return_optional_when_stored_context_exist() {
    Object context = new Object();
    underTest.setContext(Object.class, context);

    assertThat(underTest.getOptionalContext(Object.class)).isNotEmpty();
  }

  @Test
  void should_return_context_by_classname() {
    Object context = new Object();
    underTest.setContext(Object.class, context);

    assertThat(underTest.get(Object.class.getSimpleName())).isNotNull();
  }
}
