package org.kockpit.features.manifest.services.s3;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.features.manifest.services.ManifestBackendRepository;
import org.kockpit.features.manifest.services.ManifestJson;
import org.kockpit.features.manifest.services.dto.ManifestDto;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class S3Repository implements ManifestBackendRepository {

    private final S3Client s3Client;
    private final String bucketName;

    private final ObjectMapper objectMapper = ManifestJson.mapper();

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
