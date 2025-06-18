package org.kockpit.audit.obfuscation.impl;

import org.kockpit.audit.obfuscation.Obfuscate;
import org.kockpit.audit.obfuscation.impl.obfuscators.json.JsonObfuscateConfig;
import org.kockpit.audit.obfuscation.impl.obfuscators.json.JsonObfuscateConfig.PathConfig;
import org.kockpit.audit.obfuscation.masker.MaskerService;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.MapFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class JsonObfuscate implements Obfuscate<JsonObfuscateConfig> {

  private final MaskerService maskerService;

  @Override
  public String doObfuscate(String data, JsonObfuscateConfig config) {
    try {
      DocumentContext jsonContext = JsonPath.parse(data);

      config.getPathConfigs().forEach(pathConfig -> {
        MapFunction mapFunction = (currentValue, configuration) -> mask(currentValue, pathConfig);
        String path = pathConfig.getPath();
        try {
          jsonContext.map(path, mapFunction);
        } catch (Exception e) {
          log.debug("Ignore json path: {}", path);
        }
      });

      return jsonContext.jsonString();
    } catch (Exception e) {
      log.debug("An exception occurred during process", e);
      return data;
    }
  }

  private String mask(Object currentValue, PathConfig jsonPathConfig) {
    return maskerService.mask(currentValue.toString(), jsonPathConfig.getMaskerId());
  }
}
