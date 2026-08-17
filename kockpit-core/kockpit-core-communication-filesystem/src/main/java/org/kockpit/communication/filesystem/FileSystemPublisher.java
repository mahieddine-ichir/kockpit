package org.kockpit.communication.filesystem;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
@Slf4j
public class FileSystemPublisher implements Publisher {

    private final String localDirectory;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public void publish(Message message) {
        // check headers (for audience)
        File directory = getDirectory(localDirectory, message.getDomain(), message.getEnv());
        File file = Paths.get(directory.getAbsolutePath(), "%s-%s.json".formatted(message.getAppId(), message.getId())).toFile();
        try (OutputStream os = new FileOutputStream(file)) {
            objectMapper.writeValue(os, message);
        }
    }


    @SneakyThrows
    @Override
    public void cleanup() {
        Path root = Paths.get(localDirectory);
        if (!Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            stream.filter(p -> p.toString().endsWith(".json"))
                    .forEach(file -> {
                        try {
                            Files.delete(file);
                            log.info("Cleaned up stale heartbeat file {}", file);
                        } catch (Exception e) {
                            log.warn("Could not delete stale heartbeat file {}", file, e);
                        }
                    });
        }
    }

    static File getDirectory(String localDirectory, String domain, String env) {
        File directory = Paths.get(localDirectory, domain, env).toFile();
        if (! directory.exists() && !directory.mkdirs()) {
            log.error("Cannot create directory {}", directory.getAbsolutePath());
        }
        return directory;
    }
}
