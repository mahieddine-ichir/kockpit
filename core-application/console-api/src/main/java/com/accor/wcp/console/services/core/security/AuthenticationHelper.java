package com.accor.wcp.console.services.core.security;

import com.accor.wcp.console.services.core.servicemanager.ServiceManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

public class AuthenticationHelper {

    public static List<String> getAuthenticatedUserGroups() {
        // Get authentication data
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        // Get user groups
        JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) authentication;
        return readGroups(authenticationToken);
    }

    public static boolean isUserGroupsAuthorizedFor(ServiceManager serviceManager, List<String> userGroups, String domain, String env) {
        Collection<String> authorizedGroupsForDomainAndEnv = serviceManager.getAuthorizedGroupsForDomainAndEnv(domain, env);

        // By default => permitted
        if (isNull(authorizedGroupsForDomainAndEnv) || authorizedGroupsForDomainAndEnv.isEmpty()) {
            return true;
        }

        return authorizedGroupsForDomainAndEnv.stream()
                .anyMatch(userGroups::contains);
    }

    private static List<String> readGroups(JwtAuthenticationToken authenticationToken) {
        Object customGroupsObj = authenticationToken.getToken().getClaims().get("custom:adgroups");
        if (isNull(customGroupsObj)) {
            return emptyList();
        }
        List<String> groups = emptyList();
        if (customGroupsObj instanceof List) {
            groups = (List<String>) customGroupsObj;
        } else if (customGroupsObj instanceof String) {
            groups = computeCustomAdGroupsStringToList(customGroupsObj.toString());
        }

        return groups;
    }

    static List<String> computeCustomAdGroupsStringToList(String customGroupsString) {
        // String example: "[AWS-359195335135-wcc_user, AWS-531583874639-platform-dev-MCO, AWS-359195335135-wcc-dev-MCO, AWS-428415080805-xss-dev-Admin]"
        return Arrays.stream(customGroupsString.replace("[", "").replace("]", "").split(",")).map(String::trim).toList();
    }

}
