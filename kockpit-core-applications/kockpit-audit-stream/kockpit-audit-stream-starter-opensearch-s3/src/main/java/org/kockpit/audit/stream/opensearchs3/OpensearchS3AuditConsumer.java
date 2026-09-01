package org.kockpit.audit.stream.opensearchs3;

import lombok.RequiredArgsConstructor;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.api.AuditStreamJson;
import org.kockpit.audit.stream.api.model.AuditReport;
import org.kockpit.audit.stream.opensearch.IndexMetadata;
import org.kockpit.audit.stream.opensearch.OpensearchIndexer;
import org.kockpit.audit.stream.s3.S3AuditConsumer;
import org.kockpit.audit.stream.s3.S3WriteEvent;
import org.springframework.context.event.EventListener;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OpensearchS3AuditConsumer implements AuditConsumer {

    private final S3AuditConsumer s3AuditConsumer;

    private final OpensearchIndexer opensearchIndexer;

    private final ObjectMapper objectMapper;

    @Override
    public void accept(List<byte[]> bytes) {
        s3AuditConsumer.accept(bytes);
    }

    @EventListener
    public void onS3Write(S3WriteEvent s3WriteEvent) {
        s3WriteEvent.getBatch().stream().map(s3Record -> {
            // s3Record.getData() is the archived wire-format bytes, possibly gzip-compressed
            // (see AuditStreamJson) - decompress before parsing, and readValue (not convertValue)
            // to actually parse it as JSON rather than round-tripping the raw bytes through
            // Jackson's default byte[]-as-base64-string handling.
            AuditReport auditReport;
            try {
                auditReport = objectMapper.readValue(AuditStreamJson.read(s3Record.getData()), AuditReport.class);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            auditReport.setS3Key(s3WriteEvent.getS3Key());
            auditReport.setS3Uri("s3://%s/%s".formatted(s3WriteEvent.getS3BucketName(), s3WriteEvent.getS3Key()));
            auditReport.setS3Size(s3Record.getLength());
            auditReport.setS3Offset(s3Record.getOffset());

            auditReport.setAudits(null);

            return auditReport;
        }).collect(Collectors.groupingBy(IndexMetadata::of))
                .forEach((indexMetadata, auditReports) ->
                        opensearchIndexer.index(auditReports, indexMetadata)
                );
    }
}
