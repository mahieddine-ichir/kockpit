package com.accor.wcp.console.services.sqk;

import com.accor.wcp.console.services.core.appmanifest.manifest.Manifest;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

@UtilityClass
public class TestUtils {

  @SneakyThrows
  public static List<Manifest> loadFakeManifests() {
    JSONObject manifestjson =
        new JSONObject(loadJsonIntoString("manifest/wcxss-manifest-test.json"));
    Manifest manifest =
        new Manifest("wcxss-manifest-test.json", Instant.now(), manifestjson, "Test");
    return Collections.singletonList(manifest);
  }

  @SneakyThrows
  public static List<Manifest> loadFakeSqsDlqManifests() {
    JSONObject manifestjson =
        new JSONObject(loadJsonIntoString("manifest/wcxss-sqsdlq-it-manifest.json"));
    Manifest manifest =
        new Manifest("wcxss-sqsdlq-it-manifest.json", Instant.now(), manifestjson, "Test");
    return Collections.singletonList(manifest);
  }

  @SneakyThrows
  private static String loadJsonIntoString(final String testCase) {
    final InputStream inputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(testCase);
    if (Objects.nonNull(inputStream)) {
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } else {
      return StringUtils.EMPTY;
    }
  }
}
