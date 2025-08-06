package com.accor.kengine.registry.seamless.v32;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.accor.kengine.registry.seamless.SeamLessRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = SeamLessApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SeamLessRegistryAnnotationsTest {

  @Autowired private SeamLessRegistry registry;

  @Test
  void testRegistryLoad() {
    assertTrue(registry.getFlows().isPresent());
    assertThat(registry.getFlows().get()).hasSize(3);
    assertNotNull(registry);
    assertNotNull(registry.getRules());
    assertEquals(3, registry.getRules().size());
  }
}
