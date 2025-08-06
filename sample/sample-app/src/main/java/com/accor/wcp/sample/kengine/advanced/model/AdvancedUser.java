package com.accor.wcp.sample.kengine.advanced.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdvancedUser {

  private UUID id;

  private String name;

}
