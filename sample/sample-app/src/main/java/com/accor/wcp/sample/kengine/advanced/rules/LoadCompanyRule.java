package com.accor.wcp.sample.kengine.advanced.rules;

import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.perform;

import com.accor.kengine.RuleNode;
import com.accor.kengine.registry.seamless.RuleNodesBuilderSeamLessSupport;
import com.accor.kengine.seamless.Action;
import com.accor.kengine.seamless.ContextResult;
import com.accor.kengine.seamless.Rule;
import com.accor.wcp.sample.kengine.advanced.CompanyContext;
import com.accor.wcp.sample.kengine.advanced.model.AdvancedCompany;
import java.util.UUID;
import lombok.AllArgsConstructor;

@Rule("LoadCompany")
@AllArgsConstructor
class LoadCompanyRule extends RuleNodesBuilderSeamLessSupport {

  static class LoadCompanyAction {
    @Action(value = "ACT_ADVANCED_LOAD_COMPANY", documentation = "Load a company")
    @ContextResult
    CompanyContext companyContext(UUID companyId) {
      return CompanyContext.builder()
          .company(AdvancedCompany.builder()
              .companyId(companyId)
              .name("Kiss")
              .build())
          .build();
    }
  }

  @Override
  public RuleNode configure() {
    LoadCompanyAction load = new LoadCompanyAction();
    return perform(load)
        .end();
  }
}
