package org.kockpit.backend.services.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigApi {

    private final ConfigApiService configApiService;

    @GetMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<List<Manifest>> getManifests() {
        return ResponseEntity.ok(configApiService.getConfig());
    }

    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<ConfigItem> createManifest(@RequestBody ConfigItem configItem) {
        ConfigItem created = configApiService.save(configItem);
        /* fixme
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
         return ResponseEntity.created(location);
         */
        return ResponseEntity.ok(created);
    }
}
