package org.kockpit.audit.stream.s3;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class S3Record {
    private final byte[] data;
    private final S3Key s3Key;
    private long offset;
    private long length;
}
