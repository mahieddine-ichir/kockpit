package com.accor.wcp.sample.obfuscationlib.bench;

import com.accor.wcp.obfuscation.ObfuscateConfig;
import com.accor.wcp.obfuscation.ObfuscationService;
import com.accor.wcp.obfuscation.impl.ObjectObfuscateConfigImpl;
import com.accor.wcp.obfuscation.impl.obfuscators.json.JsonObfuscateConfig;
import com.accor.wcp.obfuscation.impl.obfuscators.value.ValueObfuscateConfig;
import com.accor.wcp.obfuscation.impl.obfuscators.xml.XmlObfuscateConfig;
import com.accor.wcp.sample.obfuscationlib.model.Address;
import com.accor.wcp.sample.obfuscationlib.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.nio.file.Files.readString;
import static java.util.Objects.requireNonNull;

@RestController
@RequiredArgsConstructor
public class ObfuscateBenchController {

  private final ObfuscationService obfuscationService;

  private static XmlObfuscateConfig createObfuscateConfig() {
    return XmlObfuscateConfig.builder()
        .pathConfigs(
            List.of(
                XmlObfuscateConfig.PathConfig.builder()
                    .path(
                        "/bench/xml/list/ReloadReservationTO/reloadReservableTOs/ReloadReservableTO/recipientEmail")
                    .maskerId("email")
                    .build(),
                XmlObfuscateConfig.PathConfig.builder()
                    .path(
                        "/bench/xml/list/ReloadReservationTO/reloadReservableTOs/ReloadReservableTO/reserverEmail")
                    .maskerId("email")
                    .build(),
                XmlObfuscateConfig.PathConfig.builder()
                        .path(
                                "/bench/xml/list/ReloadReservationTO/reloadReservableTOs/ReloadReservableTO/recipientFirstname")
                        .maskerId("keepLast2")
                    .build(),
                XmlObfuscateConfig.PathConfig.builder()
                    .path(
                        "/bench/xml/list/ReloadReservationTO/reloadReservableTOs/ReloadReservableTO/hostFirstname")
                    .build(),
                    XmlObfuscateConfig.PathConfig.builder()
                            .path(
                                    "/bench/xml/list/ReloadReservationTO/reloadReservableTOs/ReloadReservableTO/recipientTel")
                            .maskerId("keepLast4")
                    .build(),
                XmlObfuscateConfig.PathConfig.builder()
                    .path("/bench/xml/list/ReloadReservationTO/hostTOList/HostTO[@email]")
                    .maskerId("email")
                    .build(),
                // this path does not exist
                XmlObfuscateConfig.PathConfig.builder()
                    .path("/bench/xml/list/ReloadReservationTO/tel")
                    .build()))
        .build();
  }

  private static JsonObfuscateConfig createJsonObfuscateConfig() {
    return JsonObfuscateConfig.builder()
        .pathConfigs(
            List.of(
                    JsonObfuscateConfig.PathConfig.builder()
                            .path("$.bookingRequests[*].hotes[*].nomHote")
                            .maskerId("keepFirst2")
                    .build(),
                    JsonObfuscateConfig.PathConfig.builder()
                            .path("$.bookingRequests[*].hotes[*].prenomHote")
                            .maskerId("keepFirst2")
                    .build(),
                    JsonObfuscateConfig.PathConfig.builder()
                            .path("$.bookingRequests[*].beneficiaire.nom1")
                            .maskerId("keepLast2")
                    .build(),
                JsonObfuscateConfig.PathConfig.builder()
                    .path("$.bookingRequests[*].beneficiaire.adresse.email")
                    .maskerId("email")
                    .build(),
                    JsonObfuscateConfig.PathConfig.builder()
                            .path("$.bookingRequests[*].beneficiaire.adresse.telephone.number")
                            .maskerId("keepLast4")
                    .build(),
                JsonObfuscateConfig.PathConfig.builder()
                    .path("$.bookingRequests[*].reservataire.nom1")
                    .build(),
                JsonObfuscateConfig.PathConfig.builder()
                    .path("$.bookingRequests[*].reservataire.adresse.email")
                    .maskerId("email")
                    .build(),
                    JsonObfuscateConfig.PathConfig.builder()
                            .path("$.bookingRequests[*].reservataire.adresse.telephone.number")
                            .maskerId("keepLast3")
                    .build(),
                    JsonObfuscateConfig.PathConfig.builder()
                            .path("$.bookingRequests[*].facturation.nom1")
                            .maskerId("keepFirst1")
                    .build(),
                JsonObfuscateConfig.PathConfig.builder()
                    .path("$.bookingRequests[*].facturation.adresse.email")
                    .maskerId("email")
                    .build(),
                    JsonObfuscateConfig.PathConfig.builder()
                            .path("$.bookingRequests[*].facturation.adresse.telephone.number")
                            .maskerId("keepFirst4")
                    .build(),
                    JsonObfuscateConfig.PathConfig.builder()
                            .path("$.bookingRequests[*].garantie.numCarteGarantie")
                            .maskerId("keepFirst4")
                    .build(),
                    JsonObfuscateConfig.PathConfig.builder()
                            .path("$.bookingRequests[*].garantie.nomPorteurGarantie")
                            .maskerId("keepFirst1")
                    .build()))
        .build();
  }

