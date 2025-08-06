package com.accor.wcp.sample.kengine.hello;

import com.accor.wcp.sample.AbstractWcpSampleApplicationTest;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HelloFlowKEngine32ControllerTest extends AbstractWcpSampleApplicationTest {

  @Test
  void should_return_hello() {
    String url = getBaseUrl() + "hello";
    ResponseEntity<String> response = testRestTemplate.getForEntity(url, String.class);
    assertThat(response.getBody()).contains("Hello");
  }

  @NotNull
  private String getBaseUrl() {
    return "http://localhost:" + port + "/wcpsamples/kengine/v32/";
  }

  @Test
  void should_return_hello_random() {
    String url = getBaseUrl() + "hello-random/cyril";
    String assertEqualsValue = "Hello 0.";
    should_call_and_assertequals(url, assertEqualsValue);
  }

  private void should_call_and_assertequals(String url, String assertEqualsValue) {
    ResponseEntity<String> response = testRestTemplate.getForEntity(url, String.class);
    assertThat(response.getBody()).startsWith(assertEqualsValue);
  }

  @Test
  void should_return_hello_name() {
    String url = getBaseUrl() + "hello/Cyril";
    should_call_and_assertequals(url, "Hey Cyril");
  }

  @Test
  void should_return_hello_void() {
    String url = getBaseUrl() + "hello-void";
    should_call_and_assertequals(url, "NoData");
  }

  @Test
  void should_return_hello_fullcontext() {
    String url = getBaseUrl() + "hello-fullcontext";
    ResponseEntity<Map> response = testRestTemplate.getForEntity(url, Map.class);
    assertThat(response.getBody()).isNotNull();
  }
}
