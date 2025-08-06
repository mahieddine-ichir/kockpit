package com.accor.wcp.console.services.featureflipping;

import com.accor.wcp.console.services.featureflipping.dto.FeatureFlippingDto;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Collections.emptyList;

@RestController
@RequestMapping("${console.services.path-prefix}/featureflipping")
@RequiredArgsConstructor
@Slf4j
public class FeatureFlippingApi {

  private final FeatureFlippingService featureFlippingService;

  @SneakyThrows
  @PutMapping("/{applicationId}/{propertyName}")
  public List<String> updateProperty(
      @PathVariable String domain,
      @PathVariable String env,
      @PathVariable String applicationId,
      @PathVariable String propertyName,
      @RequestBody String newValue) {
    return featureFlippingService.update(domain, env, applicationId, propertyName, newValue);
  }

  @SneakyThrows
  @PutMapping("/{applicationId}/update")
  public List<String> updateDynaConfig(
      @PathVariable String domain,
      @PathVariable String env,
      @PathVariable String applicationId,
      @RequestBody FeatureFlippingDto featureFlippingDto,
      Authentication authentication) {
    if (Objects.nonNull(featureFlippingDto) && !CollectionUtils.isEmpty(featureFlippingDto.getProperties()))
      return featureFlippingService.update(domain, env, applicationId, featureFlippingDto, authentication.getName());
    else
      return emptyList();
  }

  @GetMapping("/{applicationId}")
  public FeatureFlippingDto getApplicationProperties(
      @PathVariable String domain, @PathVariable String env, @PathVariable String applicationId) {
    return featureFlippingService.getByApplicationId(domain, env, applicationId);
  }

  @PostMapping("/{applicationId}/refresh")
  public List<String> refreshApplication(
      @PathVariable String domain,
      @PathVariable String env,
      @PathVariable String applicationId) {
    return featureFlippingService.refresh(domain, env, applicationId);
  }

  @PostMapping("/{applicationId}/flush")
  public void flushDynaConfig(
          @PathVariable String domain,
          @PathVariable String env,
          @PathVariable String applicationId) {
      featureFlippingService.flush(domain, env, applicationId);
  }

}
