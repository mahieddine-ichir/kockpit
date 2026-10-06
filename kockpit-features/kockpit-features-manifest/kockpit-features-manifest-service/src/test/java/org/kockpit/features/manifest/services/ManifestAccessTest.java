package org.kockpit.features.manifest.services;

import org.junit.jupiter.api.Test;
import org.kockpit.features.manifest.services.dto.ManifestDto;
import org.kockpit.features.manifest.services.dto.ServiceDto;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ManifestAccessTest {

    // Top-level groups only (manifest "example 1").
    private static final ManifestDto GROUPS_ONLY = ManifestReader.read("""
            {
              "domain": "wcxss", "env": "dev",
              "groups": ["ACCOR-WCPConsole-WCXSSTeam"],
              "policies": null,
              "services": [ { "type": "audit", "name": "a", "id": "a" } ]
            }
            """.getBytes(StandardCharsets.UTF_8));

    // Policies only, "type:*" resources and empty permissions (manifest "example 2").
    private static final ManifestDto POLICIES_ONLY = ManifestReader.read("""
            {
              "domain": "wcoff", "env": "review-mr-1551",
              "groups": null,
              "policies": [
                {"groups":["ACCOR-WCPConsole-QaNGATeam","ACCOR-WCPConsole-QaNGATeam-Managed"],"permissions":[],"resources":["audit:*"]},
                {"groups":["ACCOR-WCPConsole-WCOFFTeam"],"permissions":[],"resources":["audit:*","cache:*","dynaconfig:*","featureflipping:*","sqsdlq:*"]}
              ],
              "services": [ { "type": "audit", "name": "offers-api", "id": "offers-api" } ]
            }
            """.getBytes(StandardCharsets.UTF_8));

    private static final ManifestDto UNRESTRICTED = ManifestReader.read("""
            { "domain": "wcc", "env": "dev", "services": [ { "type": "audit", "name": "a", "id": "a" } ] }
            """.getBytes(StandardCharsets.UTF_8));

    @Test
    void a_member_of_the_manifest_groups_sees_everything() {
        Set<String> user = Set.of("ACCOR-WCPConsole-WCXSSTeam");

        assertThat(ManifestAccess.canSee(GROUPS_ONLY, user)).isTrue();
        assertThat(ManifestAccess.visibleTo(GROUPS_ONLY, user).getServices()).hasSize(1);
        assertThat(ManifestAccess.canAccess(GROUPS_ONLY, user, "cache", "anything")).isTrue();
    }

    @Test
    void a_non_member_does_not_see_a_groups_only_manifest() {
        Set<String> user = Set.of("ACCOR-WCPConsole-WBNO-ReadOnly");

        assertThat(ManifestAccess.canSee(GROUPS_ONLY, user)).isFalse();
        assertThat(ManifestAccess.canAccess(GROUPS_ONLY, user, "audit", null)).isFalse();
    }

    @Test
    void policies_grant_by_type_wildcard_even_with_empty_permissions() {
        Set<String> wcoff = Set.of("ACCOR-WCPConsole-WCOFFTeam");

        assertThat(ManifestAccess.canSee(POLICIES_ONLY, wcoff)).isTrue();
        assertThat(ManifestAccess.visibleTo(POLICIES_ONLY, wcoff).getServices())
                .extracting(ServiceDto::getId).containsExactly("offers-api");
        assertThat(ManifestAccess.canAccess(POLICIES_ONLY, wcoff, "cache", "currenciesCache")).isTrue();
        assertThat(ManifestAccess.canAccess(POLICIES_ONLY, wcoff, "audit", null)).isTrue();
    }

    @Test
    void a_policy_only_grants_its_own_resources() {
        Set<String> qa = Set.of("ACCOR-WCPConsole-QaNGATeam-Managed");

        assertThat(ManifestAccess.canSee(POLICIES_ONLY, qa)).isTrue();
        assertThat(ManifestAccess.canAccess(POLICIES_ONLY, qa, "audit", "offers-api")).isTrue();
        assertThat(ManifestAccess.canAccess(POLICIES_ONLY, qa, "cache", "currenciesCache")).isFalse();
        assertThat(ManifestAccess.canAccess(POLICIES_ONLY, qa, "dynaconfig", null)).isFalse();
    }

    @Test
    void a_user_in_no_policy_sees_nothing() {
        Set<String> other = Set.of("ACCOR-WCPConsole-WBNO-ReadOnly");

        assertThat(ManifestAccess.canSee(POLICIES_ONLY, other)).isFalse();
        assertThat(ManifestAccess.visibleTo(POLICIES_ONLY, other).getServices()).isEmpty();
        assertThat(ManifestAccess.canAccess(POLICIES_ONLY, other, "audit", null)).isFalse();
    }

    @Test
    void a_specific_resource_grants_only_that_service() {
        ManifestDto manifest = ManifestReader.read("""
                {
                  "domain": "wcoff", "env": "pro",
                  "policies": [ {"groups":["team"],"permissions":[],"resources":["audit:offers-api"]} ],
                  "services": [ { "type": "audit", "name": "offers-api", "id": "offers-api" },
                                { "type": "audit", "name": "other-api", "id": "other-api" } ]
                }
                """.getBytes(StandardCharsets.UTF_8));
        Set<String> team = Set.of("team");

        assertThat(ManifestAccess.visibleTo(manifest, team).getServices())
                .extracting(ServiceDto::getId).containsExactly("offers-api");
        assertThat(ManifestAccess.canAccess(manifest, team, "audit", "other-api")).isFalse();
        assertThat(ManifestAccess.canAccess(manifest, team, "audit", null)).isTrue();
    }

    @Test
    void legacy_permissions_without_resources_grant_the_whole_type() {
        ManifestDto manifest = ManifestReader.read("""
                {
                  "domain": "wcbno", "env": "dev",
                  "policies": [ { "groups": ["team"], "permissions": ["audit:read"], "resources": [] } ],
                  "services": [ { "type": "audit", "name": "a", "id": "a" } ]
                }
                """.getBytes(StandardCharsets.UTF_8));

        assertThat(ManifestAccess.visibleTo(manifest, Set.of("team")).getServices()).hasSize(1);
        assertThat(ManifestAccess.canAccess(manifest, Set.of("team"), "cache", null)).isFalse();
    }

    @Test
    void a_manifest_without_groups_nor_policies_is_visible_to_everyone() {
        assertThat(ManifestAccess.isRestricted(UNRESTRICTED)).isFalse();
        assertThat(ManifestAccess.canSee(UNRESTRICTED, Set.of())).isTrue();
        assertThat(ManifestAccess.canAccess(UNRESTRICTED, Set.of(), "audit", "a")).isTrue();
    }

    @Test
    void top_level_groups_survive_the_legacy_applications_conversion() {
        ManifestDto legacy = ManifestReader.read("""
                {
                  "domain": "wcoff", "env": "review-mr-1299",
                  "groups": ["ACCOR-WCPConsole-WCOFFTeam"],
                  "applications": [ { "id": "offers-api", "services": { "audit": [ { "name": "offers-api" } ] } } ]
                }
                """.getBytes(StandardCharsets.UTF_8));

        assertThat(legacy.getGroups()).containsExactly("ACCOR-WCPConsole-WCOFFTeam");
        assertThat(ManifestAccess.canSee(legacy, Set.of("ACCOR-WCPConsole-WBNO-ReadOnly"))).isFalse();
        assertThat(ManifestAccess.canSee(legacy, Set.of("ACCOR-WCPConsole-WCOFFTeam"))).isTrue();
    }

    @Test
    void policies_apply_to_the_legacy_applications_format_too() {
        // Legacy format: services grouped by type per application; only audit ones are converted
        // (id = name), the other types are still reachable through the API and checked by type.
        ManifestDto legacy = ManifestReader.read("""
                {
                  "domain": "wcoff", "env": "review-mr-1551",
                  "groups": null,
                  "policies": [
                    {"groups":["ACCOR-WCPConsole-QaNGATeam"],"permissions":[],"resources":["audit:offers-api"]},
                    {"groups":["ACCOR-WCPConsole-WCOFFTeam"],"permissions":[],"resources":["audit:*","cache:*"]}
                  ],
                  "applications": [
                    { "id": "offers-api", "services": {
                        "audit": [ { "name": "offers-api" }, { "name": "offers-business-api" } ],
                        "cache": [ { "name": "currenciesCache" } ] } }
                  ]
                }
                """.getBytes(StandardCharsets.UTF_8));
        Set<String> qa = Set.of("ACCOR-WCPConsole-QaNGATeam");
        Set<String> wcoff = Set.of("ACCOR-WCPConsole-WCOFFTeam");

        assertThat(legacy.getPolicies()).hasSize(2);
        assertThat(ManifestAccess.visibleTo(legacy, qa).getServices())
                .extracting(ServiceDto::getId).containsExactly("offers-api");
        assertThat(ManifestAccess.visibleTo(legacy, wcoff).getServices())
                .extracting(ServiceDto::getId).containsExactly("offers-api", "offers-business-api");
        assertThat(ManifestAccess.canAccess(legacy, qa, "cache", "currenciesCache")).isFalse();
        assertThat(ManifestAccess.canAccess(legacy, wcoff, "cache", "currenciesCache")).isTrue();
        assertThat(ManifestAccess.canSee(legacy, Set.of("ACCOR-WCPConsole-WBNO-ReadOnly"))).isFalse();
    }

    @Test
    void top_level_groups_apply_to_the_current_format_too() {
        ManifestDto current = ManifestReader.read("""
                {
                  "domain": "wcbno", "env": "dev",
                  "groups": ["ACCOR-WCPConsole-WBNO-ReadOnly"],
                  "services": [ { "type": "audit", "name": "wcbno-order-api-v1", "id": "wcbno-order-api-v1" } ]
                }
                """.getBytes(StandardCharsets.UTF_8));

        assertThat(ManifestAccess.canSee(current, Set.of("ACCOR-WCPConsole-WBNO-ReadOnly"))).isTrue();
        assertThat(ManifestAccess.visibleTo(current, Set.of("ACCOR-WCPConsole-WBNO-ReadOnly")).getServices()).hasSize(1);
        assertThat(ManifestAccess.canSee(current, Set.of("ACCOR-WCPConsole-WCOFFTeam"))).isFalse();
    }
}
