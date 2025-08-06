package com.accor.wcp.sample.kengine.advanced;

import com.accor.kengine.seamless.Flow;
import com.accor.kengine.seamless.NamedFlowExecution;
import com.accor.wcp.sample.kengine.advanced.rules.UserNotificationRule;

@Flow(documentation = "Simple Fake user notification Flow definition", ruleClasses = UserNotificationRule.class)
public interface UserNotificationFlow {

  @NamedFlowExecution(parameterName = "executionName")
  void notify(String executionName, EnrichedUser user);

}
