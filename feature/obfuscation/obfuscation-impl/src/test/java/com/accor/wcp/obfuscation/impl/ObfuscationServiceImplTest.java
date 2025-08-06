package com.accor.wcp.obfuscation.impl;

import com.accor.wcp.obfuscation.ObfuscateConfig;
import com.accor.wcp.obfuscation.impl.masker.MaskerServiceImpl;
import com.accor.wcp.obfuscation.impl.masker.maskers.EmailMasker;
import com.accor.wcp.obfuscation.impl.masker.maskers.KeepFirstNbCharsMasker;
import com.accor.wcp.obfuscation.impl.masker.maskers.KeepLastNbCharsMasker;
import com.accor.wcp.obfuscation.impl.model.Address;
import com.accor.wcp.obfuscation.impl.model.User;
import com.accor.wcp.obfuscation.impl.obfuscators.json.JsonObfuscateConfig;
import com.accor.wcp.obfuscation.impl.obfuscators.json.JsonObfuscateConfig.PathConfig;
import com.accor.wcp.obfuscation.impl.obfuscators.value.ValueObfuscateConfig;
import com.accor.wcp.obfuscation.masker.MaskerService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.Files.readString;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ObfuscationServiceImplTest {

  @Test
  void should_not_create_ObfuscationServiceImpl() {
    // When
    ObfuscationServiceImpl underTest = new ObfuscationServiceImpl(List.of((data, config) -> null));
    // Then
    assertThat(underTest).isNotNull();
  }

  @Test
  void should_obfuscate_with_simple_value() {
    // Given
    ObfuscationServiceImpl underTest =
        new ObfuscationServiceImpl(List.of(new FakeSimpleValueObfuscate()));

    // When
    String obfuscated = underTest.obfuscate("hello world", new FakeValueObfuscateConfig());

    // Then
    assertThat(obfuscated).isEqualTo("***");
  }

  @Test
  void should_not_obfuscate_with_a_warn_log() {
    // Given
    ObfuscationServiceImpl underTest =
        new ObfuscationServiceImpl(List.of(new FakeSimpleValueObfuscate(), new BadObfuscate()));

    // When
    String data = "hello world";
    String obfuscated = underTest.obfuscate(data, new UnknownObfuscateConfig());

    // Then
    assertThat(obfuscated).isEqualTo(data);
  }

  private static MaskerService createMaskerService() {
    return new MaskerServiceImpl(
            List.of(new EmailMasker(), new KeepFirstNbCharsMasker(1), new KeepFirstNbCharsMasker(2), new KeepFirstNbCharsMasker(3), new KeepFirstNbCharsMasker(4),
                    new KeepLastNbCharsMasker(1), new KeepLastNbCharsMasker(2), new KeepLastNbCharsMasker(3), new KeepLastNbCharsMasker(4)));
  }

  private static User getUser(String json) {
    Map<String, String> preferences = new HashMap<>();
    preferences.put("api-key", "HHpbgQh2LYU78xaX9amy0mQhenGMpmVA");
    preferences.put("cardNumber", "4111 1111 1111 1111");
    preferences.put("loyaltyCard", "100001065441011");

    return User.builder()
        .id("userFunctionalId")
        .firstname("Cyril")
        .lastname("JOUI")
        .preferences(preferences)
        .address(
            Address.builder()
                .street1("Rue des Roses")
                .street2("KISS SAS")
                .zipCode("91390")
                .city("Morsang")
                .build())
        .otherAddress(
            Map.of("OFFICE", Address.builder().street1("Rue des Roses").city("Morsang").build()))
        .financialDataJson(json)
        .build();
  }

  @Test
  void should_obfuscate_with_object() throws URISyntaxException, IOException, JSONException {
    // Given
    MaskerService maskerService = createMaskerService();
    ObfuscationServiceImpl underTest =
        new ObfuscationServiceImpl(
            List.of(new ValueObfuscate(maskerService), new JsonObfuscate(maskerService)));
    String json =
        readString(
            Path.of(
                requireNonNull(
                        ObfuscationServiceImplTest.class.getResource(
                            "/json/user-financialdata1.json"))
                    .toURI()));
    User userData = getUser(json);
    Map<String, ObfuscateConfig> mapConfig = new HashMap<>();
    ValueObfuscateConfig valueObfuscateConfig = new ValueObfuscateConfig();
    mapConfig.put("address.city", valueObfuscateConfig);
    mapConfig.put("address.country", valueObfuscateConfig);
    mapConfig.put("lastname", valueObfuscateConfig);
    mapConfig.put(
        "preferences",
        ObjectObfuscateConfigImpl.builder()
            .obfuscateConfigByProperty(
                    Map.of(
                            "api-key",
                            new ValueObfuscateConfig("keepLast4"),
                            "cardNumber",
                            new ValueObfuscateConfig(),
                            "other",
                            new ValueObfuscateConfig()))
            .build());
    JsonObfuscateConfig financialJsonObfuscateConfig =
        JsonObfuscateConfig.builder()
            .pathConfigs(
                List.of(
                    PathConfig.builder().path("properties.others.criticalData").build(),
                    PathConfig.builder().path("iban").build(),
                    PathConfig.builder().path("bank").build(),
                    PathConfig.builder().path("notfound").build()))
            .build();
    mapConfig.put("financialDataJson", financialJsonObfuscateConfig);
    // do not obfuscate, address is not a string
    mapConfig.put("address", valueObfuscateConfig);
    // do not obfuscate because otherAddress is not a map of string
    mapConfig.put("otherAddress", valueObfuscateConfig);
    // do not obfuscate because gender does not exist
    mapConfig.put("gender", valueObfuscateConfig);
    ObjectObfuscateConfigImpl objectObfuscateConfig =
        ObjectObfuscateConfigImpl.builder().obfuscateConfigByProperty(mapConfig).build();

    // When
    User obfuscated = underTest.obfuscateObject(userData, objectObfuscateConfig);

    // Then
    String jsonObfuscated =
            readString(
                    Path.of(
                            requireNonNull(
                                    ObfuscationServiceImplTest.class.getResource(
                                            "/json/user-financialdata1-obfuscated.json"))
                                    .toURI()))
                    .trim();
    assertThat(obfuscated)
            .hasFieldOrPropertyWithValue("lastname", "*")
            .hasFieldOrPropertyWithValue("address.city", "*")
            .extracting("preferences")
            .extracting("api-key", "cardNumber", "loyaltyCard")
            .doesNotContainNull()
            .containsExactly("****************************pmVA", "*", "100001065441011");

    JSONAssert.assertEquals(obfuscated.getFinancialDataJson(),
            jsonObfuscated,
            new DefaultComparator(JSONCompareMode.STRICT));
  }

  @Test
  void should_not_obfuscate_with_null_object() {
    // Given
    MaskerService maskerService = createMaskerService();
    ObfuscationServiceImpl underTest =
        new ObfuscationServiceImpl(
            List.of(new ValueObfuscate(maskerService), new JsonObfuscate(maskerService)));
    ObjectObfuscateConfigImpl objectObfuscateConfig = ObjectObfuscateConfigImpl.builder().build();
    User userData = null;

    // When
    User result = underTest.obfuscateObject(userData, objectObfuscateConfig);

    // Then
    assertThat(result).isNull();
  }

  @Test
  void should_not_obfuscate_with_null_config() {
    // Given
    MaskerService maskerService = createMaskerService();
    ObfuscationServiceImpl underTest =
        new ObfuscationServiceImpl(
            List.of(new ValueObfuscate(maskerService), new JsonObfuscate(maskerService)));
    User userData = getUser(null);

    // When
    User result = underTest.obfuscateObject(userData, null);

    // Then
    assertThat(result).isEqualTo(userData);
  }

  @Test
  void should_not_obfuscate_with_null_ObfuscateConfigByProperty() {
    // Given
    MaskerService maskerService = createMaskerService();
    ObfuscationServiceImpl underTest =
        new ObfuscationServiceImpl(
            List.of(new ValueObfuscate(maskerService), new JsonObfuscate(maskerService)));
    User userData = getUser(null);
    ObjectObfuscateConfigImpl objectObfuscateConfig = ObjectObfuscateConfigImpl.builder().build();

    // When
    User result = underTest.obfuscateObject(userData, objectObfuscateConfig);

    // Then
    assertThat(result).isEqualTo(userData);
  }

  @Test
  void should_not_obfuscate_empty_map() {
    // Given
    MaskerService maskerService = createMaskerService();
    ObfuscationServiceImpl underTest =
        new ObfuscationServiceImpl(List.of(new ValueObfuscate(maskerService)));

    Map<String, ObfuscateConfig> mapConfig = new HashMap<>();
    mapConfig.put(
        "preferences",
        ObjectObfuscateConfigImpl.builder()
            .obfuscateConfigByProperty(Map.of("cardNumber", new ValueObfuscateConfig()))
            .build());

    ObjectObfuscateConfigImpl objectObfuscateConfig =
        ObjectObfuscateConfigImpl.builder().obfuscateConfigByProperty(mapConfig).build();

    User userData = User.builder().preferences(new HashMap<>()).build();

    // When
    User result = underTest.obfuscateObject(userData, objectObfuscateConfig);

    // Then
    assertThat(result).isEqualTo(userData);
  }

  @Getter
  @Setter
  @AllArgsConstructor
  public static class CustomUser {
    private final Map<String, Integer> preferences;
  }

  @Test
  void should_not_obfuscate_map_of_different_types_than_string_string() {
    // Given
    MaskerService maskerService = createMaskerService();
    ObfuscationServiceImpl underTest =
        new ObfuscationServiceImpl(List.of(new ValueObfuscate(maskerService)));

    Map<String, ObfuscateConfig> mapConfig = new HashMap<>();
    mapConfig.put(
        "preferences",
        ObjectObfuscateConfigImpl.builder()
            .obfuscateConfigByProperty(Map.of("cardNumber", new ValueObfuscateConfig()))
            .build());

    ObjectObfuscateConfigImpl objectObfuscateConfig =
        ObjectObfuscateConfigImpl.builder().obfuscateConfigByProperty(mapConfig).build();

    CustomUser userData = new CustomUser(Map.of("key", 1));
    // When
    CustomUser result = underTest.obfuscateObject(userData, objectObfuscateConfig);

    // Then
    assertThat(result).isEqualTo(userData);
  }

  @Test
  void should_not_obfuscate_map_when_config_is_not_a_ObjectObfuscateConfig() {
    // Given
    MaskerService maskerService = createMaskerService();
    ObfuscationServiceImpl underTest =
        new ObfuscationServiceImpl(List.of(new ValueObfuscate(maskerService)));

    Map<String, ObfuscateConfig> mapConfig = new HashMap<>();
    mapConfig.put("preferences", new FakeValueObfuscateConfig());

    ObjectObfuscateConfigImpl objectObfuscateConfig =
        ObjectObfuscateConfigImpl.builder().obfuscateConfigByProperty(mapConfig).build();

    Map<String, String> preferences = new HashMap<>();
    preferences.put("api-key", "HHpbgQh2LYU78xaX9amy0mQhenGMpmVA");
    preferences.put("cardNumber", "4111 1111 1111 1111");
    User userData = User.builder().preferences(preferences).build();

    // When
    User result = underTest.obfuscateObject(userData, objectObfuscateConfig);

    // Then
    assertThat(result).isEqualTo(userData);
  }
}
