package org.kockpit.features.manifest.services.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PolicyDto {

    private List<String> groups;

    /**
     * Legacy grant form: "serviceType:action" (e.g. "audit:read"); with empty {@link #resources}
     * it grants every service of that type. Current manifests leave it empty and use resources.
     */
    private List<String> permissions;

    /**
     * Granted services: "serviceType:serviceId" (e.g. "audit:offers-api"), "serviceType:*" for
     * every service of a type (e.g. "audit:*", "cache:*"), or "*" for everything.
     * See {@link org.kockpit.features.manifest.services.ManifestAccess}.
     */
    private List<String> resources;
}
