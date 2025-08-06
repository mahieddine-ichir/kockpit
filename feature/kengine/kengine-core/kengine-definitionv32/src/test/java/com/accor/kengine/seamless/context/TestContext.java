package com.accor.kengine.seamless.context;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestContext {
  private Cat cat;
  private Dog dog;
  private List<Cat> cats;
  private TestInnerContext innerContext;

  private String myResult;
}
