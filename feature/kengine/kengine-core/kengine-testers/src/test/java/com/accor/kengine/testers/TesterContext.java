package com.accor.kengine.testers;

import com.accor.kengine.testers.model.Car;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TesterContext {
  private String name;
  private Car car;
  private Car car2;
  private String greetings;
}
