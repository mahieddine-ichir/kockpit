package com.accor.wcp.sample.kengine.advanced.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdvancedCompany {

  private UUID companyId;

  private String name;

  private Address address;
}
