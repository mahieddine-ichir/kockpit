package com.accor.kengine.testers.json;

import com.accor.kengine.testers.model.Car;
import org.springframework.stereotype.Component;

@Component
public class CarAction {
  public void show(Car car) {
    System.out.println("Car: " + car);
  }

  public String update(Car car) {
    car.setBrand("Peugeot");
    car.setModel("5008");
    System.out.println("Updated car: " + car);
    return "ok";
  }
}
