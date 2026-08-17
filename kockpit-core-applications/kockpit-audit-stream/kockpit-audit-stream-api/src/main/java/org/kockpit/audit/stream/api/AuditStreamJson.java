package org.kockpit.audit.stream.api;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Contrat de lecture du flux audit, cote consommateur.
 *
 * <p>Le flux transporte les {@code Instant} en secondes.nanosecondes
 * ({@code "start":1750197974.047081776}), format produit par le SDK d'audit
 * ({@code NotificationAuditReportManager}). En Jackson 3
 * {@code DateTimeFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS} est actif par defaut : rien a
 * regler ici, mais c'est bien ce defaut qui rend la lecture correcte. Un mapper qui
 * l'interpreterait en millisecondes produirait des dates en 1970.
 *
 * <p>Les starters Kafka et Kinesis partagent ce mapper. Le {@code SerdesTest} du starter
 * Kafka l'utilise directement, de sorte qu'il verifie la configuration reellement en
 * production et non une copie de celle-ci ; le starter Kinesis n'a pas de test propre.
 */
public final class AuditStreamJson {

    /**
     * Tolerance aux proprietes inconnues et ignorees : un consommateur doit pouvoir lire un
     * rapport produit par une version plus recente du SDK. Les deux reglages sont deja les
     * defauts de Jackson 3 ; ils restent explicites parce que cette tolerance est le coeur du
     * contrat de compatibilite du flux.
     */
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
            .build();

    private AuditStreamJson() {
    }

    /**
     * Mapper partage. En Jackson 3 un {@code ObjectMapper} est immuable et thread-safe :
     * une seule instance suffit pour tout le processus.
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }
}
