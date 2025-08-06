package com.accor.wcp.console.services.dynaconfig;

import com.accor.wcp.console.services.dynaconfig.dto.DynaConfigDto;
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
@RequestMapping("${console.services.path-prefix}/dynaconfig")
@RequiredArgsConstructor
@Slf4j
public class DynaConfigApi {

  private final DynaConfigService dynaConfigService;

  @SneakyThrows
  @PutMapping("/{applicationId}/{propertyName}")
  public List<String> updateProperty(
      @PathVariable String domain,
      @PathVariable String env,
      @PathVariable String applicationId,
      @PathVariable String propertyName,
      @RequestBody String newValue) {
    return dynaConfigService.update(domain, env, applicationId, propertyName, newValue);
  }

  @SneakyThrows
  @PutMapping("/{applicationId}/update")
  public List<String> updateDynaConfig(
      @PathVariable String domain,
      @PathVariable String env,
      @PathVariable String applicationId,
      @RequestBody DynaConfigDto dynaConfigDto,
      Authentication authentication) {
    if (Objects.nonNull(dynaConfigDto) && !CollectionUtils.isEmpty(dynaConfigDto.getProperties()))
      return dynaConfigService.update(domain, env, applicationId, dynaConfigDto, authentication.getName());
    else
      return emptyList();
  }

  @GetMapping("/{applicationId}")
  public DynaConfigDto getApplicationProperties(
      @PathVariable String domain, @PathVariable String env, @PathVariable String applicationId) {
    return dynaConfigService.getByApplicationId(domain, env, applicationId);
  }

  @PostMapping("/{applicationId}/refresh")
  public List<String> refreshApplication(
      @PathVariable String domain,
      @PathVariable String env,
      @PathVariable String applicationId) {
    return dynaConfigService.refresh(domain, env, applicationId);
  }

  @PostMapping("/{applicationId}/flush")
  public void flushDynaConfig(
          @PathVariable String domain,
          @PathVariable String env,
          @PathVariable String applicationId) {
      dynaConfigService.flush(domain, env, applicationId);
  }

}
