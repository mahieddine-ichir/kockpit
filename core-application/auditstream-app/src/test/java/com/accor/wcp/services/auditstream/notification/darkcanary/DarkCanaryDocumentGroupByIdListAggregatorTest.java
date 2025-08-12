package com.accor.wcp.services.auditstream.notification.darkcanary;

import com.accor.wcp.services.auditstream.notification.darkcanary.comparators.JSONAssertComparator;
import com.accor.wcp.services.auditstream.notification.darkcanary.comparators.JSONPathComparator;
import com.accor.wcp.services.auditstream.notification.darkcanary.comparators.JsonListReorderer;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryIdConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryWatch;
import com.accor.wcp.services.auditstream.notification.darkcanary.fromaudit.DarkCanaryDocumentGroupByIdListAggregator;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.ConfiguredDarkCanaryIndexDocument;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.DarkCanaryIndexDocument;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.KeyValuePair;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.PropertyDifference;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@Import({DarkCanaryDocumentGroupByIdListAggregator.class,
        JSONPathComparator.class, JSONAssertComparator.class, JsonListReorderer.class})
@Slf4j
class DarkCanaryDocumentGroupByIdListAggregatorTest {

    @Autowired
    private DarkCanaryDocumentGroupByIdListAggregator darkCanaryDocumentGroupByIdListAggregator;

    @Test
    @DisplayName("Merge should detect difference on watch")
    void testGroupId() {

        List<KeyValuePair> id = List.of(
                KeyValuePair.builder().key("traceId").value("1234").build(),
                KeyValuePair.builder().key("spanId").value("4321").build()
        );

        List<ConfiguredDarkCanaryIndexDocument> list = new ArrayList<>();
        list.add(of(id, """
                {
                    "firstName": "Mehdi",
                    "phoneNumber": {
                        "number": 1234
                    },
                    "version": 1
                }
                """));
        list.add(of(id, """
                {
                    "firstName": "Mahieddine",
                    "phoneNumber": {
                        "number": 234
                    }
                }
                """));

        Map.Entry<List<KeyValuePair>, List<ConfiguredDarkCanaryIndexDocument>> entry = Map.entry(id, list);

        ConfiguredDarkCanaryIndexDocument configuredDarkCanaryIndexDocument = darkCanaryDocumentGroupByIdListAggregator.apply(entry);
        DarkCanaryIndexDocument merged = configuredDarkCanaryIndexDocument.darkCanaryIndexDocument();

        log.info("Merged diff {}", merged.getDifferences());

        Assertions.assertEquals(id, merged.getId());
        Assertions.assertEquals(2, merged.getDifferences().size());

        PropertyDifference propertyDifference = merged.getDifferences().get(0);
        Assertions.assertEquals("$.firstName", propertyDifference.getPropertyName());
        Assertions.assertEquals("Mehdi", propertyDifference.getLeftValue());
        Assertions.assertEquals("Mahieddine", propertyDifference.getRightValue());
    }

    private static ConfiguredDarkCanaryIndexDocument of(List<KeyValuePair> id, String responseV1) {
        DarkCanaryIndexDocument darkCanaryIndexDocument = new DarkCanaryIndexDocument();
        darkCanaryIndexDocument.setId(id);
        darkCanaryIndexDocument.setAppId("wcpsamples");
        darkCanaryIndexDocument.setDomain("wcplatform");
        darkCanaryIndexDocument.setResponseLeft(responseV1);

        DarkCanaryConfiguration canaryConfiguration = DarkCanaryConfiguration.builder()
                .domain("wcplatform")
                .appId("wcpsamples")
                .id(List.of(
                        DarkCanaryIdConfiguration.builder()
                                .key("X-B3-TraceId")
                                .type("header")
                                .build()
                ))
                .includes(List.of(
                        DarkCanaryWatch.builder().left("$.firstName").right("$.firstName").build(),
                        DarkCanaryWatch.builder().left("$.phoneNumber.number").build()
                )).build();

        return new ConfiguredDarkCanaryIndexDocument(darkCanaryIndexDocument, canaryConfiguration);
    }
}