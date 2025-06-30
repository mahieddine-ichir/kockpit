package org.kockpit.audit.annotation;

import static org.kockpit.audit.annotation.AuditAttributesAnnotationProcessor.extractKeyValueListFromObjectField;
import static org.kockpit.audit.annotation.AuditAttributesAnnotationProcessor.getAuditAttributes;
import static org.kockpit.audit.annotation.AuditAttributesAnnotationProcessor.isElementAnnotatedWithAuditAttribute;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.kockpit.audit.annotation.AuditAttributesAnnotationProcessorTest.ExampleAuditedClass.ExampleErrorClass;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuditAttributesAnnotationProcessorTest {

  AuditAttributesAnnotationProcessor underTest;

  @BeforeEach
  public void setUp() {
    underTest = new AuditAttributesAnnotationProcessor();
  }

  public String methodForTest(
      @AuditAttribute String input1,
      @AuditAttribute(key = "key2_1_attr", path = "attributeWithNoKey")
          @AuditAttribute(key = "key2_2_code", path = "error.code")
          ExampleAuditedClass objectToAudit) {
    return "Fake string";
  }

  @Test
  void should_extractKeyValueListFromObjectField()
      throws NoSuchMethodException, NoSuchFieldException, InvocationTargetException,
          IllegalAccessException, InstantiationException {
    // Given
    Method methodForTest =
        AuditAttributesAnnotationProcessorTest.class.getMethod(
            "methodForTest", String.class, ExampleAuditedClass.class);
    Parameter parameter1 = methodForTest.getParameters()[0];
    Parameter parameter2 = methodForTest.getParameters()[1];
    assertTrue(isElementAnnotatedWithAuditAttribute(parameter1));
    assertTrue(isElementAnnotatedWithAuditAttribute(parameter2));
    AuditAttribute[] auditAttributes1 = getAuditAttributes(parameter1);
    AuditAttribute[] auditAttributes2 = getAuditAttributes(parameter2);

    ExampleAuditedClass auditedClass = new ExampleAuditedClass();
    auditedClass.attributeWithNoKey = "attributeWithNoKey_value";
    auditedClass.error = new ExampleErrorClass("code1", "msg1");

    // When
    List<Entry<String, Object>> entries1 =
        extractKeyValueListFromObjectField(
            auditAttributes1, "input1", Optional.of("parameterValue1"));
    List<Entry<String, Object>> entries2 =
        extractKeyValueListFromObjectField(
            auditAttributes2, "objectToAudit", Optional.of(auditedClass));

    // Then
    assertNotNull(entries1);
    assertNotNull(entries2);
    assertThat(entries1).hasSize(1);
    assertThat(entries2).hasSize(2);
  }

  @Test
  void should_extract_audited_entries_from_annotation_attributes() throws Exception {
    ExampleAuditedClass classWithAuditedAttr = new ExampleAuditedClass();

    Map<String, Object> maps = underTest.processAnnotation(classWithAuditedAttr);

    assertThat(maps)
        .hasSize(6)
        .containsEntry("attributeWithNoKey", "stringAttribute")
        .containsEntry("subClassAttribute", "code1")
        .containsEntry("subSubClassAttribute", "error_detail")
        .containsEntry("listOfSubClassAttributes", "code2,code3")
        .containsEntry("listOfSubSubClassAttributes", "error_detail2,error_detail3")
        .containsEntry("auditedMethod", "Result of audited method");
    assertThat(maps.get("attributeNotAudited")).isNull();
  }

  @Audited
  class ExampleAuditedClass {
    @AuditAttribute String attributeWithNoKey;

    @AuditAttribute(key = "subClassAttribute", path = "error.code")
    @AuditAttribute(key = "subSubClassAttribute", path = "error.detail.msg")
    ExampleErrorClass error;

    @AuditAttribute(key = "listOfSubClassAttributes", path = "errors.code")
    @AuditAttribute(key = "listOfSubSubClassAttributes", path = "errors.detail.msg")
    List<ExampleErrorClass> errors;

    private String attributeNotAudited;

    @AuditAttribute
    public String auditedMethod() {
      return "Result of audited method";
    }

    public ExampleAuditedClass() {
      this.attributeWithNoKey = "stringAttribute";
      this.attributeNotAudited = "noAudit";
      this.error = new ExampleErrorClass("code1", "error_detail");
      this.errors =
          List.of(
              new ExampleErrorClass("code2", "error_detail2"),
              new ExampleErrorClass("code3", "error_detail3"));
    }

    static class ExampleErrorClass {
      private String code;
      private ExampleErrorDetailClass detail;

      public ExampleErrorClass(String code, String msg) {
        this.code = code;
        this.detail = new ExampleErrorDetailClass(msg);
      }
    }

    static class ExampleErrorDetailClass {
      private String msg;

      public ExampleErrorDetailClass(String msg) {
        this.msg = msg;
      }
    }
  }
}
