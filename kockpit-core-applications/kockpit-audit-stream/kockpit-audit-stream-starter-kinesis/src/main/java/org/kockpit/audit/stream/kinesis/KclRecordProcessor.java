package org.kockpit.audit.stream.kinesis;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

@Slf4j
@RequiredArgsConstructor
public class KclRecordProcessor {

    private final ApplicationEventPublisher applicationEventPublisher;

    @SneakyThrows
    String read(byte[] message) {
        // Check if the data is GZIP compressed by checking the magic number
        // GZIP files start with 0x1f 0x8b
        if (message.length >= 2 && message[0] == (byte) 0x1f && message[1] == (byte) 0x8b) {
            log.debug("Detected GZIP compressed data, decompressing...");
            return decompress(message);
        } else {
            log.debug("Data is not compressed, converting directly to string");
            return new String(message, StandardCharsets.UTF_8);
        }
    }

    private String decompress(byte[] compressedData) throws IOException {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipStream = new GZIPInputStream(byteStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            byte[] decompressed = outputStream.toByteArray();
            log.debug("Decompressed {} bytes to {} bytes", compressedData.length, decompressed.length);

            return new String(decompressed, StandardCharsets.UTF_8);
        }
    }
}
