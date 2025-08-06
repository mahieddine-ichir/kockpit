package com.accor.kengine.starter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.accor.kengine.registry.seamless.SeamLessRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = KEngineStarterTesterApp.class)
@ActiveProfiles("stater")
class SeamLessV32ApplicationTest {

  @Autowired private SeamLessRegistry registry;

  @Autowired private NoopFlow noopFlow;

  @Test
  void should_start_normally_with_registry() {
    // Given
    // Start ok

    // Then
    assertNotNull(registry);
    assertNotNull(noopFlow);
  }
}
