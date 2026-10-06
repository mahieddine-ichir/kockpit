package org.kockpit.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class Me {

    // groups: what manifest access is evaluated against (the token's custom:adgroups).
    @GetMapping
    Map<String, Object> authenticate(Authentication authentication) {
        if (authentication == null) {
            return Map.of("username", "anonymous", "groups", List.of());
        }
        return Map.of(
                "username", authentication.getName(),
                "groups", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }
}
