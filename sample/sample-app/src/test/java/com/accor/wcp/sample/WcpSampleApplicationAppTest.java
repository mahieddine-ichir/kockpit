package com.accor.wcp.sample;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class WcpSampleApplicationAppTest extends AbstractWcpSampleApplicationTest {

  @Test
  void should_start() {
    String url =
        "http://localhost:" + port + "/wcpsamples/actuator/health";
    ResponseEntity<String> response = testRestTemplate.getForEntity(url, String.class);
    assertThat(response.getBody()).contains("UP");
  }
}
