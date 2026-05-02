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
     * Permission entries in the form "serviceType:action" where action is "*" or "read".
     * Example: "audit:*", "audit:read"
     */
    private List<String> permissions;

    /**
     * Resource entries in the form "serviceType:serviceId".
     * Empty list means the permission applies to all resources of the matched type.
     * Example: "audit:wcpsamples"
     */
    private List<String> resources;
}
