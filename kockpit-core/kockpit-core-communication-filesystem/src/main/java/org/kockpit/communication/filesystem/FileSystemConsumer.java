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

import static org.kockpit.communication.filesystem.FileSystemPublisher.getDirectory;

@RequiredArgsConstructor
@Slf4j
public class FileSystemConsumer implements Consumer {

    private final String localDirectory;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public Message poll(String domain, String env, String appId, String type) {
        File directory = getDirectory(localDirectory, domain, env, appId, type);
        return Files.list(directory.toPath())
                .min((o1, o2) -> Math.toIntExact(o1.toFile().lastModified() - o2.toFile().lastModified()))
                .map(this::read)
                .orElse(null);
    }

    @SneakyThrows
    private Message read(Path path) {
        return objectMapper.readValue(path.toFile(), Message.class);
    }
}
