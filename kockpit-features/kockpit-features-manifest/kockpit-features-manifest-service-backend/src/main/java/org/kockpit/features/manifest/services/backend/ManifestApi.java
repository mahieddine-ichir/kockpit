package org.kockpit.features.manifest.services.backend;

import lombok.RequiredArgsConstructor;
import org.kockpit.features.manifest.services.dto.ManifestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("manifests")
@RequiredArgsConstructor
public class ManifestApi {

    private final ManifestBackendService manifestBackendService;

    @GetMapping
    List<ManifestDto> list() {
        return manifestBackendService.list();
    }

    @GetMapping("{name}")
    ResponseEntity<ManifestDto> byName(String name) {
        return manifestBackendService.get(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    ManifestDto save(@RequestBody ManifestDto manifestDto) {
        return manifestBackendService.save(manifestDto);
    }
}
