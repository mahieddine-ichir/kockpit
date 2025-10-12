package org.kockpit.backend.services.storage.filesystem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.backend.services.storage.*;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class FileSystemRepository implements ConfigApiService {

    private final String localFilePath;
    private final String HISTORY_FILE = "feature-flipping-history.json";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @SneakyThrows
    @Override
    public List<Manifest> getConfig() {
        log.debug("loading config from {}", localFilePath);
        return Files.list(Path.of(localFilePath))
                .map(this::read)
                .filter(Objects::nonNull)
                .toList();
    }

    @SneakyThrows
    @Override
    public ConfigItem save(ConfigItem configItem) {
        objectMapper.writeValue(new File(localFilePath, UUID.randomUUID().toString()), configItem);
        return configItem;
    }

    private Manifest read(Path path) {
        try {
            TypeReference<List<ConfigItem>> typeRef = new TypeReference<>() {};
            List<ConfigItem> configItems = objectMapper.readValue(new FileInputStream(path.toFile()), typeRef);
            Manifest manifest = new Manifest();
            manifest.setConfigs(configItems);
            manifest.setName(path.toFile().getName());
            return manifest;
        } catch (Exception e) {
            log.error("error reading config from {}", localFilePath, e);
            return null;
        }
    }

    @SneakyThrows
    @Override
    public Object getFeatureFlipping(String domain, String env) {
        return getConfig().stream()
                .flatMap(m -> m.getConfigs().stream())
                .filter(c -> domain.equals(c.getDomain()) && env.equals(c.getEnv()))
                .flatMap(c -> c.getServices().stream())
                .filter(s -> "feature-flipping".equals(s.getName()))
                .findFirst()
                .map(Service::getConfig)
                .orElse(null);
    }

    @SneakyThrows
    @Override
    public Object updateFeatureFlag(String domain, String env, String key, Object value) {
        List<Manifest> manifests = getConfig();
        Object updatedFlag = null;
        Object oldValue = null;

        for (Manifest manifest : manifests) {
            for (ConfigItem configItem : manifest.getConfigs()) {
                if (domain.equals(configItem.getDomain()) && env.equals(configItem.getEnv())) {
                    for (Service service : configItem.getServices()) {
                        if ("feature-flipping".equals(service.getName())) {
                            ObjectMapper mapper = new ObjectMapper();
                            var configMap = mapper.convertValue(service.getConfig(), Map.class);
                            oldValue = configMap.get(key);
                            break;
                        }
                    }
                }
            }
        }

        for (Manifest manifest : manifests) {
            for (ConfigItem configItem : manifest.getConfigs()) {
                if (domain.equals(configItem.getDomain()) && env.equals(configItem.getEnv())) {
                    for (Service service : configItem.getServices()) {
                        if ("feature-flipping".equals(service.getName())) {
                            ObjectMapper mapper = new ObjectMapper();
                            var configMap = mapper.convertValue(service.getConfig(), Map.class);
                            configMap.put(key, value);
                            service.setConfig(configMap);
                            File target = new File(localFilePath, manifest.getName());
                            mapper.writerWithDefaultPrettyPrinter().writeValue(target, manifest.getConfigs());
                            updatedFlag = configMap;
                            break;
                        }
                    }
                }
            }
        }

        if (updatedFlag != null) {
            logFeatureHistory(key, domain, env, "Updated flag", oldValue, value);
        }

        return updatedFlag;
    }


    @SneakyThrows
    @Override
    public List<FeatureFlippingHistory> getHistory(String domain, String env) {
        return loadHistory();
    }


    @SneakyThrows
    private void logFeatureHistory(String key, String domain, String env, String action, Object oldValue, Object newValue) {
        List<FeatureFlippingHistory> history = loadHistory();
        FeatureFlippingHistory entry = new FeatureFlippingHistory();
        entry.setKey(key);

        String actionMessage;
        if (oldValue == null) {
            actionMessage = String.format("Created flag with value: %s", newValue);
        } else if (newValue == null) {
            actionMessage = String.format("Deleted flag (was: %s)", oldValue);
        } else {
            actionMessage = String.format("Updated flag: %s → %s", oldValue, newValue);
        }

        entry.setAction(actionMessage);
        entry.setUser("use");
        entry.setTimestamp(LocalDateTime.now());
        history.add(entry);
        saveHistory(history);
    }

    @SneakyThrows
    private void saveHistory(List<FeatureFlippingHistory> history) {
        Path tempPath = Paths.get(localFilePath, HISTORY_FILE + ".tmp");
        Path targetPath = Paths.get(localFilePath, HISTORY_FILE);
        Files.createDirectories(tempPath.getParent());

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempPath.toFile(), history);
        Files.move(tempPath, targetPath,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);
    }

    @SneakyThrows
    private List<FeatureFlippingHistory> loadHistory() {
        File historyFile = new File(localFilePath, HISTORY_FILE);
        if (!historyFile.exists()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(historyFile,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, FeatureFlippingHistory.class));
        } catch (Exception e) {
            log.error("corrupted history .creating fresh one", e);
            File backupFile = new File(localFilePath, HISTORY_FILE + ".corrupted." + System.currentTimeMillis());
            Files.copy(historyFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return new ArrayList<>();
        }
    }

}
