package com.accor.wcp.sample.dynaconfig;

import com.accor.wcp.sdk.application.lifecycle.SdkBeforeInitializationLifeCycleMarker;
import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigAttribute;
import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigEnabler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@DynaConfigEnabler
@RestController
public class DynaConfigSampleController implements SdkBeforeInitializationLifeCycleMarker {

  @DynaConfigAttribute("ff.backend.new2022")
  private String backend2022FeatureFlipping;

  @Value("${dyna.property.sample.setter:defaultValue}")
  @DynaConfigAttribute(beanPropertyAccess = true)
  private String dynaPropertyThroughSetter;

  @Value("${dyna.property.backend.call.timeout:1000}")
  @DynaConfigAttribute(beanPropertyAccess = true)
  private Integer backendCallTimeout;

  @GetMapping(value = "/dynaconfig/featureflipping/backend2022")
  public ResponseEntity<String> backend2022() {
    return ResponseEntity.ok("ff.backend.new2022=" + backend2022FeatureFlipping);
  }

  @GetMapping(value = "/dynaconfig/dynaPropertyThroughSetter")
  public ResponseEntity<String> dynaPropertyThroughSetter() {
    return ResponseEntity.ok("dynaPropertyThroughSetter=" + dynaPropertyThroughSetter);
  }

  @GetMapping(value = "/dynaconfig/backendCallTimeout")
  public ResponseEntity<String> backendCallTimeout() {
    return ResponseEntity.ok("backendCallTimeout=" + backendCallTimeout);
  }

  public void setDynaPropertyThroughSetter(String dynaPropertyThroughSetter) {
    this.dynaPropertyThroughSetter = dynaPropertyThroughSetter;
  }

  public void setBackendCallTimeout(Integer backendCallTimeout) {
    this.backendCallTimeout = backendCallTimeout;
  }
}
