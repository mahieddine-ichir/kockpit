package com.accor.wcp.sample.kengine.advanced.rules;

import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.perform;

import com.accor.kengine.RuleNode;
import com.accor.kengine.registry.seamless.RuleNodesBuilderSeamLessSupport;
import com.accor.kengine.seamless.Action;
import com.accor.kengine.seamless.ContextResult;
import com.accor.kengine.seamless.Rule;
import com.accor.wcp.sample.kengine.advanced.UserContext;
import com.accor.wcp.sample.kengine.advanced.model.AdvancedUser;
import java.util.UUID;
import lombok.AllArgsConstructor;

@Rule("LoadUser")
@AllArgsConstructor
class LoadUserRule extends RuleNodesBuilderSeamLessSupport {

  static class LoadUserAction {
    @Action(value = "ACT_ADVANCED_LOAD_USER", documentation = "Load given user")
    @ContextResult
    UserContext userContext(UUID userId) {
      return UserContext.builder()
          .user(AdvancedUser.builder()
              .id(userId)
              .name("Cyril")
              .build())
          .build();
    }
  }

  @Override
  public RuleNode configure() {
    LoadUserAction load = new LoadUserAction();
    return perform(load)
        .end();
  }
}
