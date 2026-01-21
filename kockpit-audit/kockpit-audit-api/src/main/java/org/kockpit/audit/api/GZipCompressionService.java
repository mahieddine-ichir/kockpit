package org.kockpit.audit.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * Service for compressing data using GZIP compression.
 * Provides a centralized compression utility for audit data.
 */
@Slf4j
@RequiredArgsConstructor
public class GZipCompressionService implements CompressionService {

    /**
     * Compresses the input string using GZIP compression if enabled.
     *
     * @param data the string to compress
     * @return compressed byte array if compression is enabled, or uncompressed bytes otherwise
     */
    @Override
    public byte[] compress(byte[] data) {
        long currentTimeMillis = System.currentTimeMillis();
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {

            gzipStream.write(data);
            gzipStream.finish();

            byte[] compressed = byteStream.toByteArray();
            log.trace("Compressed {} bytes to {} bytes ({}% reduction) in {} ms",
                    data.length, compressed.length,
                    (100 - (compressed.length * 100 / data.length)),
                    (System.currentTimeMillis() - currentTimeMillis)
            );

            return compressed;
        } catch (IOException e) {
            log.error("Failed to compress data", e);
            // Fallback to uncompressed data
            return data;
        }
    }
}
