package com.accor.wcp.services.auditstream.notification.darkcanary.model;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
class DarkCanaryIndexDocumentTest {

    @Test
    @DisplayName("documents with the same ID should be equal")
    void documentsWithSameIdShouldBeEqual() {

        // doc 1
        DarkCanaryIndexDocument darkCanaryIndexDocument1 = new DarkCanaryIndexDocument();
        {
            var id = List.of(
                    KeyValuePair.builder()
                            .key("traceId")
                            .value("anyTraceIdValue")
                            .build(),
                    KeyValuePair.builder()
                            .key("spanId")
                            .value("anySpanIdValue")
                            .build()
            );
            darkCanaryIndexDocument1.setId(id);
        }
        // doc 2 (doc 2.id == doc 1.id)
        DarkCanaryIndexDocument darkCanaryIndexDocument2 = new DarkCanaryIndexDocument();
        {
            var id = List.of(
                    KeyValuePair.builder()
                            .key("traceId")
                            .value("anyTraceIdValue")
                            .build(),
                    KeyValuePair.builder()
                            .key("spanId")
                            .value("anySpanIdValue")
                            .build()
            );
            darkCanaryIndexDocument2.setId(id);
        }

        // doc3 ( != doc 1.id)
        DarkCanaryIndexDocument darkCanaryIndexDocument3 = new DarkCanaryIndexDocument();
        {
            var id = List.of(
                    KeyValuePair.builder()
                            .key("traceId")
                            .value("anyTraceIdValue")
                            .build(),
                    KeyValuePair.builder()
                            .key("spanId")
                            .value("anotherSpanIdValue")
                            .build()
            );
            darkCanaryIndexDocument3.setId(id);
        }

        Map<List<KeyValuePair>, List<DarkCanaryIndexDocument>> collect = Stream.of(darkCanaryIndexDocument1, darkCanaryIndexDocument2, darkCanaryIndexDocument3)
                .collect(Collectors.groupingBy(DarkCanaryIndexDocument::getId));
        log.info(collect.toString());

        Assertions.assertThat(collect.size()).isEqualTo(2);
        Assertions.assertThat(collect.get(darkCanaryIndexDocument1.getId()).size()).isEqualTo(2);
        Assertions.assertThat(collect.get(darkCanaryIndexDocument3.getId()).size()).isEqualTo(1);

        Assertions.assertThat(collect.get(darkCanaryIndexDocument1.getId())).isEqualTo(collect.get(darkCanaryIndexDocument2.getId()));
        Assertions.assertThat(collect.get(darkCanaryIndexDocument1.getId())).isNotEqualTo(collect.get(darkCanaryIndexDocument3.getId()));
    }
}