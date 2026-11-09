package org.kockpit.backend.tenants;

import lombok.RequiredArgsConstructor;
import org.kockpit.backend.security.local.PasswordResetService;
import org.kockpit.backend.tenants.TenantRepository.ApiKey;
import org.kockpit.backend.tenants.TenantRepository.Tenant;
import org.kockpit.features.manifest.services.backend.ManifestAccessService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tenants and API keys of the signed-in local user. Its members see a tenant and its keys; its
 * owner (and administrators) create and revoke keys and manage the members, see {@link MembershipService}.
 * <ul>
 *   <li>{@code GET /tenants}: their tenants, envs and keys with today's usage, plus the limits;</li>
 *   <li>{@code POST /tenants} {@code {slug, envs}}: create a tenant;</li>
 *   <li>{@code POST /tenants/{slug}/api-keys} {@code {name}}: create a key - the only response
 *       that holds the key itself;</li>
 *   <li>{@code DELETE /tenants/{slug}/api-keys/{id}}: revoke a key;</li>
 *   <li>members: {@code GET /tenants/{slug}/members}, {@code DELETE /tenants/{slug}/members/{userId}}
 *       (the owner removes a member, a member leaves);</li>
 *   <li>invitations (owner): {@code POST /tenants/{slug}/invitations} {@code {email?}} - the only
 *       response that holds the code -, {@code GET} the open ones, {@code DELETE .../{id}} to cancel
 *       one; the invited account: {@code POST /invitations/preview} and {@code POST /invitations/accept}
 *       {@code {code}} (the code in the body, never in a URL that access logs would keep);</li>
 *   <li>administrators: {@code GET /admin/tenants} (every tenant, with its owner),
 *       {@code PUT /admin/api-keys/{id}/quota} {@code {dailyQuota}} and the invitations to create an
 *       account: {@code POST /admin/invitations} {@code {email?}} - the only response that holds the
 *       code -, {@code GET} the open ones, {@code DELETE .../{id}} to cancel one; the administrators:
 *       {@code GET /admin/administrators}, {@code POST} {@code {email}}, {@code DELETE .../{userId}};
 *       a password reset link: {@code POST /admin/password-resets} {@code {email}} - the only response
 *       that holds the code.</li>
 * </ul>
 */
@RestController
@ConditionalOnProperty(name = "kockpit.security.local.enabled", havingValue = "true")
@RequiredArgsConstructor
public class TenantApi {

    private final TenantService service;

    private final MembershipService memberships;

    private final AdministratorService administrators;

    private final PasswordResetService passwordResets;

    private final TenantProperties properties;

    private final ManifestAccessService access;

    public record NewTenant(String slug, List<String> envs) {
    }

    public record NewKey(String name) {
    }

    public record Quota(Integer dailyQuota) {
    }

    public record NewInvitation(String email) {
    }

    public record Code(String code) {
    }

    public record NewAdministrator(String email) {
    }

    public record NewPasswordReset(String email) {
    }

    @GetMapping("/tenants")
    Map<String, Object> mine(Authentication authentication) {
        UUID userId = userId(authentication);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("admin", access.isAdmin(authentication));
        body.put("canCreateTenant", service.canCreateTenants(userId, access.isAdmin(authentication)));
        body.put("today", service.today().toString());
        body.put("limits", Map.of(
                "tenants", properties.getMaxPerUser(),
                "keysPerTenant", properties.getMaxKeysPerTenant(),
                "defaultDailyQuota", properties.getDefaultDailyQuota(),
                "envs", properties.getMaxEnvs(),
                "members", properties.getMaxMembers(),
                "openInvitations", properties.getMaxOpenInvitations(),
                "invitationTtlDays", properties.getInvitationTtl().toDays()));
        body.put("tenants", service.tenantsOf(userId).stream().map(t -> mine(t, userId)).toList());
        return body;
    }

    @PostMapping("/tenants")
    ResponseEntity<Map<String, Object>> create(@RequestBody NewTenant request, Authentication authentication) {
        Tenant tenant = service.create(userId(authentication), access.isAdmin(authentication),
                request.slug(), request.envs());
        return ResponseEntity.status(HttpStatus.CREATED).body(mine(tenant, userId(authentication)));
    }

