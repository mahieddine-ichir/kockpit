package org.kockpit.communication.filesystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.Message;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.kockpit.communication.filesystem.FileSystemPublisher.getDirectory;

@RequiredArgsConstructor
@Slf4j
public class FileSystemConsumer implements Consumer {

    private final String localDirectory;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public List<Message> poll(String domain, String env, String appId, String type) {
        File directory = getDirectory(localDirectory, domain, env, appId, type);
        return Files.list(directory.toPath())
                .sorted((o1, o2) -> Math.toIntExact(o1.toFile().lastModified() - o2.toFile().lastModified()))
                .map(this::read)
                .filter(Objects::nonNull)
                .toList();
    }

    private Message read(Path path) {
        try {
            return objectMapper.readValue(path.toFile(), Message.class);
        } catch (Exception e) {
            log.error("error reading path {}", path, e);
            return null;
        }
    }
}
