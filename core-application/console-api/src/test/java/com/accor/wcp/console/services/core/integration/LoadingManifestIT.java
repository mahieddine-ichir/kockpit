package com.accor.wcp.console.services.core.integration;

import com.accor.wcp.console.services.core.appmanifest.s3.datasource.ResourceLoaderS3;
import com.accor.wcp.console.services.core.integration.utils.DedicatedBaseIt;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(OrderAnnotation.class)
class LoadingManifestIT extends DedicatedBaseIt {

  @Autowired MockMvc mockMvc;

  // test doesn't launch without these beans
  @MockitoBean
  ResourceLoaderS3 resourceLoaderS3;
  @MockitoBean
  RestHighLevelClient restHighLevelClient;
  @MockitoBean JwtDecoder jwtDecoder;

  @Test
  @Order(1)
  void should_not_upload_invalid_manifest_and_return_500() throws Exception {
    InputStream inputStream =
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("manifest/wcxss-invalid-manifest-test.json");
    MockMultipartFile multipartFile =
        new MockMultipartFile(
            "file", "wcxss-invalid-manifest-test.json", "application/json", inputStream);

    performPostManifests(multipartFile, 500);
    performGetManifests(0);
    performGetConsoleConfig(0, 0, 0, 0, 0, 0);
  }

  @Test
  @Order(2)
  void should_upload_manifest_then_return_manifest_and_console_config_with_status_200()
      throws Exception {
    InputStream inputStream =
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("manifest/wcxss-manifest-test.json");
    MockMultipartFile multipartFile =
        new MockMultipartFile("file", "wcxss-manifest-test.json", "application/json", inputStream);

    performPostManifests(multipartFile, 200);
    performGetManifests(5);
    performGetConsoleConfig(2, 5, 3, 2, 5, 3);
  }

  private void performGetManifests(int manifestsSize) throws Exception {
    mockMvc
        .perform(get("/api/console/manifests", 42L))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$", hasSize(manifestsSize)));
  }

  private void performPostManifests(MockMultipartFile multipartFile, int expectedStatus)
      throws Exception {
    mockMvc
        .perform(multipart("/api/console/manifests").file(multipartFile))
        .andExpect(status().is(expectedStatus));
  }

  private void performGetConsoleConfig(
      int configCacheSize,
      int configAuditSize,
      int configSqsSize,
      int menuCacheSize,
      int menuAuditSize,
      int menuSqsSize)
      throws Exception {
    setUpSecurityContext();
    mockMvc
        .perform(get("/api/console/config", 42L))
        .andExpect(status().is(200))
        .andExpect(
            jsonPath(
                "$.consoleServiceConfigs[0].config.cacheSettingsMap",
                aMapWithSize(configCacheSize)))
        .andExpect(
            jsonPath("$.consoleServiceConfigs[1].config.auditViews", hasSize(configAuditSize)))
        .andExpect(
            jsonPath(
                "$.consoleServiceConfigs[4].config.sqsDlqSettingsDtos", hasSize(configSqsSize)))
        .andExpect(
            jsonPath("$.consoleServiceConfigs[5].config.dynaConfigSettingsMap", aMapWithSize(0)))
        .andExpect(jsonPath("$.consoleServiceMenus[0].menuItems", hasSize(menuCacheSize)))
        .andExpect(jsonPath("$.consoleServiceMenus[1].menuItems", hasSize(menuAuditSize)))
        .andExpect(jsonPath("$.consoleServiceMenus[2].menuItems", hasSize(0))) // darkcanary
        .andExpect(jsonPath("$.consoleServiceMenus[3].menuItems", hasSize(0))) // featureflipping
        .andExpect(jsonPath("$.consoleServiceMenus[4].menuItems", hasSize(menuSqsSize)))
        .andExpect(jsonPath("$.consoleServiceMenus[5].menuItems", hasSize(0))); // dynaconfig
  }

  private static void setUpSecurityContext() {
    HashMap<String, Object> headers = new HashMap<>();
    HashMap<String, Object> claims = new HashMap<>();
    headers.put("Access-Control-Allow-Credentials", "true");
    claims.put("custom:adgroups", Arrays.asList("test1", "admin"));

    Jwt jwt = new Jwt("tokenValue", Instant.now(), Instant.now().plus(1, ChronoUnit.MINUTES), headers, claims);
    JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
  }
}
