package com.accor.kengine.registry.registryappwithflow;

import com.accor.kengine.DefaultDocumentationDetails;
import com.accor.kengine.DocumentationDetails;
import com.accor.kengine.registry.RuleNodeRegistry;
import com.accor.kengine.registry.model.Flow;
import com.accor.kengine.registry.model.FlowEntry;
import com.accor.kengine.registry.registryapp.RuleRegistryApplication1;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(RuleNodeRegistry.class)
@ComponentScan(
    basePackageClasses = {RuleRegistryApplication1.class, RuleRegistryApplicationWithFlow.class})
public class RuleRegistryApplicationWithFlow {

  @Bean
  public Flow flow1() {
    return new Flow() {
      @Override
      public String getId() {
        return "flow1";
      }

      @Override
      public DocumentationDetails getDetails() {
        return new DefaultDocumentationDetails(this.getClass().getSimpleName(), "Flow Details 1");
      }

      @Override
      public List<FlowEntry> getEntries() {
        FlowEntry flowEntry1 = () -> "RuleNodeBuilderFakeForTest1";
        FlowEntry flowEntry2 = () -> "RuleNodeBuilderFakeForTestFlow1";

        return Arrays.asList(flowEntry1, flowEntry2);
      }
    };
  }
}
