package com.accor.kengine.testers.v32;

import com.accor.kengine.seamless.Predicate;
import com.accor.kengine.testers.model.Car;
import org.springframework.stereotype.Component;

@Component
class MySimplePredicate {
  @Predicate(documentation = "ffdfdfd", value = "PRE_???")
  boolean validate(Car car, Car car2) {
    System.out.println("Car in predicate: " + car);
    return true;
  }
}
