package com.accor.wcp.sample.dynaconfig;

import com.accor.wcp.sdk.application.lifecycle.SdkBeforeInitializationLifeCycleMarker;
import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigAttribute;
import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigEnabler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@DynaConfigEnabler
@RestController
@Slf4j
public class DynaConfigTestController implements SdkBeforeInitializationLifeCycleMarker {

  @DynaConfigAttribute(beanPropertyAccess = true, value = "ff.backend.test.nullvalue")
  private String defaultNullStringValue;

  @GetMapping(value = "/dynaconfig/test/nullvalue")
  public ResponseEntity<String> backend2022() {
    return ResponseEntity.ok("defaultNullStringValue=" + defaultNullStringValue);
  }

  public void setDefaultNullStringValue(String defaultNullStringValue) {
    this.defaultNullStringValue = defaultNullStringValue;
    log.info("setting defaultNullStringValue= " + defaultNullStringValue);
  }
}
