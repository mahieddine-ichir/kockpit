package com.accor.kengine;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

class DefaultDetailHandlerTest {

  @Test
  void handle_null() {
    // Given
    DefaultDetailHandler underTest = new DefaultDetailHandler();

    // When
    SimpleDetail detail = underTest.handle(null);

    // Then
    assertThat(detail).isNotNull();
  }

  @Test
  void handle_normal_case() {
    // Given
    DefaultDetailHandler underTest = new DefaultDetailHandler();

    // When
    SimpleDetail detail =
        underTest.handle(
            new DocumentationDetails() {
              @Override
              public String getCode() {
                return "code";
              }

              @Override
              public String getDocumentation() {
                return "documentation";
              }
            });

    // Then
    assertThat(detail).isNotNull();
    assertThat(detail.getCode()).isEqualTo("code");
    assertThat(detail.getName()).isEqualTo("code");
    assertThat(detail.getDescription()).isEqualTo("documentation");
  }
}
