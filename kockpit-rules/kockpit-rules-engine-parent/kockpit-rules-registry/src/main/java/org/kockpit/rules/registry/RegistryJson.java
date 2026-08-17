package org.kockpit.rules.registry;

import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Contrat de serialisation utilise pour calculer le hash d'identite d'un registre de regles.
 *
 * <p>Le JSON produit n'est jamais transmis ni stocke : il sert uniquement de base au
 * {@code hashCode()} qui identifie un registre. C'est pourquoi l'ordre des proprietes compte
 * ici plus qu'ailleurs — deux registres aux regles identiques doivent produire le meme hash,
 * y compris entre deux versions de Jackson.
 *
 * <p>Partage par {@code RuleNodeRegistry} et {@code SeamLessRegistry}, qui calculaient le meme
 * hash avec deux mappers construits separement.
 */
public final class RegistryJson {

    /**
     * {@code SORT_PROPERTIES_ALPHABETICALLY} passe a {@code true} par defaut en Jackson 3 :
     * le desactiver conserve l'ordre de declaration, donc les hash deja attribues aux
     * registres existants.
     */
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .build();

    private RegistryJson() {
    }

    /**
     * Mapper partage. En Jackson 3 un {@code ObjectMapper} est immuable et thread-safe :
     * une seule instance suffit pour tout le processus.
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }
}
