package org.kockpit.backend.tenants;

import org.junit.jupiter.api.Test;
import org.kockpit.backend.security.local.AttemptLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/** Sign-up by invitation only (the default): real HTTP, PostgreSQL in a container. */
@SpringBootTest(classes = TenantTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "kockpit.security.local.enabled=true",
                "kockpit.security.local.jwt-secret=test-secret-test-secret-test-secret-0123",
                "kockpit.security.local.signup.per-ip-per-hour=1000",
                "kockpit.security.local.admin.email=admin@example.com",
                "kockpit.security.local.admin.password=admin-password-123",
                "kockpit.security.access-control.admin-groups=kockpit-admins",
                "kockpit.security.access-control.hide-unrestricted=true",
        })
@Testcontainers
class SignupInvitationIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("kockpit.security.local.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("kockpit.security.local.datasource.username", POSTGRES::getUsername);
        registry.add("kockpit.security.local.datasource.password", POSTGRES::getPassword);
    }

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final String UNKNOWN_CODE = "kpi_unknown";

    @LocalServerPort
    int port;

    @Autowired
    AttemptLimiter limiter;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void without_an_invitation_nobody_signs_up() throws Exception {
        assertThat(json(send("GET", "/auth/signup", null, null)).get("mode").asString()).isEqualTo("invite");

        HttpResponse<String> refused = send("POST", "/auth/signup",
                "{\"email\":\"%s\",\"password\":\"a-good-password\"}".formatted(email()), null);

        assertThat(refused.statusCode()).isEqualTo(403);
        assertThat(refused.body()).contains("invitation_required");
        assertThat(signup(email(), UNKNOWN_CODE).body()).contains("invalid_invitation");
    }

    @Test
    void an_administrators_invitation_creates_an_account_that_may_create_a_tenant_once() throws Exception {
        String code = accountInvitation(null);

        HttpResponse<String> created = signup(email(), code);

        assertThat(created.statusCode()).isEqualTo(201);
        String cookie = cookieOf(created);
        assertThat(json(send("GET", "/tenants", null, cookie)).get("canCreateTenant").asBoolean()).isTrue();
        assertThat(createTenant(cookie, slug()).statusCode()).isEqualTo(201);
        // used up
        HttpResponse<String> again = signup(email(), code);
        assertThat(again.statusCode()).isEqualTo(404);
        assertThat(again.body()).contains("invalid_invitation");
    }

    @Test
    void a_tenants_invitation_creates_a_member_that_cannot_create_a_tenant() throws Exception {
        String owner = owner();
        String tenant = slug();
        createTenant(owner, tenant);
        String code = tenantInvitation(owner, tenant);

        HttpResponse<String> created = signup(email(), code);

        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(created.body()).contains("tenant:" + tenant);
        String member = cookieOf(created);
        JsonNode mine = json(send("GET", "/tenants", null, member));
        assertThat(mine.get("canCreateTenant").asBoolean()).isFalse();
        assertThat(mine.get("tenants").get(0).get("role").asString()).isEqualTo("member");
        assertThat(createTenant(member, slug()).statusCode()).isEqualTo(403);
        assertThat(signup(email(), code).statusCode()).isEqualTo(404);
    }

    @Test
    void an_invitation_for_an_email_is_for_that_email_only() throws Exception {
        String invited = email();
        String code = accountInvitation(invited);

        assertThat(signup(email(), code).statusCode()).isEqualTo(403);

        assertThat(signup(invited.toUpperCase(), code).statusCode()).isEqualTo(201);
    }

    @Test
    void a_taken_email_leaves_the_invitation_usable() throws Exception {
        String code = accountInvitation(null);

        HttpResponse<String> taken = signup("admin@example.com", code);

        assertThat(taken.statusCode()).isEqualTo(409);
        assertThat(taken.body()).contains("email_taken");
        assertThat(signup(email(), code).statusCode()).isEqualTo(201);
    }

    @Test
    void an_invitation_creates_one_account_even_used_at_once() throws Exception {
        String code = accountInvitation(null);

        List<CompletableFuture<Integer>> attempts = List.of(1, 2, 3, 4).stream()
                .map(i -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return signup(email(), code).statusCode();
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                })).toList();

        assertThat(attempts.stream().map(CompletableFuture::join).toList())
                .containsExactlyInAnyOrder(201, 404, 404, 404);
    }

    @Test
    void only_administrators_manage_account_invitations() throws Exception {
        String user = owner();
        assertThat(send("POST", "/admin/invitations", "{}", user).statusCode()).isEqualTo(403);
        assertThat(send("GET", "/admin/invitations", null, user).statusCode()).isEqualTo(403);
        assertThat(send("POST", "/admin/invitations", "{}", null).statusCode()).isEqualTo(401);

        HttpResponse<String> created = send("POST", "/admin/invitations", "{}", admin());
        String id = json(created).get("id").asString();
        assertThat(send("GET", "/admin/invitations", null, admin()).body()).contains(id);
        assertThat(send("DELETE", "/admin/invitations/" + id, null, user).statusCode()).isEqualTo(403);
        assertThat(send("DELETE", "/admin/invitations/" + id, null, admin()).statusCode()).isEqualTo(204);

        assertThat(signup(email(), json(created).get("code").asString()).statusCode()).isEqualTo(404);
    }

    @Test
    void an_existing_account_cannot_accept_an_account_invitation() throws Exception {
        String code = accountInvitation(null);
        String user = owner();

        HttpResponse<String> preview = send("POST", "/invitations/preview", "{\"code\":\"%s\"}".formatted(code), user);

        assertThat(preview.statusCode()).isEqualTo(403);
        assertThat(preview.body()).contains("creates a new account");
        assertThat(send("POST", "/invitations/accept", "{\"code\":\"%s\"}".formatted(code), user).statusCode()).isEqualTo(403);
    }

    @Test
    void refused_attempts_lock_the_ip_out() throws Exception {
        String key = "signup-refused-ip:127.0.0.1";
        for (int i = 0; i < 1000; i++) {
            limiter.tryAcquire(key, 1000, Duration.ofHours(1));
        }
        try {
            assertThat(signup(email(), accountInvitation(null)).statusCode()).isEqualTo(429);
        } finally {
            limiter.reset(key);
        }
    }

    @Test
    void refused_reset_links_lock_the_ip_out() throws Exception {
        String key = "password-reset-refused-ip:127.0.0.1";
        for (int i = 0; i < 20; i++) {
            limiter.tryAcquire(key, 20, Duration.ofHours(1));
        }
        try {
            assertThat(send("POST", "/auth/password-reset", "{\"code\":\"kpr_x\",\"password\":\"a-good-password\"}", null).statusCode()).isEqualTo(429);
            assertThat(send("POST", "/auth/password-reset/preview", "{\"code\":\"kpr_x\"}", null).statusCode()).isEqualTo(429);
        } finally {
            limiter.reset(key);
        }
    }

    private String admin() throws Exception {
        HttpResponse<String> login = send("POST", "/auth/login",
                "{\"email\":\"admin@example.com\",\"password\":\"admin-password-123\"}", null);
        assertThat(login.statusCode()).isEqualTo(200);
        return cookieOf(login);
    }

    private String accountInvitation(String email) throws Exception {
        String body = email == null ? "{}" : "{\"email\":\"%s\"}".formatted(email);
        HttpResponse<String> created = send("POST", "/admin/invitations", body, admin());
        assertThat(created.statusCode()).isEqualTo(201);
        return json(created).get("code").asString();
    }

    private String tenantInvitation(String owner, String tenant) throws Exception {
        HttpResponse<String> created = send("POST", "/tenants/" + tenant + "/invitations", "{}", owner);
        assertThat(created.statusCode()).isEqualTo(201);
        return json(created).get("code").asString();
    }

    // an account that may create a tenant
    private String owner() throws Exception {
        HttpResponse<String> created = signup(email(), accountInvitation(null));
        assertThat(created.statusCode()).isEqualTo(201);
        return cookieOf(created);
    }

    private HttpResponse<String> signup(String email, String code) throws Exception {
        return send("POST", "/auth/signup",
                "{\"email\":\"%s\",\"password\":\"a-good-password\",\"invitation\":\"%s\"}".formatted(email, code), null);
    }

    private HttpResponse<String> createTenant(String cookie, String slug) throws Exception {
        return send("POST", "/tenants", "{\"slug\":\"%s\"}".formatted(slug), cookie);
    }

    private static String email() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private static String slug() {
        return "t" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private static JsonNode json(HttpResponse<String> response) {
        return JSON.readTree(response.body());
    }

    private static String cookieOf(HttpResponse<?> response) {
        return response.headers().firstValue("Set-Cookie").orElseThrow().split(";", 2)[0];
    }

    private HttpResponse<String> send(String method, String path, String json, String cookie) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .method(method, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        if (json != null) {
            request.header("Content-Type", "application/json");
        }
        if (cookie != null) {
            request.header("Cookie", cookie);
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
