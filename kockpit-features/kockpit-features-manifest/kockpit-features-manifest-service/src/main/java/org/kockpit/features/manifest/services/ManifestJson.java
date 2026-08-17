package org.kockpit.features.manifest.services;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Contrat de serialisation des fichiers manifest, ecrits puis relus par les depots
 * filesystem, S3 et storage account.
 *
 * <p>Ces fichiers sont des artefacts persistes, souvent lus a l'oeil : le format doit rester
 * stable et lisible independamment de la configuration {@code spring.jackson.*} de
 * l'application hote. C'est pourquoi ce mapper est dedie et non injecte.
 */
public final class ManifestJson {

    /**
     * {@code WRITE_DATES_AS_TIMESTAMPS} est desactive pour ecrire des dates ISO-8601 lisibles.
     * Le reglage est explicite bien que ce soit desormais le defaut de Jackson 3, parce que
     * c'etait l'inverse en Jackson 2 : le rendre implicite masquerait un choix de format.
     *
     * <p>{@code SORT_PROPERTIES_ALPHABETICALLY} est desactive parce que Jackson 3 l'active par
     * defaut, ce qui reordonnerait les champs des manifests deja stockes.
     */
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .build();

    private ManifestJson() {
    }

    /**
     * Mapper partage. En Jackson 3 un {@code ObjectMapper} est immuable et thread-safe :
     * une seule instance suffit pour tout le processus.
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }
}
