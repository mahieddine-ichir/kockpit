package org.kockpit.features.manifest.services;

import org.kockpit.features.manifest.services.dto.ManifestDto;

import java.util.List;
import java.util.Optional;

public interface ManifestBackendRepository {

    List<ManifestDto> findAll();

    Optional<ManifestDto> findByName(String name);

    ManifestDto save(ManifestDto manifestDto);
}
