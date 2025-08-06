package com.accor.kengine.seamless;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import com.accor.kengine.seamless.DynamicEndpointHelper.DynamicEndpoint;
import com.accor.kengine.seamless.DynamicEndpointHelper.DynamicParameter;
import com.accor.kengine.seamless.context.Cat;
import com.accor.kengine.seamless.context.Dog;
import com.accor.kengine.seamless.context.House;
import com.accor.kengine.seamless.context.TestContext;
import com.accor.kengine.seamless.context.TestInnerContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.junit.jupiter.api.Test;

class DynamicEndpointHelperTest {

  static final String VALUE_OK = "OK";

  static class TesterClass {
    @Action
    public void methodToRead(String string, Map mapToBind, Cat aNewCat) {}
  }

  static class ContextAsParameterMethod {
    @Action
    public void methodWithContext(TestContext context) {
      context.setMyResult(VALUE_OK);
    }
  }

  static class ContextParameterMethod {
    @Action
    @ContextResult
    public String myResult(
        @ContextParameter("cats[0].name") String firstCatName,
        @ContextParameter("innerContext.myHouse") House myHouse) {
      return firstCatName + " leaves " + myHouse.getName();
    }
  }

  @Getter
  static class NoParameterMethod {

    private boolean executed;

    @Action
    public void methodWithContext() {
      executed = true;
    }
  }

  static class NoAnnotationMethod {
    public void methodWithContext(TestContext context) {
      context.setMyResult(VALUE_OK);
    }
  }

  static class NoAnnotationMethods {
    public void method1(TestContext context) {}

    public void method2(TestContext context) {}
  }

  static class TesterClassWithMultipleMethods {
    @Action
    public void methodToRead(String string, Map mapToBind, Cat aNewCat) {}

    @Action
    public void anotherOne(String string, Map mapToBind, Cat aNewCat) {}
  }

  @Test
  void should_read_method_and_return_dynamiqueendpoint() {
    DynamicEndpoint<Action> dynamicEndpoint =
        DynamicEndpointHelper.readDynamicEndpoint(new TesterClass(), Action.class);

    assertThat(dynamicEndpoint).isNotNull();
    assertThat(dynamicEndpoint.getMethod()).isNotNull();
    assertThat(dynamicEndpoint.getMethod().getName()).isEqualTo("methodToRead");
    assertThat(dynamicEndpoint.getParameters().stream().map(DynamicParameter::getName).toList())
        .isEqualTo(Arrays.asList("string", "mapToBind", "aNewCat"));
  }

  @Test
  void should_not_read_method() {
    assertThatCode(
            () ->
                DynamicEndpointHelper.readDynamicEndpoint(
                    new TesterClassWithMultipleMethods(), Action.class))
        .hasMessage(
            "Only one method must be annotated with interface com.accor.kengine.seamless.Action");
  }

  @Test
  void should_read_method_with_previously_methodsignature_read() {
    MultipleActionMethodsHelper.$(TesterClassWithMultipleMethods.class)
        .methodToRead(null, null, null);
    Method methodSignature = MultipleActionMethodReferenceRecorderInterceptor.lastExecutedMethod;
    DynamicEndpoint<Action> dynamicEndpoint =
        DynamicEndpointHelper.readDynamicEndpoint(
            new TesterClassWithMultipleMethods(), methodSignature, Action.class);

    assertThat(dynamicEndpoint).isNotNull();
    assertThat(dynamicEndpoint.getMethod()).isNotNull();
    assertThat(dynamicEndpoint.getMethod().getName()).isEqualTo("methodToRead");
    assertThat(dynamicEndpoint.getParameters().stream().map(DynamicParameter::getName).toList())
        .isEqualTo(Arrays.asList("string", "mapToBind", "aNewCat"));
  }

  @Test
  void resolveArgumentsFromPojoProperties_map() {
    // Given
    List<DynamicEndpointHelper.DynamicParameter> parameters = new ArrayList<>();
    parameters.add(
        DynamicEndpointHelper.DynamicParameter.builder().name("cat").dynamic(false).build());
    parameters.add(
        DynamicEndpointHelper.DynamicParameter.builder()
            .name("inner.myHouse.name")
            .dynamic(true)
            .build());
    Map<String, Object> mapContext = new HashMap<>();
    House myHouse = House.builder().name("MyHouse").build();
    mapContext.put("cat", "My cat");
    mapContext.put("inner", TestInnerContext.builder().myHouse(myHouse).build());

    // When
    Object[] objects =
        new DynamicEndpointHelper.DynamicEndpoint<Action>(null, null, parameters)
            .resolveArguments(mapContext);

    // Then
    assertThat(objects).isNotNull();
    assertThat(objects[0]).isEqualTo("My cat");
    assertThat(objects[1]).isEqualTo(myHouse.getName());
  }

