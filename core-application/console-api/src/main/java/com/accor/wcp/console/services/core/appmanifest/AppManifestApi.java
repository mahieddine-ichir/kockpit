package com.accor.wcp.console.services.core.appmanifest;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/console/manifests")
@RequiredArgsConstructor
@Slf4j
public class AppManifestApi {
  private final AppManifestServiceImpl appManifestService;

  @GetMapping()
  public Collection<AppManifest> getAllManifest() {
    return appManifestService.getAppManifests();
  }
}
