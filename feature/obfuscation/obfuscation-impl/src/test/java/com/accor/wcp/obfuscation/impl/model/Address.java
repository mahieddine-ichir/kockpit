package com.accor.wcp.obfuscation.impl.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Address {
  private String street1;
  private String street2;
  private String zipCode;
  private String city;
  private String country;

}
