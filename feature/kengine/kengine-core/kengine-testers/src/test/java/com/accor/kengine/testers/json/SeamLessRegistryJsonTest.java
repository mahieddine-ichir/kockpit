package com.accor.kengine.testers.json;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeExecutor;
import com.accor.kengine.execution.ExecutionResult;
import com.accor.kengine.registry.model.Rule;
import com.accor.kengine.registry.seamless.SeamLessRegistry;
import com.accor.kengine.testers.TesterContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = SeamLessJsonApplication.class)
@ComponentScan(basePackageClasses = {SeamLessRegistryJsonTest.class})
@ActiveProfiles("jsonregistry")
class SeamLessRegistryJsonTest {

  @Autowired private SeamLessRegistry registry;

  @Test
  void simpleFullExecution() {
    RuleNodeExecutor ruleNodeExecutor = new RuleNodeExecutor<>();
    List<Rule> rules = registry.getRulesByFlowId("hello");
    List<RuleNode> ruleNodes = rules.stream().map(Rule::getRuleNode).toList();
    TesterContext context =
        TesterContext.builder()
            .name("Cyril")
            //            .car(Car.builder()
            //                    .brand("Porsche")
            //                    .model("911")
            //                    .build())
            .build();
    ExecutionResult executionResult = ruleNodeExecutor.execute(ruleNodes, context);
    assertNotNull(executionResult);
  }
}
