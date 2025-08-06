package com.accor.kengine.seamless;

import static com.accor.kengine.seamless.MultipleActionMethodsHelper.$;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.accor.kengine.WarningExecutionException;
import com.accor.kengine.seamless.context.Cat;
import com.accor.kengine.seamless.context.Dog;
import com.accor.kengine.seamless.context.FlowContextContainer;
import com.accor.kengine.seamless.context.House;
import com.accor.kengine.seamless.context.TestContext;
import com.accor.kengine.seamless.context.TestInnerContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.junit.jupiter.api.Test;

class ActionSeamLessWithWrapperTest {

  static final String ID_ACTION_WITH_ACTIONRESULT_ANNOTATION =
      "ACTION_WITH_ACTIONRESULT_ANNOTATION";

  static class MySoSimpleAction {

    @Action
    String readAndCompute(List<Cat> cats, Cat cat, House boughtHouse) {
      // Result is not used at all!
      return "cat: " + cat.getName() + " in house: " + boughtHouse;
    }
  }

  static class MyActionWithActionResult {

    @Action(ID_ACTION_WITH_ACTIONRESULT_ANNOTATION)
    @ContextResult("myResult")
    String readAndCompute(
        Cat cat,
        Dog oldDog,
        House houseToSell,
        House boughtHouse,
        @ContextParameter("innerContext.myHouse") House innerMyHouse) {
      return "KEngine.yeah";
    }
  }

  static class MyActionWithBeanUtils {

    @Action
    @ContextResult("innerContext.catHouseGreetings")
    String readAndCompute(
        @ContextParameter("cats[0].name") String firstCatName,
        @ContextParameter("innerContext.myHouse.name") String innerMyHouseName) {
      return firstCatName + " in the house: " + innerMyHouseName;
    }
  }

  static class MyActionWithFlowContextContainer {

    @Action
    @ContextResult("TestInnerContext.catHouseGreetings")
    String readAndCompute(
        TestContext testContext,
        @ContextParameter("TestContext.cats[1].name") String firstCatName,
        @ContextParameter("TestInnerContext.myHouse.name") String innerMyHouseName) {
      return firstCatName + " in the house: " + innerMyHouseName;
    }
  }

  static class ExceptionAction {

    @Action
    String throwException() {
      throw new WarningExecutionException("Warning1");
    }
  }

  @Getter
  static class OldCompatibilityAction implements com.accor.kengine.Action<Object> {

    private boolean executed;

    @Override
    public void execute(Object context) throws Exception {
      this.executed = true;
    }
  }

  @Test
  void should_execute_action_retro_compatibility() throws Exception {
    // Given
    OldCompatibilityAction oldCompatibilityAction = new OldCompatibilityAction();
    ActionSeamLessWrapper wrapper = new ActionSeamLessWrapper(oldCompatibilityAction, null);
    Map<Object, Object> simpleContext = new HashMap<>();

    // When
    wrapper.execute(simpleContext);

    // Then
    assertThat(oldCompatibilityAction.isExecuted()).isTrue();
  }

  @Test
  void should_read_action_annotation() {
    // Given
    ActionSeamLessWrapper wrapper = new ActionSeamLessWrapper(new MySoSimpleAction(), null);

    // Then
    assertThat(wrapper.getDetails().getCode()).isEqualTo(MySoSimpleAction.class.getCanonicalName());
  }

  @Test
  void should_read_default_action_details() {
    // Given
    ActionSeamLessWrapper wrapper = new ActionSeamLessWrapper(new MyActionWithActionResult(), null);

    // Then
    assertThat(wrapper.getDetails().getCode()).isEqualTo(ID_ACTION_WITH_ACTIONRESULT_ANNOTATION);
  }

  @Test
  void should_execute_action_method_with_a_map_context() throws Exception {
    // Given
    ActionSeamLessWrapper wrapper = new ActionSeamLessWrapper(new MyActionWithActionResult(), null);
    ActionSeamLessWrapper simpleWrapper = new ActionSeamLessWrapper(new MySoSimpleAction(), null);

    List<Cat> cats = new ArrayList<>();
    cats.add(new Cat());
    cats.add(new Cat());

    Map<Object, Object> context = new HashMap<>();
    context.put("cat", Cat.builder().name("cat1").build());
    context.put("dog", new Dog());
    context.put("cats", cats);

    // When
    wrapper.execute(context);

    // Then
    assertThat(context.get("myResult")).isNotNull().isEqualTo("KEngine.yeah");

    // When
    context.put("myResult", "");
    simpleWrapper.execute(context);

    // Then
    assertThat(context.get("myResult")).isEqualTo("");
  }

  @Test
  void should_execute_action_method_with_a_map_empty_context() throws Exception {
    // Given
    ActionSeamLessWrapper wrapper = new ActionSeamLessWrapper(new MyActionWithActionResult(), null);
    Map<Object, Object> contextWithNulls = new HashMap<>();

    // When
    wrapper.execute(contextWithNulls);

    // Then
    assertThat(contextWithNulls.get("myResult")).isNotNull().isEqualTo("KEngine.yeah");
  }

