package org.kockpit.features.manifest.services;

import org.kockpit.features.manifest.services.dto.ManifestDto;
import org.kockpit.features.manifest.services.dto.ServiceDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * One entry per domain/env, merging the services of every manifest declaring it - what the
 * console's domain/env selector and menus need, instead of one entry per manifest file.
 *
 * <p>Apply access filtering ({@link ManifestAccess#visibleTo}) to each manifest <em>before</em>
 * aggregating: groups/policies belong to a manifest and govern its own services only, so the
 * aggregate carries none. Entries keep the order in which their domain/env first appears.
 */
public final class ManifestAggregator {

    private ManifestAggregator() {
    }

    public static List<ManifestDto> byDomainAndEnv(List<ManifestDto> manifests) {
        Map<List<String>, List<ManifestDto>> groups = new LinkedHashMap<>();
        for (ManifestDto manifest : manifests) {
            groups.computeIfAbsent(List.of(Objects.toString(manifest.getDomain(), ""), Objects.toString(manifest.getEnv(), "")),
                    key -> new ArrayList<>()).add(manifest);
        }
        return groups.values().stream().map(ManifestAggregator::merge).toList();
    }

    private static ManifestDto merge(List<ManifestDto> manifests) {
        ManifestDto first = manifests.getFirst();
        if (manifests.size() == 1) {
            return ManifestDto.builder()
                    .domain(first.getDomain())
                    .env(first.getEnv())
                    .name(first.getName())
                    .services(first.getServices() == null ? List.of() : first.getServices())
                    .build();
        }

        // The same service declared by two manifests appears once (first declaration wins).
        Set<String> seen = new LinkedHashSet<>();
        List<ServiceDto> services = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (ManifestDto manifest : manifests) {
            if (manifest.getName() != null) {
                names.add(manifest.getName());
            }
            if (manifest.getServices() == null) {
                continue;
            }
            for (ServiceDto service : manifest.getServices()) {
                if (seen.add(service.getType() + ":" + service.getId())) {
                    services.add(service);
                }
            }
        }
        return ManifestDto.builder()
                .domain(first.getDomain())
                .env(first.getEnv())
                .name(String.join(", ", names))
                .services(services)
                .build();
    }
}
