package com.accor.wcp.sample.kengine.warning;

import com.accor.kengine.seamless.Flow;

@Flow(ruleClasses = SingleWarningRule.class)
public interface WarningFlow {
  FlowResult flowWithWarning();
}
