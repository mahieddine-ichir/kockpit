
package com.accor.wcp.sample.dynaconfig;

import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigAttribute;
import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigEnabler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@DynaConfigEnabler
@RestController
@RequiredArgsConstructor
@Slf4j
public class AnotherDynaConfigSampleController {

  private final RestTemplate restTemplate;

  @DynaConfigAttribute("ff.backend.new2022")
  private Boolean backend2022FeatureFlipping;

  @DynaConfigAttribute(
          value = "sample.resttemplate.timeout")
  private long sampleRestTemplateTimeout;

  @Value("${dyna.property.sample.setter:defaultValue2}")
  @DynaConfigAttribute
  private String dynaPropertyDirect;

  @GetMapping(value = "/dynaconfig2/featureflipping/backend2022")
  public ResponseEntity<String> backend2022() {
    return ResponseEntity.ok("ff.backend.new2022=" + backend2022FeatureFlipping);
  }

  @GetMapping(value = "/dynaconfig2/dynaPropertyDirect")
  public ResponseEntity<String> dynaPropertyDirect() {
    return ResponseEntity.ok("dynaPropertyDirect=" + dynaPropertyDirect);
  }

  @GetMapping(value = "/dynaconfig2/backend")
  public ResponseEntity<String> home() {
    log.info("requestFactory: {}", restTemplate.getRequestFactory());
    return restTemplate.getForEntity("https://httpbin.org/headers", String.class);
  }
}
