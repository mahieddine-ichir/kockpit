package org.kockpit.service.backend.featureflipping.filesystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.service.featureflipping.api.FeatureFlippingDto;
import org.kockpit.service.featureflipping.api.FeatureFlippingHistory;
import org.kockpit.service.featureflipping.api.FeatureFlippingService;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class FileSystemFeatureFlippingRepository implements FeatureFlippingService {

    private final String localDirectory;
    private final ObjectMapper objectMapper;

    //private final String HISTORY_FILE = "feature-flipping-history.json";

    /*
    @SneakyThrows
    @Override
    public Object getFeatureFlipping(String domain, String env) {
        return list().stream()
                .flatMap(m -> m.getConfigs().stream())
                .filter(c -> domain.equals(c.getDomain()) && env.equals(c.getEnv()))
                .flatMap(c -> c.getServices().stream())
                .filter(s -> "feature-flipping".equals(s.getName()))
                .findFirst()
                .map(Service::getConfig)
                .orElse(null);
    }
     */

    @SneakyThrows
    @Override
    public FeatureFlippingDto update(String domain, String env, FeatureFlippingDto featureFlippingDto) {
        File directory = getDirectory(domain, env);
        File file = Paths.get(directory.getAbsolutePath(), "%s.json".formatted(featureFlippingDto.getKey())).toFile();
        objectMapper.writeValue(file, featureFlippingDto);
        return featureFlippingDto;
    }

    @SneakyThrows
    @Override
    public List<FeatureFlippingHistory> getHistory(String domain, String env) {
        return List.of();
    }

    @Override
    public List<FeatureFlippingDto> findAll(String domain, String env) {
        File directory = getDirectory(domain, env);
        return Stream.of(Objects.requireNonNull(directory.listFiles()))
                .filter(file -> !file.isDirectory())
                .map(this::readSource)
                .toList();
    }

    @SneakyThrows
    private FeatureFlippingDto readSource(File file) {
        return objectMapper.readValue(file, FeatureFlippingDto.class);
    }

    /*

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
     */

    private File getDirectory(String domain, String env) {
        File directory = Paths.get(localDirectory, domain, env).toFile();
        if (! directory.exists() && !directory.mkdirs()) {
            log.error("Cannot create directory {}", directory.getAbsolutePath());
        }
        return directory;
    }

}
