package com.accor.wcp.console.services.core.application;

import com.accor.wcp.console.services.core.application.model.ApplicationDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApplicationApi {

  private final AppInstanceManager appInstanceManager;

  @GetMapping("/api/console/applications/{domain}/{env}")
  public List<ApplicationDto> list(@PathVariable String domain, @PathVariable String env) {
    return appInstanceManager.list(domain, env);
  }
}
