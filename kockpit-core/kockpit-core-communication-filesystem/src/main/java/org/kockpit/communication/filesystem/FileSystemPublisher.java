package org.kockpit.communication.filesystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

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

    static File getDirectory(String localDirectory, String domain, String env) {
        File directory = Paths.get(localDirectory, domain, env).toFile();
        if (! directory.exists() && !directory.mkdirs()) {
            log.error("Cannot create directory {}", directory.getAbsolutePath());
        }
        return directory;
    }

    static String getMessageAudience(Message message) {
        if (isEmpty(message.getHeaders())) {
            return null;
        } else {
            Object o = message.getHeaders().get("audience");
            return isNull(o) ? null : o.toString() ;
        }
    }
}
