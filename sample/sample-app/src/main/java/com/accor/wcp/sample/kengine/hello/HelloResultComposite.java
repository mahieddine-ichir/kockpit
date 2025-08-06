package com.accor.wcp.sample.kengine.hello;

import com.accor.kengine.seamless.ContextResult;
import com.accor.wcp.audit.annotation.AuditAttribute;
import com.accor.wcp.audit.annotation.Audited;
import lombok.Data;

@Data
@Audited
@ContextResult
public class HelloResultComposite {

  @AuditAttribute
  private String name;

  @AuditAttribute
  @ContextResult("sayHelloToWorld")
  private String greetings;

  private String randomGreetings;
}
