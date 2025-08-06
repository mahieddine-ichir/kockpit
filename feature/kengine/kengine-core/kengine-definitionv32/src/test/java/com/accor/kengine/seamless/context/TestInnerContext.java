package com.accor.kengine.seamless.context;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestInnerContext {
  private List<Dog> dogs;
  private House myHouse;
  private String catHouseGreetings;
}
