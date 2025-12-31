package org.kockpit.audit.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

/**
 * Service for compressing data using GZIP compression.
 * Provides a centralized compression utility for audit data.
 */
@Slf4j
@RequiredArgsConstructor
public class CompressionService {

    private final boolean compressionEnabled;

    /**
     * Compresses the input string using GZIP compression if enabled.
     *
     * @param data the string to compress
     * @return compressed byte array if compression is enabled, or uncompressed bytes otherwise
     */
    public byte[] compress(String data) {
        if (!compressionEnabled) {
            log.debug("Compression disabled, returning uncompressed data");
            return data.getBytes(StandardCharsets.UTF_8);
        }

        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {

            gzipStream.write(data.getBytes(StandardCharsets.UTF_8));
            gzipStream.finish();

            byte[] compressed = byteStream.toByteArray();
            log.debug("Compressed {} bytes to {} bytes ({}% reduction)",
                    data.length(), compressed.length,
                    (100 - (compressed.length * 100 / data.length())));

            return compressed;
        } catch (IOException e) {
            log.error("Failed to compress data", e);
            // Fallback to uncompressed data
            return data.getBytes(StandardCharsets.UTF_8);
        }
    }
}
