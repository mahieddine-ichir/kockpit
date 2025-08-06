package com.accor.wcp.sample.trap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class TrapController {

  private final RestTemplate restTemplate;

  public TrapController(@Qualifier("fakeRestTemplate") RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @GetMapping(value = "/trap")
  public ResponseEntity<String> home(HttpServletRequest request, HttpServletResponse response) {
    String url = "https://httpbin.org/headers?" + request.getQueryString();
    return restTemplate.getForEntity(url, String.class);
  }
}
