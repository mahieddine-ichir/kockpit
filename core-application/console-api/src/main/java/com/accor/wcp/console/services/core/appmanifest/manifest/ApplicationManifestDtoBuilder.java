package com.accor.wcp.console.services.core.appmanifest.manifest;

import static java.util.Objects.nonNull;
import static java.util.stream.StreamSupport.stream;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Slf4j
public class ApplicationManifestDtoBuilder {

  public List<ApplicationManifestDto> buildApplicationManifestsDto(Manifest manifest) {
    JSONObject manifestJSON = manifest.getContent();
    String env = manifestJSON.getString("env");
    String domain = manifestJSON.getString("domain");
    JSONArray applications = manifestJSON.getJSONArray("applications");

    List<String> groups = null;
    try {
      JSONArray jsonArrayGroups = manifestJSON.getJSONArray("groups");
      if (nonNull(jsonArrayGroups)) {
        groups =
            stream(jsonArrayGroups.spliterator(), false)
                .map(Object::toString)
                .toList();
      }
    } catch (JSONException e) {
      log.debug(
          "No Json 'groups' property found. Default permission is open: all permit... manifest: {}",
          manifestJSON);
    }

    List<String> finalGroups = groups;
    return stream(applications.spliterator(), false)
        .map(
            app ->
                this.buildAppManifest(
                    manifest.getName(),
                    manifest.getLastModificationDate(),
                    env,
                    domain,
                    finalGroups,
                    (JSONObject) app,
                    manifest.getSource()))
        .toList();
  }

  private ApplicationManifestDto buildAppManifest(
      String name,
      Instant lastModificationDate,
      String env,
      String domain,
      List<String> groups,
      JSONObject app,
      String source) {
    return ApplicationManifestDto.builder()
        .domain(domain)
        .subDomain(getApplicationSubDomain(app))
        .env(env)
        .groups(groups)
        .id(app.getString("id"))
        .name(name)
        .lastModificationDate(lastModificationDate)
        .source(source)
        .label(app.getString("label"))
        .services(getApplicationServices(app))
        .build();
  }

  private Map<String, List<Map<String, Object>>> getApplicationServices(JSONObject application) {
    Map<String, ?> services = application.getJSONObject("services").toMap();
    return (Map<String, List<Map<String, Object>>>) services;
  }

  private String getApplicationSubDomain(JSONObject application) {
    return application.isNull("subDomain") ? null : application.getString("subDomain");
  }
}
