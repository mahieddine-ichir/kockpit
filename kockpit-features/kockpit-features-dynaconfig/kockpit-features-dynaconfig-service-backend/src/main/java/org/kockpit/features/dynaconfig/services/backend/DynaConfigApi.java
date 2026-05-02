package org.kockpit.features.dynaconfig.services.backend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.features.dynaconfig.service.DynaConfigDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{domain}/{env}/dyna-config/{id}")
@RequiredArgsConstructor
@Slf4j
public class DynaConfigApi {

    private final DynaConfigService dynaConfigService;

    @GetMapping
    ResponseEntity<List<DynaConfigDto>> get(
            @PathVariable String domain,
            @PathVariable String env,
            @PathVariable String id
    ) {
        return ResponseEntity.ok(dynaConfigService.get(domain, env, id));
    }

    @PostMapping("_sync")
    ResponseEntity<Void> sync(
            @PathVariable String domain,
            @PathVariable String env,
            @PathVariable String id
    ) {
        dynaConfigService.republish();
        return ResponseEntity.ok().build();
    }

    @PutMapping
    ResponseEntity<DynaConfigDto> update(
            @PathVariable String domain,
            @PathVariable String env,
            @PathVariable String id,
            @RequestParam String key,
            @RequestBody DynaConfigDto dynaConfigDto
    ) {
        log.trace("Update dyna-config key {}, value {} (domain {}, env {}, appId {})", key, dynaConfigDto.getValue(), domain, env, id);
        return ResponseEntity.ok(dynaConfigService.update(domain, env, id, dynaConfigDto));
    }
}
