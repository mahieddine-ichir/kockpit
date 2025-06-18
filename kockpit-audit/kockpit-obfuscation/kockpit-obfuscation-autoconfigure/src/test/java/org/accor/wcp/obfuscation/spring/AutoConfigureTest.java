package com.accor.wcp.obfuscation.spring;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.kockpit.audit.obfuscation.ObfuscationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AutoConfigureTest {

  @Autowired
  private ObfuscationService obfuscationService;

  @Test
  void should_initialize_obfuscation() {
    assertThat(obfuscationService).isNotNull();
  }
}
