package com.accor.wcp.console.services.me;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

@RestController
class MeUserController {
    @GetMapping(value = "/me", produces = "application/json")
    public ResponseEntity<MeInfo> login(Authentication authentication) {
        if (isNull(authentication)) {
            return ResponseEntity.notFound().build();
        }
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        Map<String, Object> tokenAttributes = jwtAuthenticationToken.getTokenAttributes();
        String email = "" + tokenAttributes.get("email");
        String familyName = "" + tokenAttributes.get("family_name");
        String givenName = "" + tokenAttributes.get("given_name");
        String username = jwtAuthenticationToken.getName();
        // Token
        String tokenValue = jwtAuthenticationToken.getToken().getTokenValue();

        // Roles
        // TODO scopes are in accessToken
        List<String> roles = jwtAuthenticationToken.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(toList());

/*
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String familyName = oAuth2User.getAttribute("family_name");
        String givenName = oAuth2User.getAttribute("given_name");
        String username = oAuth2User.getName();

        // Token
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String tokenValue = oidcUser.getIdToken().getTokenValue();

        // Roles
        List<String> roles = oAuth2User.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(toList());
 */

        return ResponseEntity.ok(MeInfo.builder()
                .username(username)
                .id(username)
                .token(tokenValue)
                .email(email)
                .firstName(givenName)
                .lastName(familyName)
                .roles(roles)
                .build());
    }
}
