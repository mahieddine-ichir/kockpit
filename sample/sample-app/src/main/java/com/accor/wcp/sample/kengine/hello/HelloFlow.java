package com.accor.wcp.sample.kengine.hello;

import com.accor.kengine.seamless.AuditFlowExecution;
import com.accor.kengine.seamless.ContextResult;
import com.accor.kengine.seamless.Flow;
import com.accor.kengine.seamless.NamedFlowExecution;
import com.accor.wcp.audit.annotation.AuditAttribute;
import java.util.Map;

@Flow(id = "hello", documentation = "Simple HelloWorld Flow definition",
ruleIds = "hello1")
public interface HelloFlow {

  // By default, return the method name as property name in context
  String sayHelloToWorld(@AuditAttribute String name);

  @NamedFlowExecution(parameterName = "name")
  @ContextResult("randomGreetings") String sayHelloToWorldRandomly(String name);

  @NamedFlowExecution(name = "helloNoResult")
  void helloWithNoResult();

  @AuditFlowExecution(audit = false)
  @NamedFlowExecution(name = "helloNoResult")
  void helloWithNoResultAndNoAudit();

  @AuditFlowExecution(parameterName = "audit")
  @ContextResult Map<String, Object> helloWithFullContextResult(boolean audit);

  @ContextResult
  HelloResultComposite helloWithCompositeContextResult(@AuditAttribute String name);
}
