package org.kockpit.features.dynaconfig.services.backend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.features.dynaconfig.service.DynaConfigDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{domain}/{env}/dyna-config/{id}")
@RequiredArgsConstructor
@Slf4j
public class DynaConfigApi {

    private final DynaConfigService dynaConfigService;

    @PutMapping
    ResponseEntity<DynaConfigDto> update(
            @PathVariable String domain,
            @PathVariable String env,
            @PathVariable String id,
            @RequestParam String key,
            @RequestBody DynaConfigDto dynaConfigDto
    ) {
        log.debug("dyna-config {} value?, {} (domain {}, env {})", key, dynaConfigDto.getValue(), domain, env);
        return ResponseEntity.ok(dynaConfigService.update(domain, env, id, dynaConfigDto));
    }
}
