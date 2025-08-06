package com.accor.wcp.sample.obfuscationlib.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
  private String id;
  private String firstname;
  private String lastname;
  private int age;
  private String phone;
  private String email;
  private Address address;
  private Map<String, String> preferences;
}
