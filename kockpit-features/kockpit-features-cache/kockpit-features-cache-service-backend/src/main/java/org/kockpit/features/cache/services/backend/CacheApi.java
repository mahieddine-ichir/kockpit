package org.kockpit.features.cache.services.backend;

import lombok.RequiredArgsConstructor;
import org.kockpit.features.cache.service.CacheStatsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/{domain}/{env}/cache/{id}")
@RequiredArgsConstructor
public class CacheApi {

    private final CacheService cacheService;

    @GetMapping
    ResponseEntity<List<CacheStatsDto>> get(
            @PathVariable String domain,
            @PathVariable String env,
            @PathVariable String id
    ) {
        return ResponseEntity.ok(cacheService.get(domain, env, id));
    }

    @PostMapping("_clear")
    ResponseEntity<Void> clear(
            @PathVariable String domain,
            @PathVariable String env,
            @PathVariable String id
    ) {
        cacheService.clearCache(domain, env, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("_reset-stats")
    ResponseEntity<Void> resetStats(
            @PathVariable String domain,
            @PathVariable String env,
            @PathVariable String id
    ) {
        cacheService.resetStats(domain, env, id);
        return ResponseEntity.ok().build();
    }
}
