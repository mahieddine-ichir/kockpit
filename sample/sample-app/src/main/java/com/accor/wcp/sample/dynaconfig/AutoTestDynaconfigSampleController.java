
package com.accor.wcp.sample.dynaconfig;

import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigAttribute;
import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigEnabler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@DynaConfigEnabler
@RestController
@RequiredArgsConstructor
@Slf4j
public class AutoTestDynaconfigSampleController {

  @DynaConfigAttribute(
          value = "sample.resttemplate.timeout")
  private long sampleRestTemplateTimeout;

  @GetMapping(value = "/autotest/dynaconfig/sample-testtemplate-timeout")
  public ResponseEntity<String> sampleRestTemplateTimeout() {
    return ResponseEntity.ok("sample.resttemplate.timeout=" + sampleRestTemplateTimeout);
  }
}
