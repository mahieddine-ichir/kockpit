package com.accor.kengine.registry.seamless.json;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.accor.kengine.registry.seamless.SeamLessRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = SeamLessJsonApplication.class)
@ComponentScan(basePackageClasses = {SeamLessRegistryJsonTest.class})
@ActiveProfiles("testjson")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SeamLessRegistryJsonTest {

  @Autowired private SeamLessRegistry registry;

  @Test
  void testRegistryLoad() {
    assertTrue(registry.getFlows().isPresent());
    assertThat(registry.getFlows().get()).hasSize(1);
    assertNotNull(registry);
    assertNotNull(registry.getRules());
    assertThat(registry.getRules()).hasSize(2);
  }
}
