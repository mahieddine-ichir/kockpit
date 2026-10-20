package org.kockpit.features.manifest.services.s3;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.features.manifest.services.ManifestBackendRepository;
import org.kockpit.features.manifest.services.ManifestJson;
import org.kockpit.features.manifest.services.ManifestReader;
import org.kockpit.features.manifest.services.dto.ManifestDto;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

@Slf4j
@RequiredArgsConstructor
public class S3Repository implements ManifestBackendRepository {

    private final S3Client s3Client;
    private final String bucketName;

    private final ObjectMapper objectMapper = ManifestJson.mapper();

    // One GetObject per manifest: read one after another, ~90 manifests took ~2.6s. Read
    // concurrently instead - bounded below the S3 client's default pool of 50 connections.
    private static final int READ_CONCURRENCY = 16;

    @SneakyThrows
    @Override
    public List<ManifestDto> findAll() {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        List<String> keys = s3Client.listObjectsV2(listRequest)
                .contents()
                .stream()
                //.filter(s3Object -> s3Object.key().contains("manifest"))
                .map(S3Object::key)
                .toList();

        Semaphore permits = new Semaphore(READ_CONCURRENCY);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ManifestDto>> manifests = keys.stream()
                    .map(key -> executor.submit(() -> {
                        permits.acquire();
                        try {
                            return readObject(key);
                        } finally {
                            permits.release();
                        }
                    }))
                    .toList();
            List<ManifestDto> result = new ArrayList<>(manifests.size());
            for (Future<ManifestDto> manifest : manifests) {
                try {
                    result.add(manifest.get());
                } catch (ExecutionException e) {
                    // same failure as the sequential read: the S3 exception itself
                    throw e.getCause();
                }
            }
            return result;
        }
    }

    private ManifestDto readObject(String key) {
        log.trace("Reading S3 object {}", key);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

        ManifestDto manifestDto = read(objectBytes.asByteArray());
        manifestDto.setName(key);
        return manifestDto;
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
            return ManifestReader.read(content);
        } catch (Exception e) {
            log.error("Error reading config from S3", e);
            return new ManifestDto();
        }
    }
}
