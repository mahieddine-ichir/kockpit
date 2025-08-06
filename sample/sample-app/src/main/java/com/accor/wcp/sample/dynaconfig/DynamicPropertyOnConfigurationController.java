
package com.accor.wcp.sample.dynaconfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DynamicPropertyOnConfigurationController {

  private final RestTemplate restTemplateWcXssInsuranceQuotation;

  private final ApplicationProperties properties;

  @GetMapping(value = "/dynaconfig2/property-on-configuration")
  public ResponseEntity<String> simple() throws IllegalAccessException {
    ClientHttpRequestFactory requestFactory1 = restTemplateWcXssInsuranceQuotation.getRequestFactory();
    Object requestFactory2 = FieldUtils.readField(requestFactory1, "requestFactory", true);
    Object requestFactory3 = FieldUtils.readField(requestFactory2, "requestFactory", true);
    Object requestConfig = FieldUtils.readField(requestFactory3, "requestConfig", true);
    return ResponseEntity.ok("restTemplateWcXssInsuranceQuotation=" + ReflectionToStringBuilder.reflectionToString(requestConfig));
  }

  @GetMapping(value = "/dynaconfig2/configurationproperties/application", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApplicationProperties applicationProperties() {
    return properties;
  }
}
