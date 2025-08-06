package com.accor.wcp.sdk.application.service.dynaconfig;

import com.accor.wcp.sdk.application.service.dynaconfig.configproperties.ApplicationProperties;
import java.lang.reflect.Field;
import java.util.List;

import com.accor.wcp.sdk.application.service.dynaconfig.configproperties.IssueNoValueForProperties;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.jupiter.api.Assertions.*;

class DynaConfigSpringBeanProcessorTest {

  @Value("${test.property.name:default value}")
  private String testValueAnnotation;

  @Value("${test.property.complex:#{dateTime.get()}}")
  private String anotherComplexValueAnnotated;

  @Test
  void computeValueAnnotationPropertyName() throws NoSuchFieldException {
    Field field = DynaConfigSpringBeanProcessorTest.class.getDeclaredField("testValueAnnotation");
    String propertyName = DynaConfigSpringBeanProcessor.computeValueAnnotationPropertyName(field);
    assertEquals("test.property.name", propertyName);
  }

  @Test
  void computeValueAnnotationPropertyNameComplex() throws NoSuchFieldException {
    Field field =
        DynaConfigSpringBeanProcessorTest.class.getDeclaredField("anotherComplexValueAnnotated");
    String propertyName = DynaConfigSpringBeanProcessor.computeValueAnnotationPropertyName(field);
    assertEquals("test.property.complex", propertyName);
  }

  @Test
  void computeDynaConfigTargetMetadataWithConfigurationPropertiesObject() {
    ApplicationProperties applicationProperties = new ApplicationProperties();
    List<DynaConfigTargetMetadata> dynaConfigTargetMetadata =
        new DynaConfigSpringBeanProcessor(null, null, null)
            .computeDynaConfigTargetMetadata(applicationProperties);
    assertNotNull(dynaConfigTargetMetadata);
    assertEquals(8, dynaConfigTargetMetadata.size());
  }

  @Test
  void computeDynaConfigTargetMetadataWithNoDefaultValueAtInitialization() {
    IssueNoValueForProperties applicationProperties = new IssueNoValueForProperties();
    List<DynaConfigTargetMetadata> dynaConfigTargetMetadata =
            new DynaConfigSpringBeanProcessor(null, null, null)
                    .computeDynaConfigTargetMetadata(applicationProperties);
    assertNotNull(dynaConfigTargetMetadata);
    assertEquals(1, dynaConfigTargetMetadata.size());
    assertNull(dynaConfigTargetMetadata.get(0).getInitialValue());
  }

  @DynaConfigEnabler
  public class Dummy {
    @DynaConfigAttribute(value = "test")
    private String test;
  }

  @Test
  void computeDynaConfigTargetMetadataWithProxy() {
    ProxyFactory factory = new ProxyFactory();
    Dummy dummy = new Dummy();
    factory.setTarget(dummy);
    Dummy proxy = (Dummy) (factory.getProxy());
    List<DynaConfigTargetMetadata> dynaConfigTargetMetadata =
        new DynaConfigSpringBeanProcessor(null, null, null).computeDynaConfigTargetMetadata(proxy);
    assertNotNull(dynaConfigTargetMetadata);
    assertEquals(1, dynaConfigTargetMetadata.size());
  }
}
