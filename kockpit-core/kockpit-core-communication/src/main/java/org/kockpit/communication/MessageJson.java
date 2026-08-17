package org.kockpit.communication;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Contrat de serialisation JSON des {@link Message} echanges entre services Kockpit.
 *
 * <p>Ce mapper est deliberement independant du bean {@code ObjectMapper} auto-configure par
 * Spring Boot : le format de ces messages ne doit pas dependre de la configuration
 * {@code spring.jackson.*} de l'application hote, sous peine de rompre la communication entre
 * deux services Kockpit configures differemment.
 *
 * <p>Les publishers et consumers filesystem, S3 et storage account partagent ce mapper : ils
 * ecrivent et relisent les memes fichiers ou objets. Toute evolution du format se fait ici,
 * en un seul endroit.
 */
public final class MessageJson {

    /**
     * Les deux {@code FAIL_ON_*} sont deja desactives par defaut en Jackson 3 ; ils restent
     * explicites parce que la tolerance aux proprietes inconnues est un choix, pas un hasard :
     * un service ancien doit pouvoir relire un message produit par un service plus recent.
     *
     * <p>{@code SORT_PROPERTIES_ALPHABETICALLY} passe a {@code true} par defaut en Jackson 3 :
     * on conserve l'ordre de declaration pour ne pas changer le JSON deja ecrit sur disque,
     * dans S3 ou dans un blob.
     */
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .build();

    private MessageJson() {
    }

    /**
     * Mapper partage. En Jackson 3 un {@code ObjectMapper} est immuable et thread-safe :
     * une seule instance suffit pour tout le processus.
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }
}
