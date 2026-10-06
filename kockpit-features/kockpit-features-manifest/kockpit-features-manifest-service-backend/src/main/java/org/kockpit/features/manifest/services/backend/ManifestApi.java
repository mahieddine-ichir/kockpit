package org.kockpit.features.manifest.services.backend;

import lombok.RequiredArgsConstructor;
import org.kockpit.features.manifest.services.dto.ManifestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("manifests")
@RequiredArgsConstructor
public class ManifestApi {

    private final ManifestBackendService manifestBackendService;

    private final ManifestAccessService manifestAccessService;

    /** The manifests (and, within them, the services) the connected user has access to. */
    @GetMapping
    List<ManifestDto> list(Authentication authentication) {
        return manifestAccessService.visibleManifests(authentication);
    }

    // A manifest the user may not see answers 404 like a missing one, so names aren't disclosed.
    @GetMapping("{name}")
    ResponseEntity<ManifestDto> byName(@PathVariable String name, Authentication authentication) {
        return manifestAccessService.visibleManifest(name, authentication)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    ManifestDto save(@RequestBody ManifestDto manifestDto) {
        return manifestBackendService.save(manifestDto);
    }
}
