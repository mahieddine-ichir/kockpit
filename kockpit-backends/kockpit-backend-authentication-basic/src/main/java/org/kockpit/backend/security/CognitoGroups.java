package org.kockpit.backend.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The user's groups from a Cognito ID token, read from the configured claims (by default the
 * SAML-mapped "custom:adgroups" attribute plus native "cognito:groups"). A claim may be a list or
 * a single string, which Cognito stores as "A", "A,B" or "[A, B]".
 */
final class CognitoGroups {

    static final List<String> DEFAULT_CLAIMS = List.of("custom:adgroups", "cognito:groups");

    private CognitoGroups() {
    }

    static Set<String> of(Jwt jwt) {
        return of(jwt, DEFAULT_CLAIMS);
    }

    static Set<String> of(Jwt jwt, List<String> claims) {
        Set<String> groups = new LinkedHashSet<>();
        claims.forEach(claim -> addAll(groups, jwt.getClaims().get(claim)));
        return groups;
    }

    /** "custom:adgroups, cognito:groups" -> [custom:adgroups, cognito:groups]; blank -> defaults. */
    static List<String> parseClaims(String configured) {
        if (configured == null || configured.isBlank()) {
            return DEFAULT_CLAIMS;
        }
        return Arrays.stream(configured.split(","))
                .map(String::trim)
                .filter(claim -> !claim.isEmpty())
                .toList();
    }

    private static void addAll(Set<String> groups, Object claim) {
        if (claim instanceof Collection<?> values) {
            values.forEach(value -> addAll(groups, value));
        } else if (claim instanceof String value) {
            String trimmed = value.trim();
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
            }
            Arrays.stream(trimmed.split(","))
                    .map(String::trim)
                    .map(group -> group.replaceAll("^\"|\"$", ""))
                    .filter(group -> !group.isEmpty())
                    .forEach(groups::add);
        }
    }
}
