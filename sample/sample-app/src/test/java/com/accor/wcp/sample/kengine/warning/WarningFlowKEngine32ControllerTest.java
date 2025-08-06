package com.accor.wcp.sample.kengine.warning;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.accor.wcp.sample.AbstractWcpSampleApplicationTest;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class WarningFlowKEngine32ControllerTest extends AbstractWcpSampleApplicationTest {

  @NotNull
  private String getBaseUrl() {
    return "http://localhost:" + port + "/wcpsamples/kengine/v32/";
  }

  @Test
  void should_return_true() {
    String url = getBaseUrl() + "warning";
    ResponseEntity<Boolean> response = testRestTemplate.getForEntity(url, Boolean.class);
    assertThat(response.getBody()).isTrue();
  }
}
