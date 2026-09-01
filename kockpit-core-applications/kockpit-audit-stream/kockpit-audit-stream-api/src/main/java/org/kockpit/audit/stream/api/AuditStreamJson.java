package org.kockpit.audit.stream.api;

import org.kockpit.audit.stream.api.model.AuditReport;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

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

    public static AuditReport readAuditReport(byte[] data) {
        return mapper().readValue(data, AuditReport.class);
    }

    public static byte[] read(byte[] data) throws IOException {
        if (isRecordCompressed(data)) {
            return new GZIPInputStream(new ByteArrayInputStream(data)).readAllBytes();
        } else {
            return data;
        }
    }

    /**
     * todo check if this method is necessary compared to the one above
     * @param compressedData
     * @return
     * @throws IOException
     */
    public static String decompress(byte[] compressedData) throws IOException {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipStream = new GZIPInputStream(byteStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            byte[] decompressed = outputStream.toByteArray();
            return new String(decompressed, StandardCharsets.UTF_8);
        }
    }

    /**
     * todo check difference with
     * if (message.length >= 2 && message[0] == (byte) 0x1f && message[1] == (byte) 0x8b)
     */
    public static boolean isRecordCompressed(byte[] data) throws IOException {
        return (data.length >= 2 && data[0] == (byte) (GZIPInputStream.GZIP_MAGIC))
                && (data[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }
}
