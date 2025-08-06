package com.accor.wcp.obfuscation.impl.masker.maskers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class EmailMaskerTest {

  private final EmailMasker emailMasker = new EmailMasker();

  @Test
  void should_return_email_masker_type() {
    assertThat(emailMasker.getType()).isEqualTo("email");
  }

  @Test
  void should_mask_data_with_email_masker_regex() {
    assertThat(emailMasker.mask("thisis.a_test@testmail.com")).isEqualTo("t************@t***********");
    assertThat(emailMasker.mask("@testmail.com")).isEqualTo("@t***********");
    assertThat(emailMasker.mask("thisis.a_test@")).isEqualTo("t************@");

  }

  @Test
  void should_mask_invalid_email_with_2_at() {
    assertThat(emailMasker.mask("thisis.a_test@testing@testmail.com")).isEqualTo("*************@*******@************");
  }

  @Test
  void should_mask_invalid_email_with_multiple_at() {
    assertThat(emailMasker.mask("thisis.a_test@testing@invalid@testmail.com")).isEqualTo("*************@*******@*******@************");
  }

  @Test
  void should_mask_invalid_email_without_at() {
    assertThat(emailMasker.mask("thisis.a_test_email")).isEqualTo("*******************");
  }

  @Test
  void should_not_mask_data_when_input_is_null() {
    assertThat(emailMasker.mask(null)).isNull();
  }
}