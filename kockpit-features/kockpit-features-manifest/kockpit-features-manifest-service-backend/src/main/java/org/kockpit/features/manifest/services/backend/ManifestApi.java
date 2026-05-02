package org.kockpit.features.manifest.services.backend;

import lombok.RequiredArgsConstructor;
import org.kockpit.features.manifest.services.dto.ManifestDto;
import org.kockpit.features.manifest.services.dto.PolicyDto;
import org.kockpit.features.manifest.services.dto.ServiceDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("manifests")
@RequiredArgsConstructor
public class ManifestApi {

    private final ManifestBackendService manifestBackendService;

    @GetMapping
    List<ManifestDto> list(Authentication authentication) {
        Set<String> userGroups = resolveGroups(authentication);
        return manifestBackendService.list().stream()
                .map(manifest -> filterServices(manifest, userGroups))
                .toList();
    }

    @GetMapping("{name}")
    ResponseEntity<ManifestDto> byName(String name) {
        return manifestBackendService.get(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    ManifestDto save(@RequestBody ManifestDto manifestDto) {
        return manifestBackendService.save(manifestDto);
    }

    private ManifestDto filterServices(ManifestDto manifest, Set<String> userGroups) {
        List<PolicyDto> policies = manifest.getPolicies();
        if (policies == null || policies.isEmpty()) {
            return manifest;
        }

        List<PolicyDto> matchingPolicies = policies.stream()
                .filter(p -> p.getGroups() != null && p.getGroups().stream().anyMatch(userGroups::contains))
                .toList();

        if (matchingPolicies.isEmpty()) {
            return withServices(manifest, List.of());
        }

        List<ServiceDto> allowed = manifest.getServices() == null ? List.of() :
                manifest.getServices().stream()
                        .filter(service -> isServiceAllowed(service, matchingPolicies))
                        .toList();

        return withServices(manifest, allowed);
    }

    private boolean isServiceAllowed(ServiceDto service, List<PolicyDto> matchingPolicies) {
        String type = service.getType();
        String id = service.getId();
        for (PolicyDto policy : matchingPolicies) {
            if (policy.getPermissions() == null) continue;
            boolean typePermitted = policy.getPermissions().stream()
                    .anyMatch(p -> p.startsWith(type + ":"));
            if (!typePermitted) continue;

            List<String> resources = policy.getResources();
            if (resources == null || resources.isEmpty()) {
                return true;
            }
            if (resources.stream().anyMatch(r -> r.equals(type + ":" + id))) {
                return true;
            }
        }
        return false;
    }

    private ManifestDto withServices(ManifestDto original, List<ServiceDto> services) {
        return ManifestDto.builder()
                .domain(original.getDomain())
                .env(original.getEnv())
                .name(original.getName())
                .services(services)
                .policies(original.getPolicies())
                .build();
    }

    private Set<String> resolveGroups(Authentication authentication) {
        if (authentication == null) {
            return Set.of();
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null) {
            return Set.of();
        }
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .collect(Collectors.toSet());
    }
}
