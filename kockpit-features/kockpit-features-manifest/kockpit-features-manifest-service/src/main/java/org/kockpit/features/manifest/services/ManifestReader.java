package org.kockpit.features.manifest.services;

import org.kockpit.features.manifest.services.dto.ManifestDto;
import org.kockpit.features.manifest.services.dto.ServiceDto;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Point d'entree unique pour parser un manifest, quel que soit le backend (filesystem, S3,
 * storage account).
 *
 * <p>En plus du format courant ({@code services} a plat), certains manifests historiques
 * ("legacy") groupent les services par application sous une cle {@code applications} :
 * {@code applications[].services} est alors une map indexee par type de service plutot qu'une
 * liste de {@link ServiceDto}. Pour l'instant seuls les services de type {@code audit} de ce
 * format legacy sont convertis vers le format courant ; les autres types (cache, dynaconfig,
 * sqsdlq, ...) sont ignores faute de consommateur.
 *
 * <p>Le {@code config} legacy d'un service audit ({@code resultColumns}/{@code searchMetadatas})
 * est retraduit vers le schema courant ({@code columns}/{@code search_columns}, cf.
 * {@code terraform/modules/kockpit-manifest}) : c'est ce schema que consomme AuditListPage cote
 * front (kockpit-console), pas les noms de champs legacy.
 */
public final class ManifestReader {

    private static final String LEGACY_APPLICATIONS_FIELD = "applications";
    private static final String LEGACY_SERVICES_FIELD = "services";
    private static final String LEGACY_AUDIT_TYPE = "audit";
    private static final String LEGACY_BUILTIN_KV_SUBTYPE = "builtin.kv";

    // AuditListPage (kockpit-console) special-cases a handful of column keys to render a status
    // badge, a method badge and a truncated/copyable path (col.key === 'status'/'method'/'path')
    // instead of the generic cell - the legacy resultColumns names carry the same data under
    // different keys. Renamed for display only: search_columns keeps the real field names below,
    // since those still have to match what's actually indexed.
    private static final Map<String, String> LEGACY_COLUMN_KEY_ALIASES = Map.of(
            "httpStatus", "status",
            "httpMethod", "method",
            "requestUri", "path"
    );

    private ManifestReader() {
    }

    public static ManifestDto read(byte[] content) {
        ObjectMapper mapper = ManifestJson.mapper();
        return toManifestDto(mapper, mapper.readTree(content));
    }

    public static ManifestDto read(InputStream content) {
        ObjectMapper mapper = ManifestJson.mapper();
        return toManifestDto(mapper, mapper.readTree(content));
    }

    private static ManifestDto toManifestDto(ObjectMapper mapper, JsonNode root) {
        ManifestDto manifestDto = mapper.convertValue(root, ManifestDto.class);

        boolean noServices = manifestDto.getServices() == null || manifestDto.getServices().isEmpty();
        if (noServices && root.has(LEGACY_APPLICATIONS_FIELD)) {
            manifestDto.setServices(convertLegacyApplications(root.get(LEGACY_APPLICATIONS_FIELD)));
        }

        return manifestDto;
    }

    private static List<ServiceDto> convertLegacyApplications(JsonNode applications) {
        List<ServiceDto> services = new ArrayList<>();

        for (JsonNode application : applications) {
            JsonNode auditServices = application.path(LEGACY_SERVICES_FIELD).path(LEGACY_AUDIT_TYPE);
            for (JsonNode auditService : auditServices) {
                String name = auditService.path("name").asText(null);

                ServiceDto serviceDto = new ServiceDto();
                serviceDto.setType(LEGACY_AUDIT_TYPE);
                serviceDto.setName(name);
                serviceDto.setId(name);
                serviceDto.setConfig(toAuditConfig(auditService));

                services.add(serviceDto);
            }
        }

        return services;
    }

    private static Map<String, Object> toAuditConfig(JsonNode auditService) {
        List<String> columns = new ArrayList<>();
        for (JsonNode resultColumn : auditService.path("resultColumns")) {
            String name = resultColumn.path("name").asText(null);
            if (name != null) {
                columns.add(LEGACY_COLUMN_KEY_ALIASES.getOrDefault(name, name));
            }
        }

        List<Map<String, Object>> searchColumns = new ArrayList<>();
        for (JsonNode searchMetadata : auditService.path("searchMetadatas")) {
            String name = searchMetadata.path("name").asText(null);
            if (name == null) {
                continue;
            }

            boolean indexedKeyValue = LEGACY_BUILTIN_KV_SUBTYPE.equals(searchMetadata.path("subtype").asText(null));

            Map<String, Object> searchColumn = new LinkedHashMap<>();
            searchColumn.put("name", name);
            searchColumn.put("type", toSearchColumnType(searchMetadata.path("type").asText(null)));
            searchColumn.put("path", indexedKeyValue ? "indexedKeyValues." + name : name);
            searchColumns.add(searchColumn);
        }

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("columns", columns);
        config.put("search_columns", searchColumns);
        return config;
    }

    private static String toSearchColumnType(String legacyType) {
        if (legacyType == null) {
            return "text";
        }
        return switch (legacyType.toLowerCase(Locale.ROOT)) {
            case "date" -> "date";
            case "integer", "number" -> "number";
            default -> "text";
        };
    }
}
