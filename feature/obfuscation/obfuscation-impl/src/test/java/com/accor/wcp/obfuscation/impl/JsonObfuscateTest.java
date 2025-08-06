package com.accor.wcp.obfuscation.impl;

import com.accor.wcp.obfuscation.impl.masker.MaskerServiceImpl;
import com.accor.wcp.obfuscation.impl.masker.maskers.EmailMasker;
import com.accor.wcp.obfuscation.impl.masker.maskers.KeepFirstNbCharsMasker;
import com.accor.wcp.obfuscation.impl.masker.maskers.KeepLastNbCharsMasker;
import com.accor.wcp.obfuscation.impl.obfuscators.json.JsonObfuscateConfig;
import com.accor.wcp.obfuscation.impl.obfuscators.json.JsonObfuscateConfig.PathConfig;
import com.accor.wcp.obfuscation.masker.MaskerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.nio.file.Files.readString;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
class JsonObfuscateTest {

  private static JsonObfuscateConfig createJsonObfuscateConfig() {
    return JsonObfuscateConfig.builder()
            .pathConfigs(
                    List.of(
                            PathConfig.builder()
                                    .path("$.bookingRequests[*].hotes[*].nomHote")
                                    .maskerId("keepLast1")
                                    .build(),
                            PathConfig.builder()
                                    .path("$.bookingRequests[*].hotes[*].prenomHote")
                                    .maskerId("keepLast2")
                                    .build(),
                            PathConfig.builder()
                                    .path("$.bookingRequests[*].beneficiaire.nom1")
                                    .maskerId("keepLast3")
                                    .build(),
                            PathConfig.builder()
                                    .path("$.bookingRequests[*].beneficiaire.adresse.email")
                                    .maskerId("email")
                                    .build(),
                            PathConfig.builder()
                                    .path("$.bookingRequests[*].beneficiaire.adresse.telephone.number")
                                    .maskerId("keepLast4")
                                    .build(),
                            PathConfig.builder().path("$.bookingRequests[*].reservataire.nom1").build(),
                            PathConfig.builder()
                                    .path("$.bookingRequests[*].reservataire.adresse.email")
                                    .maskerId("email")
                                    .build(),
                            PathConfig.builder()
                                    .path("$.bookingRequests[*].reservataire.adresse.telephone.number")
                                    .maskerId("keepLast4")
                                    .build(),
                            PathConfig.builder()
                                    .path("$.bookingRequests[*].facturation.nom1")
                                    .maskerId("keepFirst1")
                                    .build(),
                            PathConfig.builder()
                                    .path("$.bookingRequests[*].facturation.nom2")
                                    .maskerId("keepFirst2")
                                    .build(),
                            PathConfig.builder()
                                    .path("$.bookingRequests[*].facturation.adresse.email")
                                    .maskerId("email")
                                    .build(),
                            PathConfig.builder()
                                    .path("$.bookingRequests[*].facturation.adresse.telephone.number")
                                    .maskerId("keepFirst4")
                                    .build(),
                            PathConfig.builder()
                                    .path("$.bookingRequests[*].garantie.numCarteGarantie")
                                    .maskerId("keepFirst3")
                                    .build(),
                            PathConfig.builder()
                                    .path("$.bookingRequests[*].garantie.nomPorteurGarantie")
                                    .maskerId("keepFirst2")
                                    .build()))
            .build();
  }

  private static JsonObfuscate createJsonObfuscate() {
    MaskerService maskerService = createMaskerService();
    return new JsonObfuscate(maskerService);
  }

  private static MaskerService createMaskerService() {
    return new MaskerServiceImpl(
            List.of(new EmailMasker(), new KeepFirstNbCharsMasker(1), new KeepFirstNbCharsMasker(2), new KeepFirstNbCharsMasker(3), new KeepFirstNbCharsMasker(4),
                    new KeepLastNbCharsMasker(1), new KeepLastNbCharsMasker(2), new KeepLastNbCharsMasker(3), new KeepLastNbCharsMasker(4)));
  }

  @Test
  void should_obfuscate_json() throws Exception {
    // Given
    JsonObfuscate underTest = createJsonObfuscate();
    String json =
            readString(
                    Path.of(
                            requireNonNull(JsonObfuscateTest.class.getResource("/json/test-ob1.json"))
                                    .toURI()));
    JsonObfuscateConfig config = createJsonObfuscateConfig();

    // When
    String obfuscatedJson = underTest.obfuscate(json, config);

    // Then
    String expectedObfuscated =
            readString(
                    Path.of(
                            requireNonNull(
                                    JsonObfuscateTest.class.getResource("/json/test-ob1-obfuscated1.json"))
                                    .toURI()));
    assertThat(obfuscatedJson).isNotNull();
    JSONAssert.assertEquals(obfuscatedJson,
            expectedObfuscated,
            new DefaultComparator(JSONCompareMode.STRICT));
  }

  @Test
  void should_not_obfuscate_json_when_content_is_not_a_json() {
    // Given
    JsonObfuscate underTest = createJsonObfuscate();
    JsonObfuscateConfig config = createJsonObfuscateConfig();

    // When
    String obfuscatedJson = underTest.obfuscate("it is not a valid json !!", config);

    assertThat(obfuscatedJson).isEqualTo("it is not a valid json !!");
  }

  @Test
  void should_obfuscate_1_JSON() throws URISyntaxException, IOException {
    // Given
    String json =
            readString(
                    Path.of(
                            requireNonNull(JsonObfuscateTest.class.getResource("/json/test-ob1.json"))
                                    .toURI()));

    JsonObfuscate underTest = createJsonObfuscate();
    JsonObfuscateConfig config = createJsonObfuscateConfig();

    // When
    long startTime = System.currentTimeMillis();
    String result = underTest.obfuscate(json, config);

    // Then
    assertThat(result).isNotNull();
    log.debug("JSON obfuscation take {} ms for 1 JSON", System.currentTimeMillis() - startTime);
  }

  @Test
  void should_obfuscate_10000_JSON_multi_threaded()
          throws URISyntaxException, IOException, InterruptedException {
    // Given
    String json =
            readString(
                    Path.of(
                            requireNonNull(JsonObfuscateTest.class.getResource("/json/test-ob1.json"))
                                    .toURI()));

    JsonObfuscate underTest = createJsonObfuscate();
    JsonObfuscateConfig config = createJsonObfuscateConfig();

    // When
    ExecutorService es = Executors.newFixedThreadPool(20);
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < 10000; i++) {
      es.execute(() -> underTest.obfuscate(json, config));
    }
    es.shutdown();
    boolean result = es.awaitTermination(2, TimeUnit.MINUTES);

    // Then
    assertThat(result).isTrue();
    log.debug("JSON obfuscation take {} ms for 10000 JSON", System.currentTimeMillis() - startTime);
  }
}
