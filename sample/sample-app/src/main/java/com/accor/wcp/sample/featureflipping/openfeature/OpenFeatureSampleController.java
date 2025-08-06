package com.accor.wcp.sample.featureflipping.openfeature;

import com.accor.wcp.sdk.application.lifecycle.SdkBeforeInitializationLifeCycleMarker;
import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.OpenFeatureAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class OpenFeatureSampleController implements SdkBeforeInitializationLifeCycleMarker {

  private final OpenFeatureAPI openFeatureAPI;

  @GetMapping(value = "/openfeature/hello", produces = MediaType.TEXT_HTML_VALUE)
  public String openfeature() {
    final Client client = openFeatureAPI.getClient();

    String result = "<h1>Tester ... Feature flipping ... </h1>\n<br/><br/>";

    // Evaluate welcome-message feature flag
    if (client.getBooleanValue("ff.key.always-active", false)) {
      result += "<h2>Hello, welcome to this OpenFeature-enabled website!\n</h2><br/>";
    }

    if (client.getBooleanValue("ff.key1.expired", false)) {
      result += "<h2>Hello, this key is already expired!\n</h2><br/>";
    }

    if (client.getBooleanValue("ff.key.feature1", false)) {
      result += "<h2>Hello, feature1!\n</h2><br/>";
    }

    return result;
  }
}