  private long obfuscate(int count, String data, ObfuscateConfig config)
      throws InterruptedException {
    ExecutorService es = Executors.newFixedThreadPool(20);
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
      es.execute(() -> obfuscationService.obfuscate(data, config));
    }
    es.shutdown();
    es.awaitTermination(5, TimeUnit.MINUTES);
    return System.currentTimeMillis() - startTime;
  }

  @PostMapping(value = "/obfuscate/bench/xml")
  @ResponseBody
  public ResponseEntity<String> obfuscateBenchXml(@RequestParam("count") int count)
      throws URISyntaxException, IOException, InterruptedException {
    String xml =
        readString(
            Path.of(
                requireNonNull(
                        ObfuscateBenchController.class.getResource("/bench/xml/test-ob1.xml"))
                    .toURI()));
    long executionTime = obfuscate(count, xml, createObfuscateConfig());

    return ResponseEntity.ok(
        "Execution time " + executionTime + " for " + count + " XML obfuscation");
  }

  @PostMapping(value = "/obfuscate/bench/json")
  @ResponseBody
  public ResponseEntity<String> obfuscateBenchJson(@RequestParam("count") int count)
      throws URISyntaxException, IOException, InterruptedException {
    String json =
        readString(
            Path.of(
                requireNonNull(
                        ObfuscateBenchController.class.getResource("/bench/json/test-ob1.json"))
                    .toURI()));

    long executionTime = obfuscate(count, json, createJsonObfuscateConfig());
    return ResponseEntity.ok(
        "Execution time " + executionTime + " for " + count + " JSON obfuscation");
  }

  private static User buildUser() {
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
                .country("FRANCE")
                .zipCode("91390")
                .city("Morsang")
                .build())
        .build();
  }

  @PostMapping(value = "/obfuscate/bench/object")
  @ResponseBody
  public ResponseEntity<String> obfuscateBenchObject(@RequestParam("count") int count)
      throws InterruptedException {

    ValueObfuscateConfig valueObfuscateConfig = new ValueObfuscateConfig();
    Map<String, ObfuscateConfig> mapConfig = new HashMap<>();
    mapConfig.put("address.city", valueObfuscateConfig);
    mapConfig.put("address.street1", valueObfuscateConfig);
    mapConfig.put("firstname", valueObfuscateConfig);
    mapConfig.put("lastname", valueObfuscateConfig);
    mapConfig.put(
        "preferences",
        ObjectObfuscateConfigImpl.builder()
            .obfuscateConfigByProperty(
                Map.of(
                        "api-key",
                        new ValueObfuscateConfig("keepLast3"),
                        "cardNumber",
                        new ValueObfuscateConfig()))
            .build());

    ObjectObfuscateConfigImpl objectObfuscateConfig =
        ObjectObfuscateConfigImpl.builder().obfuscateConfigByProperty(mapConfig).build();

    ExecutorService es = Executors.newFixedThreadPool(20);
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
      es.execute(() -> obfuscationService.obfuscateObject(buildUser(), objectObfuscateConfig));
    }
    es.shutdown();
    es.awaitTermination(5, TimeUnit.MINUTES);
    return ResponseEntity.ok(
        "Execution time "
            + (System.currentTimeMillis() - startTime)
            + " for "
            + count
            + " Object obfuscation");
  }
}
