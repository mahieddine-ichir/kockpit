package com.accor.kengine.registry;

import static org.junit.jupiter.api.Assertions.*;

import com.accor.kengine.registry.model.Rule;
import com.accor.kengine.registry.registryappwithflow.MockLocalRegistryDao;
import com.accor.kengine.registry.registryappwithflow.RuleNodeBuilderFakeForTestFlow1;
import com.accor.kengine.registry.registryappwithflow.RuleRegistryApplicationWithFlow;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = RuleRegistryApplicationWithFlow.class)
class RuleNodeRegistryWithFlowTest {

  @Autowired private RuleNodeRegistry<String> registry;

  @Autowired private MockLocalRegistryDao mockLocalRegistryDao;

  @Test
  public void testRegistryLoad() {
    assertTrue(registry.getFlows().isPresent());
    assertNotNull(registry);
    assertNotNull(registry.getRules());
    assertEquals(2, registry.getRules().size());

    // Testing flow
    List<Rule<String>> flow1Rules = registry.getRulesByFlowId("flow1");
    assertEquals(2, flow1Rules.size());
    assertEquals(RuleNodeBuilderFakeForTestFlow1.class.getSimpleName(), flow1Rules.get(1).getId());

    // Registry
    assertNotNull(mockLocalRegistryDao.getRegistry());
  }
}
