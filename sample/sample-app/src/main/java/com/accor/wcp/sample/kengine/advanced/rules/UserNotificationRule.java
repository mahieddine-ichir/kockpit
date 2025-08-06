package com.accor.wcp.sample.kengine.advanced.rules;

import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.perform;

import com.accor.kengine.RuleNode;
import com.accor.kengine.registry.seamless.RuleNodesBuilderSeamLessSupport;
import com.accor.kengine.seamless.Action;
import com.accor.kengine.seamless.Rule;
import com.accor.wcp.sample.kengine.advanced.EnrichedUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Rule()
@AllArgsConstructor
@Slf4j
public class UserNotificationRule extends RuleNodesBuilderSeamLessSupport {

  static class Notify {
    @Action(value = "ACT_NOTIFY_USER", documentation = "Notify user")
    void notify(EnrichedUser user) {
      log.info("Notify user: {} of company: {}", user.getUser(), user.getCompany());
    }
  }

  @Override
  public RuleNode configure() {
    Notify notify = new Notify();
    return perform(notify).end();
  }
}
