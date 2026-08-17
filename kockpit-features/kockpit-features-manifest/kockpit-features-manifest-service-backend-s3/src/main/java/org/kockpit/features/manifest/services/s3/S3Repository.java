package org.kockpit.features.manifest.services.s3;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.features.manifest.services.ManifestBackendRepository;
import org.kockpit.features.manifest.services.dto.ManifestDto;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class S3Repository implements ManifestBackendRepository {

    private final S3Client s3Client;
    private final String bucketName;

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT)
            // Jackson 3 trie les proprietes alphabetiquement par defaut ; on conserve l'ordre
            // de declaration (defaut Jackson 2) pour ne pas changer le JSON produit.
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .build();

    @SneakyThrows
    @Override
    public List<ManifestDto> findAll() {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        return s3Client.listObjectsV2(listRequest)
                .contents()
                .stream()
                //.filter(s3Object -> s3Object.key().contains("manifest"))
                .map(s3Object -> {
                    log.trace("Reading S3 object {}", s3Object.key());

                    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Object.key())
                            .build();

                    ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

                    ManifestDto manifestDto = read(objectBytes.asByteArray());
                    manifestDto.setName(s3Object.key());
                    return manifestDto;
                })
                .toList();
    }

    @Override
    public Optional<ManifestDto> findByName(String name) {
        return Optional.empty();
    }

    @SneakyThrows
    @Override
    public ManifestDto save(ManifestDto manifestDto) {
        String key = manifestDto.getName();
        if (key == null) {
            key = "%s-%s-%d.json".formatted(
                    manifestDto.getDomain(),
                    manifestDto.getEnv(),
                    Instant.now().getEpochSecond()
            );
        }

        byte[] jsonBytes = objectMapper.writeValueAsBytes(manifestDto);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/json")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(jsonBytes));
        manifestDto.setName(key);
        return manifestDto;
    }

    ManifestDto read(byte[] content) {
        try {
            TypeReference<ManifestDto> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(content, typeRef);
        } catch (Exception e) {
            log.error("Error reading config from S3", e);
            return new ManifestDto();
        }
    }
}
