package org.kockpit.communication.filesystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;

import java.io.File;
import java.nio.file.Paths;

@RequiredArgsConstructor
@Slf4j
public class FileSystemPublisher implements Publisher {

    private final String localDirectory;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public void publish(Message message) {
        File directory = getDirectory(localDirectory, message.getDomain(), message.getEnv(), message.getAppId(), message.getType());
        File file = Paths.get(directory.getAbsolutePath(), "%s.json".formatted(message.getId())).toFile();
        objectMapper.writeValue(file, message);
    }


    static File getDirectory(String localDirectory, String domain, String env, String appId, String type) {
        File directory = Paths.get(localDirectory, domain, env, appId, type).toFile();
        if (! directory.exists() && !directory.mkdirs()) {
            log.error("Cannot create directory {}", directory.getAbsolutePath());
        }
        return directory;
    }
}
