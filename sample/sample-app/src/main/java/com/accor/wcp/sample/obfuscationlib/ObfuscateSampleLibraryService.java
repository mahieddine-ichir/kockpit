package com.accor.wcp.sample.obfuscationlib;

import com.accor.wcp.obfuscation.ObfuscateConfig;
import com.accor.wcp.obfuscation.ObfuscationService;
import com.accor.wcp.obfuscation.impl.ObjectObfuscateConfigImpl;
import com.accor.wcp.obfuscation.impl.obfuscators.json.JsonObfuscateConfig;
import com.accor.wcp.obfuscation.impl.obfuscators.value.ValueObfuscateConfig;
import com.accor.wcp.obfuscation.impl.obfuscators.xml.XmlObfuscateConfig;
import com.accor.wcp.obfuscation.masker.MaskerService;
import com.accor.wcp.sample.obfuscationlib.model.Address;
import com.accor.wcp.sample.obfuscationlib.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
class ObfuscateSampleLibraryService {

  private final MaskerService maskerService;

  private final ObfuscationService obfuscationService;

  User obfuscateUser() {
    Map<String, String> prefs = new HashMap<>();
    prefs.put("couleur", "jaune");
    prefs.put("fruit", "durian");
    prefs.put("loisir", "dormir");

    User user =
        User.builder()
            .id("8642535876542")
            .firstname("Olivier")
            .lastname("Cai")
            .age(21)
            .phone("0607080910")
            .email("olivier@gmail.com")
            .address(
                Address.builder()
                    .street1("35 rue de la vallée")
                    .zipCode("75015")
                    .city("Paris")
                    .build())
            .preferences(prefs)
            .build();

    Map<String, ObfuscateConfig> preferencesObfuscation =
        Map.of("fruit", new ValueObfuscateConfig(), "loisir", new ValueObfuscateConfig());

    ObfuscateConfig preferenceObfuscateConfig =
        ObjectObfuscateConfigImpl.builder()
            .obfuscateConfigByProperty(preferencesObfuscation)
            .build();

    Map<String, ObfuscateConfig> configs =
        Map.of(
                "email",
                new ValueObfuscateConfig("email"),
                "firstName",
                new ValueObfuscateConfig("keepLast2"),
                "address.street1",
                new ValueObfuscateConfig(),
                "phone",
                new ValueObfuscateConfig("keepLast4"),
                "id",
                new ValueObfuscateConfig("keepLast3"),
                "preferences",
                preferenceObfuscateConfig);

    ObjectObfuscateConfigImpl obfuscateConfig =
        ObjectObfuscateConfigImpl.builder().obfuscateConfigByProperty(configs).build();

    return obfuscationService.obfuscateObject(user, obfuscateConfig);
  }

  String obfuscateValue(String body, String masker) {

    if (isNull(masker)) {
      masker = "DEFAULT";
    }

    return maskerService.mask(body, masker);
  }

  String obfuscateJson(List<String> path, List<String> masker, String body) {

    Map<String, String> obfuscationConfig =
        IntStream.range(0, path.size()).boxed().collect(Collectors.toMap(path::get, masker::get));

    List<JsonObfuscateConfig.PathConfig> configList =
        obfuscationConfig.entrySet().stream()
            .map(
                config ->
                    JsonObfuscateConfig.PathConfig.builder()
                        .path(config.getKey())
                        .maskerId(config.getValue())
                        .build())
            .collect(Collectors.toList());

    JsonObfuscateConfig config = JsonObfuscateConfig.builder().pathConfigs(configList).build();

    return obfuscationService.obfuscate(body, config);
  }

  String obfuscateXml(List<String> path, List<String> masker, String body) {

    Map<String, String> obfuscationConfig =
        IntStream.range(0, path.size()).boxed().collect(Collectors.toMap(path::get, masker::get));

    List<XmlObfuscateConfig.PathConfig> configList =
        obfuscationConfig.entrySet().stream()
            .map(
                config ->
                    XmlObfuscateConfig.PathConfig.builder()
                        .path(config.getKey())
                        .maskerId(config.getValue())
                        .build())
            .collect(Collectors.toList());

    XmlObfuscateConfig xmlConfig = XmlObfuscateConfig.builder().pathConfigs(configList).build();

    return obfuscationService.obfuscate(body, xmlConfig);
  }
}
