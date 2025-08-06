package com.accor.wcp.sample.kengine.advanced;

import com.accor.wcp.sample.kengine.advanced.model.AdvancedCompany;
import com.accor.wcp.sample.kengine.advanced.model.AdvancedUser;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnrichedUser {

  private AdvancedUser user;
  private AdvancedCompany company;

}
