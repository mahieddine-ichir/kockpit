package org.kockpit.features.manifest.services.backend;

import lombok.RequiredArgsConstructor;
import org.kockpit.features.manifest.services.ManifestBackendRepository;
import org.kockpit.features.manifest.services.dto.ManifestDto;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ManifestBackendService {

    // Access checks run on every API request; the S3 repository re-reads every manifest object on
    // each findAll(), so they use a short-lived copy instead. Short enough that a manifest change
    // (new group/policy) applies within seconds; save() drops it immediately.
    private static final Duration CACHE_TTL = Duration.ofSeconds(30);

    private final ManifestBackendRepository manifestBackendRepository;

    private volatile CachedList cached;

    public List<ManifestDto> list() {
        return manifestBackendRepository.findAll();
    }

    List<ManifestDto> cachedList() {
        CachedList current = cached;
        long now = System.nanoTime();
        if (current == null || now - current.loadedAt() > CACHE_TTL.toNanos()) {
            current = new CachedList(manifestBackendRepository.findAll(), now);
            cached = current;
        }
        return current.manifests();
    }

    Optional<ManifestDto> get(String name) {
        return manifestBackendRepository.findByName(name);
    }

    ManifestDto save(ManifestDto manifestDto) {
        ManifestDto saved = manifestBackendRepository.save(manifestDto);
        cached = null;
        return saved;
    }

    private record CachedList(List<ManifestDto> manifests, long loadedAt) {
    }
}