  @Test
  void should_throw_original_exception() throws Exception {
    // Given
    ActionSeamLessWrapper wrapper = new ActionSeamLessWrapper(new ExceptionAction(), null);
    Map<Object, Object> context = new HashMap<>();

    // When
    WarningExecutionException ex = null;
    try {
      wrapper.execute(context);
    } catch (WarningExecutionException e) {
      ex = e;
    }

    // Then
    assertThat(ex).isNotNull();
  }

  @Test
  void should_execute_action_method_with_pojo_context() throws Exception {
    // Given
    ActionSeamLessWrapper wrapper = new ActionSeamLessWrapper(new MyActionWithActionResult(), null);

    List<Cat> cats = new ArrayList<>();
    cats.add(new Cat());
    cats.add(new Cat());

    // POJO context
    TestContext testContext =
        TestContext.builder()
            .dog(new Dog())
            .cat(new Cat())
            .cats(cats)
            .innerContext(
                TestInnerContext.builder()
                    .dogs(new ArrayList<>())
                    .myHouse(House.builder().name("Inner House!").build())
                    .build())
            .build();

    // When
    wrapper.execute(testContext);

    // Then
    assertThat(testContext.getMyResult()).isNotNull().isEqualTo("KEngine.yeah");
  }

  @Test
  void should_execute_action_method_with_pojo_context_beanutils() throws Exception {
    // Given
    ActionSeamLessWrapper wrapper = new ActionSeamLessWrapper(new MyActionWithBeanUtils(), null);

    List<Cat> cats = new ArrayList<>();
    cats.add(Cat.builder().name("cat1").build());
    cats.add(Cat.builder().name("cat2").build());

    // POJO context
    TestContext testContext =
        TestContext.builder()
            .cats(cats)
            .innerContext(
                TestInnerContext.builder()
                    .myHouse(House.builder().name("InnHouse!").build())
                    .build())
            .build();

    // When
    wrapper.execute(testContext);

    // Then
    assertThat(testContext.getInnerContext().getCatHouseGreetings())
        .isNotNull()
        .isEqualTo("cat1 in the house: InnHouse!");
  }

  static class MultipleActionMethods {

    @Action
    @ContextResult("innerContext.catHouseGreetings")
    String readAndCompute(
        @ContextParameter("cats[0].name") String firstCatName,
        @ContextParameter("innerContext.myHouse.name") String innerMyHouseName) {
      return firstCatName + " in the house (#readAndCompute) : " + innerMyHouseName;
    }

    @Action
    @ContextResult("innerContext.catHouseGreetings")
    String anotherActionMethod(
        @ContextParameter("cats[0].name") String firstCatName,
        @ContextParameter("innerContext.myHouse.name") String innerMyHouseName,
        @ContextParameter("cats[0].name") String anotherParam) {
      return firstCatName + " in the house (#anotherActionMethod) : " + innerMyHouseName;
    }
  }

  @Test
  void should_execute_action_method_with_functionpointer() throws Exception {
    // Given
    MultipleActionMethods actionObj = new MultipleActionMethods();

    MultipleActionMethods pointer = $(MultipleActionMethods.class);

    ActionSeamLessWrapper wrapperAction1 =
        new ActionSeamLessWrapper(actionObj, pointer.readAndCompute(null, null), null);
    ActionSeamLessWrapper wrapperAction2 =
        new ActionSeamLessWrapper(actionObj, pointer.anotherActionMethod(null, null, null), null);

    List<Cat> cats = new ArrayList<>();
    cats.add(Cat.builder().name("cat1").build());
    cats.add(Cat.builder().name("cat2").build());

    // POJO context
    TestContext testContext =
        TestContext.builder()
            .cats(cats)
            .innerContext(
                TestInnerContext.builder()
                    .myHouse(House.builder().name("InnHouse!").build())
                    .build())
            .build();

    // When
    wrapperAction1.execute(testContext);

    // Then
    assertThat(testContext.getInnerContext().getCatHouseGreetings())
        .isNotNull()
        .isEqualTo("cat1 in the house (#readAndCompute) : InnHouse!");

    // When
    wrapperAction2.execute(testContext);

    // Then
    assertThat(testContext.getInnerContext().getCatHouseGreetings())
        .isNotNull()
        .isEqualTo("cat1 in the house (#anotherActionMethod) : InnHouse!");
  }

  @Test
  void should_execute_action_method_with_flowcontextcontainer() throws Exception {
    // Given
    ActionSeamLessWrapper wrapper =
        new ActionSeamLessWrapper(new MyActionWithFlowContextContainer(), null);

    List<Cat> cats = new ArrayList<>();
    cats.add(Cat.builder().name("cat1").build());
    cats.add(Cat.builder().name("cat2").build());

    // Flow context container for demo / backward compatibility
    FlowContextContainer flowContextContainer = new FlowContextContainer();

    TestInnerContext testInnerContext =
        TestInnerContext.builder().myHouse(House.builder().name("AnotherHouse!").build()).build();
    TestContext testContext =
        TestContext.builder().cats(cats).innerContext(testInnerContext).build();

    flowContextContainer.setContext(TestContext.class, testContext);
    flowContextContainer.setContext(TestInnerContext.class, testInnerContext);

    // When
    wrapper.execute(flowContextContainer);

    // Then
    assertThat(flowContextContainer.getContext(TestInnerContext.class).getCatHouseGreetings())
        .isNotNull()
        .isEqualTo("cat2 in the house: AnotherHouse!");
  }
}