    @PostMapping("/tenants/{slug}/api-keys")
    ResponseEntity<Map<String, Object>> createKey(@PathVariable String slug, @RequestBody NewKey request,
                                                  Authentication authentication) {
        Tenant tenant = memberships.owned(userId(authentication), access.isAdmin(authentication), slug);
        TenantService.CreatedKey created = service.createKey(tenant, request.name());
        Map<String, Object> body = view(created.apiKey());
        body.put("key", created.key());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @DeleteMapping("/tenants/{slug}/api-keys/{id}")
    ResponseEntity<Void> revokeKey(@PathVariable String slug, @PathVariable String id, Authentication authentication) {
        Tenant tenant = memberships.owned(userId(authentication), access.isAdmin(authentication), slug);
        service.revokeKey(tenant, uuid(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tenants/{slug}/members")
    List<Map<String, Object>> members(@PathVariable String slug, Authentication authentication) {
        UUID userId = userId(authentication);
        Tenant tenant = memberships.visible(userId, access.isAdmin(authentication), slug);
        return memberships.members(tenant).stream().map(member -> {
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("userId", member.userId().toString());
            view.put("email", member.email());
            view.put("owner", memberships.isOwner(tenant, member.userId()));
            view.put("you", member.userId().equals(userId));
            view.put("joinedAt", text(member.joinedAt()));
            return view;
        }).toList();
    }

    @DeleteMapping("/tenants/{slug}/members/{memberId}")
    ResponseEntity<Void> removeMember(@PathVariable String slug, @PathVariable String memberId, Authentication authentication) {
        UUID userId = userId(authentication);
        boolean admin = access.isAdmin(authentication);
        Tenant tenant = memberships.visible(userId, admin, slug);
        memberships.removeMember(tenant, userId, admin, uuid(memberId, "member"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tenants/{slug}/invitations")
    ResponseEntity<Map<String, Object>> invite(@PathVariable String slug, @RequestBody(required = false) NewInvitation request,
                                               Authentication authentication) {
        UUID userId = userId(authentication);
        Tenant tenant = memberships.owned(userId, access.isAdmin(authentication), slug);
        MembershipService.CreatedInvitation created = memberships.invite(tenant, userId, request == null ? null : request.email());
        Map<String, Object> body = view(created.invitation());
        body.put("code", created.code());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/tenants/{slug}/invitations")
    List<Map<String, Object>> invitations(@PathVariable String slug, Authentication authentication) {
        Tenant tenant = memberships.owned(userId(authentication), access.isAdmin(authentication), slug);
        return memberships.openInvitations(tenant).stream().map(TenantApi::view).toList();
    }

    @DeleteMapping("/tenants/{slug}/invitations/{id}")
    ResponseEntity<Void> cancelInvitation(@PathVariable String slug, @PathVariable String id, Authentication authentication) {
        Tenant tenant = memberships.owned(userId(authentication), access.isAdmin(authentication), slug);
        memberships.revokeInvitation(tenant, uuid(id, "invitation"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invitations/preview")
    Map<String, Object> preview(@RequestBody Code request, Authentication authentication) {
        MembershipService.Preview preview = memberships.preview(code(request), userId(authentication));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tenant", preview.tenant().slug());
        body.put("invitedBy", preview.invitedBy());
        body.put("expiresAt", text(preview.expiresAt()));
        body.put("alreadyMember", preview.alreadyMember());
        return body;
    }

    @PostMapping("/invitations/accept")
    Map<String, Object> accept(@RequestBody Code request, Authentication authentication) {
        UUID userId = userId(authentication);
        return mine(memberships.accept(code(request), userId), userId);
    }

    @GetMapping("/admin/tenants")
    List<Map<String, Object>> all(Authentication authentication) {
        requireAdmin(authentication);
        return service.all().stream().map(t -> view(t, true)).toList();
    }

    @PutMapping("/admin/api-keys/{id}/quota")
    Map<String, Object> setQuota(@PathVariable String id, @RequestBody Quota request, Authentication authentication) {
        requireAdmin(authentication);
        if (request.dailyQuota() == null) {
            throw TenantException.badRequest("dailyQuota is required");
        }
        return view(service.setQuota(uuid(id), request.dailyQuota()));
    }

    @PostMapping("/admin/invitations")
    ResponseEntity<Map<String, Object>> inviteAccount(@RequestBody(required = false) NewInvitation request, Authentication authentication) {
        requireAdmin(authentication);
        MembershipService.CreatedInvitation created =
                memberships.inviteAccount(userId(authentication), request == null ? null : request.email());
        Map<String, Object> body = view(created.invitation());
        body.put("code", created.code());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/admin/invitations")
    List<Map<String, Object>> accountInvitations(Authentication authentication) {
        requireAdmin(authentication);
        return memberships.openAccountInvitations().stream().map(TenantApi::view).toList();
    }

    @DeleteMapping("/admin/invitations/{id}")
    ResponseEntity<Void> cancelAccountInvitation(@PathVariable String id, Authentication authentication) {
        requireAdmin(authentication);
        memberships.revokeAccountInvitation(uuid(id, "invitation"));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/administrators")
    List<Map<String, Object>> administrators(Authentication authentication) {
        requireAdmin(authentication);
        UUID userId = userId(authentication);
        return administrators.list().stream().map(a -> view(a, userId)).toList();
    }

    @PostMapping("/admin/administrators")
    ResponseEntity<Map<String, Object>> addAdministrator(@RequestBody NewAdministrator request, Authentication authentication) {
        requireAdmin(authentication);
        UUID userId = userId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(view(administrators.add(userId, request.email()), userId));
    }

    @DeleteMapping("/admin/administrators/{id}")
    ResponseEntity<Void> removeAdministrator(@PathVariable String id, Authentication authentication) {
        requireAdmin(authentication);
        administrators.remove(userId(authentication), uuid(id, "administrator"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/password-resets")
    ResponseEntity<Map<String, Object>> createPasswordReset(@RequestBody NewPasswordReset request, Authentication authentication) {
        requireAdmin(authentication);
        if (request.email() == null || request.email().isBlank()) {
            throw TenantException.badRequest("email is required");
        }
        PasswordResetService.Created created = passwordResets.create(request.email(), userId(authentication))
                .orElseThrow(() -> TenantException.notFound("No account for this email"));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", created.email());
        body.put("expiresAt", text(created.expiresAt()));
        body.put("code", created.code());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @ExceptionHandler(TenantException.class)
    ResponseEntity<Map<String, String>> refused(TenantException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getError(), "message", e.getMessage()));
    }

    // a tenant as one of its members sees it: their role decides what the console offers
    private Map<String, Object> mine(Tenant tenant, UUID userId) {
        Map<String, Object> view = view(tenant, false);
        view.put("role", memberships.isOwner(tenant, userId) ? "owner" : "member");
        return view;
    }

    private Map<String, Object> view(Tenant tenant, boolean withOwner) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("slug", tenant.slug());
        view.put("domain", tenant.slug());
        view.put("envs", service.envs(tenant));
        view.put("createdAt", tenant.createdAt().toString());
        if (withOwner) {
            view.put("owner", service.ownerEmail(tenant));
        }
        view.put("apiKeys", service.keys(tenant).stream().map(TenantApi::view).toList());
        return view;
    }

    private static Map<String, Object> view(AdministratorService.Administrator administrator, UUID userId) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("userId", administrator.userId().toString());
        view.put("email", administrator.email());
        view.put("configured", administrator.configured());
        view.put("you", administrator.userId().equals(userId));
        return view;
    }

    private static Map<String, Object> view(ApiKey key) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", key.id().toString());
        view.put("name", key.name());
        view.put("prefix", key.prefix());
        view.put("dailyQuota", key.dailyQuota());
        view.put("usedToday", key.usedToday());
        view.put("active", key.active());
        view.put("createdAt", text(key.createdAt()));
        view.put("lastUsedAt", text(key.lastUsedAt()));
        view.put("revokedAt", text(key.revokedAt()));
        return view;
    }

    private static Map<String, Object> view(MembershipRepository.Invitation invitation) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", invitation.id().toString());
        view.put("email", invitation.email());
        view.put("createdAt", text(invitation.createdAt()));
        view.put("expiresAt", text(invitation.expiresAt()));
        return view;
    }

    private static String code(Code request) {
        if (request == null || request.code() == null || request.code().isBlank()) {
            throw TenantException.badRequest("code is required");
        }
        return request.code().trim();
    }

    private static String text(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    private void requireAdmin(Authentication authentication) {
        if (!access.isAdmin(authentication)) {
            throw TenantException.forbidden("Administrators only");
        }
    }

    private static UUID uuid(String id) {
        return uuid(id, "key");
    }

    private static UUID uuid(String id, String what) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw TenantException.notFound("No %s %s".formatted(what, id));
        }
    }

    // local accounts' tokens: subject = user id (LocalAuthConfiguration)
    private static UUID userId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            try {
                return UUID.fromString(jwt.getSubject());
            } catch (IllegalArgumentException e) {
                // fall through
            }
        }
        throw new InsufficientAuthenticationException("A local account is required");
    }
}
