package com.accor.wcp.sample.kengine.advanced.rules;

import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.perform;

import com.accor.kengine.RuleNode;
import com.accor.kengine.registry.seamless.RuleNodesBuilderSeamLessSupport;
import com.accor.kengine.seamless.Action;
import com.accor.kengine.seamless.ContextParameter;
import com.accor.kengine.seamless.ContextResult;
import com.accor.kengine.seamless.Rule;
import com.accor.wcp.sample.kengine.advanced.CompanyContext;
import com.accor.wcp.sample.kengine.advanced.EnrichedUser;
import com.accor.wcp.sample.kengine.advanced.UserContext;
import com.accor.wcp.sample.kengine.advanced.UserNotificationFlow;
import com.accor.wcp.sample.kengine.advanced.model.AdvancedCompany;
import com.accor.wcp.sample.kengine.advanced.model.AdvancedUser;
import lombok.AllArgsConstructor;

@Rule("EnrichUser")
@AllArgsConstructor
class EnrichUserRule extends RuleNodesBuilderSeamLessSupport {

  private final UserNotificationFlow notificationUserFlow;

  static class EnrichedUserAction {
    @Action(value = "ACT_ADVANCED_ENRICH_USER", documentation = "Create an enriched user")
    @ContextResult("enrichedUser")
    EnrichedUser enrich(@ContextParameter("userContext.user") AdvancedUser user,
        @ContextParameter("companyContext.company") AdvancedCompany company,
        CompanyContext companyContext,
        UserContext userContext) {
      return EnrichedUser.builder()
          .user(user)
          .company(company)
          .build();
    }
  }

  class LaunchNotifyAction {
    @Action(value = "ACT_ADVANCED_NOTIFY", documentation = "Launch subprocess of notification")
    void launchNotification(EnrichedUser enrichedUser) {
      notificationUserFlow.notify("Notify " + enrichedUser.getUser().getName(), enrichedUser);
    }
  }

  @Override
  public RuleNode configure() {
    EnrichedUserAction enrich = new EnrichedUserAction();
    LaunchNotifyAction launchNotify = new LaunchNotifyAction();
    return perform(enrich)
        .lastly(perform(launchNotify))
        .end();
  }
}
