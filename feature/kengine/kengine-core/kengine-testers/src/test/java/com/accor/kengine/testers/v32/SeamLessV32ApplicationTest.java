package com.accor.kengine.testers.v32;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeExecutor;
import com.accor.kengine.execution.ExecutionResult;
import com.accor.kengine.registry.model.Rule;
import com.accor.kengine.registry.seamless.SeamLessRegistry;
import com.accor.kengine.testers.TesterContext;
import com.accor.kengine.testers.model.Car;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = SeamLessV32Application.class)
// @ComponentScan(basePackageClasses = {SeamLessV32ApplicationTest.class})
@ActiveProfiles("v32")
class SeamLessV32ApplicationTest {

  @Autowired private HelloFlow helloFlow;

  @Autowired private SeamLessRegistry registry;

  @Test
  void should_execute_helloflow_through_old_standard_way() {
    // Given
    TesterContext context = computeTesterContext();

    // When
    RuleNodeExecutor ruleNodeExecutor = new RuleNodeExecutor<>();
    List<Rule> rules = registry.getRulesByFlowId("hello");
    List<RuleNode> ruleNodes = rules.stream().map(Rule::getRuleNode).toList();

    ExecutionResult executionResult = ruleNodeExecutor.execute(ruleNodes, context);

    // Then
    assertNotNull(executionResult);
    assertThat(context.getGreetings()).isEqualTo("Hello world Cyril");
  }

  private static TesterContext computeTesterContext() {
    TesterContext context =
        TesterContext.builder()
            .name("Cyril")
            .car(Car.builder().brand("Pigeot").model("103").build())
            .car2(Car.builder().brand("Rinault").model("R8").build())
            .build();
    return context;
  }

  @Test
  void should_execute_helloflow_through_autoproxy_generation() {
    // When
    // Use interface auto-proxy generation
    String helloResult4Cyril = helloFlow.sayHello("Cyril");
    String helloResult4Bob = helloFlow.sayHello("Bob");

    // Then
    assertThat(helloResult4Cyril).isNotNull().isEqualTo("Hello world Cyril");
    assertThat(helloResult4Bob).isNotNull().isEqualTo("Hello world Bob");
  }
}
