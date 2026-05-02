package org.kockpit.features.manifest.services.backend;

import lombok.RequiredArgsConstructor;
import org.kockpit.features.manifest.services.ManifestBackendRepository;
import org.kockpit.features.manifest.services.dto.ManifestDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ManifestBackendService {

    private final ManifestBackendRepository manifestBackendRepository;

    public List<ManifestDto> list() {
        return manifestBackendRepository.findAll();
    }

    Optional<ManifestDto> get(String name) {
        return manifestBackendRepository.findByName(name);
    }

    ManifestDto save(ManifestDto manifestDto) {
        return manifestBackendRepository.save(manifestDto);
    }
}
