package com.accor.wcp.audit.it;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserData {
  private String name;
  private int age;
}
