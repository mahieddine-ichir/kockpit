package org.kockpit.features.manifest.services.backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.kockpit.features.manifest.services.ManifestBackendRepository;
import org.kockpit.features.manifest.services.ManifestReader;
import org.kockpit.features.manifest.services.dto.ManifestDto;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /manifests filtering and the 403 guard on domain/env endpoints, wired as in the app
 * (same interceptor, same path patterns), with the user's groups as authorities.
 */
class ManifestAccessApiTest {

    private static final List<ManifestDto> MANIFESTS = List.of(
            manifest("""
                    { "domain": "wcxss", "env": "dev", "name": "wcxss-dev",
                      "groups": ["ACCOR-WCPConsole-WCXSSTeam"], "policies": null,
                      "services": [ { "type": "audit", "name": "a", "id": "a" } ] }"""),
            manifest("""
                    { "domain": "wcoff", "env": "review-mr-1551", "name": "wcoff-review",
                      "groups": null,
                      "policies": [
                        {"groups":["ACCOR-WCPConsole-QaNGATeam"],"permissions":[],"resources":["audit:*"]},
                        {"groups":["ACCOR-WCPConsole-WCOFFTeam"],"permissions":[],"resources":["audit:*","cache:*","dynaconfig:*","featureflipping:*","sqsdlq:*"]}
                      ],
                      "services": [ { "type": "audit", "name": "offers-api", "id": "offers-api" } ] }"""),
            manifest("""
                    { "domain": "wcc", "env": "dev", "name": "wcc-dev",
                      "services": [ { "type": "audit", "name": "c", "id": "c" } ] }"""));

    @AfterEach
    void clearUser() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void manifests_only_lists_what_the_user_can_see() throws Exception {
        MockMvc mvc = mvc(true);

        loginWith("ACCOR-WCPConsole-WCOFFTeam");
        mvc.perform(asUser(get("/manifests")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].domain").value("wcoff"))
                .andExpect(jsonPath("$[1].domain").value("wcc"));

        loginWith("ACCOR-WCPConsole-WBNO-ReadOnly");
        mvc.perform(asUser(get("/manifests")))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].domain").value("wcc"));
    }

    @Test
    void domain_env_endpoints_are_forbidden_without_access() throws Exception {
        MockMvc mvc = mvc(true);

        loginWith("ACCOR-WCPConsole-WBNO-ReadOnly");
        mvc.perform(get("/wcxss/dev/audits/_search")).andExpect(status().isForbidden());
        mvc.perform(get("/wcxss/dev/heartbeat")).andExpect(status().isForbidden());
        mvc.perform(get("/wcc/dev/audits/_search")).andExpect(status().isOk());
        mvc.perform(get("/unknown/dev/audits/_search")).andExpect(status().isForbidden());

        loginWith("ACCOR-WCPConsole-WCXSSTeam");
        mvc.perform(get("/wcxss/dev/audits/_search")).andExpect(status().isOk());
        mvc.perform(get("/wcxss/dev/dashboard/app_details")).andExpect(status().isOk());
        mvc.perform(get("/wcxss/dev/cache/anything")).andExpect(status().isOk());
    }

    @Test
    void policies_scope_access_by_service_type() throws Exception {
        MockMvc mvc = mvc(true);

        loginWith("ACCOR-WCPConsole-QaNGATeam");
        mvc.perform(get("/wcoff/review-mr-1551/audits/_search")).andExpect(status().isOk());
        mvc.perform(get("/wcoff/review-mr-1551/cache/currenciesCache")).andExpect(status().isForbidden());
        mvc.perform(get("/wcoff/review-mr-1551/heartbeat")).andExpect(status().isOk());

        loginWith("ACCOR-WCPConsole-WCOFFTeam");
        mvc.perform(get("/wcoff/review-mr-1551/cache/currenciesCache")).andExpect(status().isOk());
        mvc.perform(get("/wcoff/review-mr-1551/dyna-config/offers-api")).andExpect(status().isOk());
    }

    @Test
    void without_cognito_configured_nothing_is_filtered() throws Exception {
        MockMvc mvc = mvc(false);

        mvc.perform(get("/manifests")).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/wcxss/dev/audits/_search")).andExpect(status().isOk());
    }

    private static MockMvc mvc(boolean cognitoConfigured) {
        ManifestBackendService backend = new ManifestBackendService(new InMemoryRepository());
        ManifestAccessService access = new ManifestAccessService(backend,
                cognitoConfigured ? "https://cognito-idp.eu-west-1.amazonaws.com/pool" : "");
        return MockMvcBuilders.standaloneSetup(new ManifestApi(backend, access), new FeatureEndpoints())
                .addMappedInterceptors(ManifestAccessWebConfig.pathPatterns(), new ManifestAccessInterceptor(access))
                .build();
    }

    // The real app's security filters expose the user both ways: to the interceptor through the
    // SecurityContext and to controller Authentication parameters through the request principal.
    private static void loginWith(String... groups) {
        TestingAuthenticationToken user = new TestingAuthenticationToken("user", null, groups);
        user.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(user);
    }

    private static MockHttpServletRequestBuilder asUser(MockHttpServletRequestBuilder request) {
        return request.principal(SecurityContextHolder.getContext().getAuthentication());
    }

    private static ManifestDto manifest(String json) {
        return ManifestReader.read(json.getBytes(StandardCharsets.UTF_8));
    }

    // Same paths as the real feature controllers (SearchApi, DashboardApi, CacheApi, ...).
    @RestController
    static class FeatureEndpoints {
        @GetMapping("/{domain}/{env}/audits/_search")
        String audits() { return "ok"; }

        @GetMapping("/{domain}/{env}/dashboard/app_details")
        String dashboard() { return "ok"; }

        @GetMapping("/{domain}/{env}/cache/{id}")
        String cache() { return "ok"; }

        @GetMapping("/{domain}/{env}/dyna-config/{id}")
        String dynaConfig() { return "ok"; }

        @GetMapping("/{domain}/{env}/heartbeat")
        String heartbeat() { return "ok"; }
    }

    static class InMemoryRepository implements ManifestBackendRepository {
        @Override
        public List<ManifestDto> findAll() { return MANIFESTS; }

        @Override
        public Optional<ManifestDto> findByName(String name) {
            return MANIFESTS.stream().filter(m -> name.equals(m.getName())).findFirst();
        }

        @Override
        public ManifestDto save(ManifestDto manifestDto) { return manifestDto; }
    }
}
