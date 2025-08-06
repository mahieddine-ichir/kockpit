package com.accor.kengine.seamless;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.accor.kengine.WarningExecutionException;
import com.accor.kengine.seamless.context.Cat;
import com.accor.kengine.seamless.context.Dog;
import com.accor.kengine.seamless.context.House;
import com.accor.kengine.seamless.context.TestContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PredicateSeamLessWrapperTest {

  static class MySimplePredicate {

    @Predicate
    boolean readAndCompute(Cat cat, Dog oldDog, House houseToSell, House boughtHouse) {
      return nonNull(cat);
    }
  }

  static class ExceptionPredicate {

    @Predicate
    boolean readAndCompute() {
      throw new WarningExecutionException("Warning");
    }
  }

  @Test
  void should_execute_predicate_method_map_context() {
    // Given
    PredicateSeamLessWrapper myPredicateWrapper =
        new PredicateSeamLessWrapper(new MySimplePredicate(), null);

    List<Cat> cats = new ArrayList<>();
    cats.add(new Cat());

    Map<String, Object> context = new HashMap<>();
    context.put("cat", new Cat());
    context.put("doh", new Dog());
    context.put("house", new House());
    context.put("cats", cats);

    // When
    boolean test = myPredicateWrapper.test(context);

    // Then
    assertTrue(test);
  }

  @Test
  void should_throw_original_exception() {
    // Given
    PredicateSeamLessWrapper wrapper = new PredicateSeamLessWrapper(new ExceptionPredicate(), null);
    Map<Object, Object> context = new HashMap<>();

    // When
    WarningExecutionException ex = null;
    try {
      wrapper.test(context);
    } catch (WarningExecutionException e) {
      ex = e;
    }

    // Then
    assertThat(ex).isNotNull();
  }

  @Test
  void should_execute_predicate_method_pojo_context() {
    // Given
    PredicateSeamLessWrapper myPredicateWrapper =
        new PredicateSeamLessWrapper(new MySimplePredicate(), null);

    Cat cat1 = Cat.builder().name("cat1").build();
    TestContext context = TestContext.builder().cat(cat1).build();

    // When
    boolean test = myPredicateWrapper.test(context);

    // Then
    assertTrue(test);
  }
}
