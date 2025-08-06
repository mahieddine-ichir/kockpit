package com.accor.wcp.obfuscation.impl.masker.maskers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DefaultMaskerTest {

  private final DefaultMasker defaultMasker = new DefaultMasker();

  @Test
  void should_return_default_masker_type() {
    assertThat(defaultMasker.getType()).isEqualTo("DEFAULT");
  }

  @Test
  void should_mask_data_with_default_masker_regex() {
    assertThat(defaultMasker.mask("This is a test")).isEqualTo("*");
  }

  @Test
  void should_not_mask_data_when_input_is_null() {
    assertThat(defaultMasker.mask(null)).isNull();
  }
}