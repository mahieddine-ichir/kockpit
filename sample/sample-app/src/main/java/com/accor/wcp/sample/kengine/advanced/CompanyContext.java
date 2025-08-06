package com.accor.wcp.sample.kengine.advanced;

import com.accor.wcp.sample.kengine.advanced.model.AdvancedCompany;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyContext {

  private AdvancedCompany company;
}
