package com.accor.wcp.console.services.featureflipping;

import com.accor.wcp.console.services.featureflipping.dto.FeatureFlippingKeyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${console.services.public-prefix}/featureflipping")
@RequiredArgsConstructor
@Slf4j
public class FeatureFlippingPublicApi {

  private final FeatureFlippingService featureFlippingService;

  @GetMapping(value = "/{applicationId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FeatureFlippingKeyDto> getApplicationProperties(@PathVariable String domain, @PathVariable String env, @PathVariable String applicationId) {
    return featureFlippingService.list(domain, env, applicationId);
  }

}
