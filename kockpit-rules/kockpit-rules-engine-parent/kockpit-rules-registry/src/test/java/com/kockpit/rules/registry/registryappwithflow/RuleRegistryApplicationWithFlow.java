package com.kockpit.rules.registry.registryappwithflow;

import com.kockpit.rules.DefaultDocumentationDetails;
import com.kockpit.rules.DocumentationDetails;
import com.kockpit.rules.registry.RuleNodeRegistry;
import com.kockpit.rules.registry.model.Flow;
import com.kockpit.rules.registry.model.FlowEntry;
import com.kockpit.rules.registry.registryapp.RuleRegistryApplication1;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.List;

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
