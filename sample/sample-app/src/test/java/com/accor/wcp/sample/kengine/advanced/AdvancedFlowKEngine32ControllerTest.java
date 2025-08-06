package com.accor.wcp.sample.kengine.advanced;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.accor.wcp.sample.AbstractWcpSampleApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class AdvancedFlowKEngine32ControllerTest extends AbstractWcpSampleApplicationTest {

  @Test
  void should_load_enricheduser_normally() {
    String url = "http://localhost:" + port + "/wcpsamples/kengine/v32/advanced";
    ResponseEntity<EnrichedUser> response = testRestTemplate.getForEntity(url, EnrichedUser.class);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getUser().getName()).isNotNull().isEqualTo("Cyril");
  }
}