  @Test
  void resolveArgumentsFromPojoProperties_pojo() {
    // Given
    List<DynamicEndpointHelper.DynamicParameter> parameters = new ArrayList<>();
    parameters.add(
        DynamicEndpointHelper.DynamicParameter.builder().name("cat").dynamic(false).build());
    parameters.add(
        DynamicEndpointHelper.DynamicParameter.builder()
            .name("innerContext.dogs[1].name")
            .dynamic(true)
            .build());
    Cat catty = Cat.builder().name("Catty").build();
    Dog dog2 = Dog.builder().name("Dog2").build();
    TestContext testContext =
        TestContext.builder()
            .cat(catty)
            .innerContext(
                TestInnerContext.builder().dogs(Arrays.asList(Dog.builder().build(), dog2)).build())
            .build();

    // When
    Object[] objects =
        new DynamicEndpointHelper.DynamicEndpoint<Action>(null, null, parameters)
            .resolveArguments(testContext);

    // Then
    assertThat(objects).isNotNull();
    assertThat(objects[0]).isEqualTo(catty);
    assertThat(objects[1]).isEqualTo(dog2.getName());
  }

  @Test
  void should_mismatch_parameter() {
    TesterClass toHandle = new TesterClass();
    DynamicEndpoint<Action> dynamicEndpoint =
        DynamicEndpointHelper.readDynamicEndpoint(toHandle, Action.class);

    Map<String, Object> context = new HashMap<>();
    context.put("aNewCat", "String instead of cat!");
    assertThatCode(() -> dynamicEndpoint.getInvokeResult(toHandle, context))
        .hasMessage("argument type mismatch");
  }

  @Test
  void should_put_context_as_parameter() throws IllegalAccessException, InvocationTargetException {
    ContextAsParameterMethod toHandle = new ContextAsParameterMethod();
    DynamicEndpoint<Action> dynamicEndpoint =
        DynamicEndpointHelper.readDynamicEndpoint(toHandle, Action.class);

    TestContext context = TestContext.builder().build();
    dynamicEndpoint.getInvokeResult(toHandle, context);

    assertThat(context.getMyResult()).isEqualTo(VALUE_OK);
  }

  // FIXME - works on IDEA but not with mvn test!
  //  @Test
  void should_execute_alone_method_without_annotation()
      throws IllegalAccessException, InvocationTargetException {
    NoAnnotationMethod toHandle = new NoAnnotationMethod();
    DynamicEndpoint<Action> dynamicEndpoint =
        DynamicEndpointHelper.readDynamicEndpoint(toHandle, Action.class);

    TestContext context = TestContext.builder().build();
    Object invokeResult = dynamicEndpoint.getInvokeResult(toHandle, context);

    assertThat(invokeResult).isNull();
    assertThat(context.getMyResult()).isEqualTo(VALUE_OK);
  }

  @Test
  void should_execute_noparameter_method()
      throws IllegalAccessException, InvocationTargetException {
    NoParameterMethod toHandle = new NoParameterMethod();
    DynamicEndpoint<Action> dynamicEndpoint =
        DynamicEndpointHelper.readDynamicEndpoint(toHandle, Action.class);

    TestContext context = TestContext.builder().build();
    dynamicEndpoint.getInvokeResult(toHandle, context);

    assertThat(toHandle.isExecuted()).isTrue();
  }

  @Test
  void should_execute_with_contextparameter_annotations()
      throws IllegalAccessException, InvocationTargetException {
    ContextParameterMethod toHandle = new ContextParameterMethod();
    DynamicEndpoint<Action> dynamicEndpoint =
        DynamicEndpointHelper.readDynamicEndpoint(toHandle, Action.class);

    Cat cat1 = Cat.builder().name("Minou1").build();
    Cat cat2 = Cat.builder().name("Didou2").build();
    House tinyTiny = House.builder().name("TinyTiny").build();
    TestContext context =
        TestContext.builder()
            .cats(Arrays.asList(cat1, cat2))
            .innerContext(TestInnerContext.builder().myHouse(tinyTiny).build())
            .build();
    Object invokeResult = dynamicEndpoint.getInvokeResult(toHandle, context);

    assertThat(invokeResult).isEqualTo("Minou1 leaves TinyTiny");
  }

  @Test
  void should_execute_with_contextparameter_annotations_badcontext()
      throws IllegalAccessException, InvocationTargetException {
    ContextParameterMethod toHandle = new ContextParameterMethod();
    DynamicEndpoint<Action> dynamicEndpoint =
        DynamicEndpointHelper.readDynamicEndpoint(toHandle, Action.class);

    House tinyTiny = House.builder().name("TinyTiny").build();
    TestContext context =
        TestContext.builder()
            .innerContext(TestInnerContext.builder().myHouse(tinyTiny).build())
            .build();
    Object invokeResult = dynamicEndpoint.getInvokeResult(toHandle, context);

    assertThat(invokeResult).isEqualTo("null leaves TinyTiny");
  }

  @Test
  void should_execute_toomany_methods() {
    NoAnnotationMethods toHandle = new NoAnnotationMethods();

    assertThatCode(() -> DynamicEndpointHelper.readDynamicEndpoint(toHandle, Action.class))
        .hasMessage("No method annotated with interface com.accor.kengine.seamless.Action");
  }
}
