package com.accor.kengine.testers.json;

import com.accor.kengine.testers.TesterContext;
import com.accor.kengine.testers.model.Car;
import org.springframework.stereotype.Component;

@Component
public class LoadAction {
  public void loadCar(TesterContext root) {
    System.out.println("Root: " + root);
    root.setCar(Car.builder().brand("Fiat").model("500").build());
  }
}
