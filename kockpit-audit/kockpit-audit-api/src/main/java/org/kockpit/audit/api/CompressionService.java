package org.kockpit.audit.api;

public interface CompressionService {

    /**
     * Compresses the input string.
     * @param data the input data to compress
     */
    byte[] compress(byte[] data);
}
