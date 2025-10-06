package org.kockpit.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class Me {

    @GetMapping
    Map<String, Object> authenticate(Principal principal) {
        return Map.of("username", principal.getName());
    }
}
