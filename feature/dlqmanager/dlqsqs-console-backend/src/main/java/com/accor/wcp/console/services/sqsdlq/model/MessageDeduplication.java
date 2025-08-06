package com.accor.wcp.console.services.sqsdlq.model;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.accor.wcp.console.services.sqsdlq.model.MessageDeduplicationStrategy.MD5_ON_PAYLOAD;
import static java.util.Objects.isNull;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

public interface MessageDeduplication {

  default String constructMessageDeduplicationId(String body, String dedupIdGenerationStrategy) {
    if (isNull(dedupIdGenerationStrategy)) return null;

    byte[] bytesBody = body.getBytes(StandardCharsets.UTF_8);
    return MD5_ON_PAYLOAD.name().equals(dedupIdGenerationStrategy)
        ? sha256Hex(bytesBody)
        : UUID.randomUUID().toString();
  }

}
