package com.accor.wcp.obfuscation.impl.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
  private String id;
  private String firstname;
  private String lastname;
  private Address address;
  private Map<String, String> preferences;
  private Map<String, Address> otherAddress;
  private String financialDataJson;
}
