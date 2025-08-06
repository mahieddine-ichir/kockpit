package com.accor.wcp.console.services.darkcanary;

import com.accor.wcp.console.services.darkcanary.model.DarkCanaryConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("${console.services.internal-public-prefix}/darkcanary")
@CrossOrigin(origins = "*", originPatterns = "*")
@RequiredArgsConstructor
@Slf4j
public class DarkCanaryManifestApi {

  private final DarkCanaryService darkCanaryService;

  @SneakyThrows
  @GetMapping(value = "/config", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DarkCanaryConfiguration> manifests() {
    return darkCanaryService.getConfigurations();
  }
}
