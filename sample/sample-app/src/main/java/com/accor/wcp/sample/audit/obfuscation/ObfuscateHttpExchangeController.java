package com.accor.wcp.sample.audit.obfuscation;

import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ObfuscateHttpExchangeController {

  public static final String API_KEY = "api-key";
  private final RestTemplate restTemplate;

  @Value("${application.basepath}")
  private String basePath;

  /** Use to demonstrate obfuscation capacity in complex http exchange. */
  @PostMapping(
      value = "/obfuscate/v1/_bookItems",
      consumes = {"application/json"},
      produces = {"application/json"})
  public ResponseEntity<String> bookItems(@RequestBody String body)
      throws URISyntaxException, IOException {
    // Irma JSON
    String json =
        Files.readString(
            Path.of(getClass().getResource("/data/postIrmaOfferRequest.json").toURI()));
    HttpHeaders jsonHeaders = new HttpHeaders();
    jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
    jsonHeaders.add(API_KEY, "0imfnc8mVLWwsAawjYr4Rx-Af50DDqtlx");
    restTemplate.postForEntity(
        basePath + "/irmaprocess/v3.1/offer", new HttpEntity<>(json, jsonHeaders), String.class);

    // do not obfuscate, path expression does not match in application-obfuscation.yml
    try {
      String basketId = JsonPath.read(body, "$.order.items[0].basketItemId");
      MultiValueMap<String, String> headers = new HttpHeaders();
      headers.add("x-accept-version", "1");
      HttpEntity<String> requestEntity = new HttpEntity<>(headers);
      restTemplate.exchange(
          basePath + "/basket/v0/baskets/" + basketId + "?login=testFakeLogin",
          HttpMethod.GET,
          requestEntity,
          String.class);
    } catch (Exception e) {
      log.warn("Path $.order.items[0].basketItemId do not exist in json request body", e);
    }

    // TARS XML
    String xml =
        Files.readString(
            Path.of(getClass().getResource("/data/bookMultipleTarsRequest.xml").toURI()));
    HttpHeaders xmlHeaders = new HttpHeaders();
    xmlHeaders.setContentType(MediaType.APPLICATION_XML);
    jsonHeaders.add(API_KEY, "d735a06a-4c17-4098-b63f-1e4cfab614a4");
    restTemplate.postForEntity(
        basePath + "/service-layer/bookMultiple", new HttpEntity<>(xml, xmlHeaders), String.class);

    return ResponseEntity.ok("created");
  }
}
