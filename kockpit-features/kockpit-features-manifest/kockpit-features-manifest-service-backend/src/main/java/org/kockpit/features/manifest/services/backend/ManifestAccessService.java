package org.kockpit.features.manifest.services.backend;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.features.manifest.services.ManifestAccess;
import org.kockpit.features.manifest.services.dto.ManifestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Applies {@link ManifestAccess} to the authenticated user. The user's groups are the request's
 * authorities, mapped from the token's "custom:adgroups" by the security configuration.
 *
 * <p>Access control is on only when Cognito authentication is configured
 * ({@code kockpit.security.cognito.issuer-uri}): without it there is no trusted identity (local
 * runs, basic auth), and everything stays visible as before.
 */
@Component
@Slf4j
public class ManifestAccessService {

    private final ManifestBackendService manifestBackendService;

    private final boolean enabled;

    public ManifestAccessService(
            ManifestBackendService manifestBackendService,
            @Value("${kockpit.security.cognito.issuer-uri:}") String cognitoIssuerUri
    ) {
        this.manifestBackendService = manifestBackendService;
        this.enabled = StringUtils.hasText(cognitoIssuerUri);
        log.info("Manifest access control by user groups: {}", enabled ? "enabled" : "disabled (no Cognito issuer configured)");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<ManifestDto> visibleManifests(Authentication authentication) {
        List<ManifestDto> manifests = manifestBackendService.list();
        if (!enabled) {
            return manifests;
        }
        Set<String> groups = userGroups(authentication);
        return manifests.stream()
                .filter(manifest -> ManifestAccess.canSee(manifest, groups))
                .map(manifest -> ManifestAccess.visibleTo(manifest, groups))
                .toList();
    }

    public Optional<ManifestDto> visibleManifest(String name, Authentication authentication) {
        Optional<ManifestDto> manifest = manifestBackendService.get(name);
        if (!enabled) {
            return manifest;
        }
        Set<String> groups = userGroups(authentication);
        return manifest
                .filter(m -> ManifestAccess.canSee(m, groups))
                .map(m -> ManifestAccess.visibleTo(m, groups));
    }

    /**
     * Whether the user may use the {@code type} service {@code id} (or, with a null id, some
     * service of that type) of the domain/env. No manifest for the domain/env means no access.
     */
    public boolean canAccess(Authentication authentication, String domain, String env, String type, String id) {
        if (!enabled) {
            return true;
        }
        Set<String> groups = userGroups(authentication);
        return manifestBackendService.cachedList().stream()
                .filter(m -> Objects.equals(domain, m.getDomain()) && Objects.equals(env, m.getEnv()))
                .anyMatch(m -> type == null
                        ? ManifestAccess.canSee(m, groups)
                        : ManifestAccess.canAccess(m, groups, type, id));
    }

    static Set<String> userGroups(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getAuthorities() == null) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .collect(Collectors.toSet());
    }
}
