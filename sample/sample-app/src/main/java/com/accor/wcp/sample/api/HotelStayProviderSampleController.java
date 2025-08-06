package com.accor.wcp.sample.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HotelStayProviderSampleController {

  @PostMapping(
      value = "/irmaprocess/v3.1/offer",
      consumes = {"application/json"},
      produces = {"application/json"})
  ResponseEntity<String> retrieveOffer(@RequestBody String body)
      throws URISyntaxException, IOException {
    return ResponseEntity.ok(
        Files.readString(
            Path.of(getClass().getResource("/data/postIrmaOfferResponse.json").toURI())));
  }

  @PostMapping(
      value = "/service-layer/bookMultiple",
      consumes = {"application/xml"},
      produces = {"application/xml"})
  ResponseEntity<String> BookMultiple(@RequestBody String body)
      throws URISyntaxException, IOException {
    return ResponseEntity.ok(
        Files.readString(
            Path.of(getClass().getResource("/data/bookMultipleTarsResponse.xml").toURI())));
  }
}
