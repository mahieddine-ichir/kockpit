package com.accor.kengine.registry;

import com.accor.kengine.registry.registryapp.RuleRegistryApplication1;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = RuleRegistryApplication1.class)
class RuleNodeRegistryTest {

  @Autowired private RuleNodeRegistry<String> registry;

  @Test
  public void testRegistryLoad() {
    assertTrue(registry.getFlows().isPresent());
    assertTrue(registry.getFlows().get().isEmpty());
    assertNotNull(registry);
    assertNotNull(registry.getRules());
    assertEquals(1, registry.getRules().size());
  }
}
