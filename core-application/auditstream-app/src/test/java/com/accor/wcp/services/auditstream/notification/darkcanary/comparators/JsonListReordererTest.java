package com.accor.wcp.services.auditstream.notification.darkcanary.comparators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

@ExtendWith(SpringExtension.class)
@Import({JsonListReorderer.class})
@Slf4j
class JsonListReordererTest {

    @Autowired
    JsonListReorderer jsonListReorderer;

    @Test
    @DisplayName("Test on a simple json ordering")
    void orderJson() {
        String json = """
                {
                    "items": [
                    1, 6, 2, 4
                    ]
                }
                """;
        String ordered = jsonListReorderer.order(json);

        JSONAssert.assertNotEquals(ordered, json, true);
        JSONAssert.assertEquals(ordered, json, false);
    }

    @Test
    @DisplayName("Test on a more complex json-format ordering")
    void orderJson_complex() throws JsonProcessingException {
        String json = """
                {
                    "items": [
                        {
                            "type": "json",
                            "code": 3,
                            "any": true
                        },
                        {
                            "type": "bson",
                            "code": 2,
                            "any": false
                        },
                        {
                            "type": "tson",
                            "code": 0,
                            "any": false
                        }
                    ]
                }
                """;
        String ordered = jsonListReorderer.order(json);
        ObjectMapper objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);

        Map<String, Object> map = objectMapper.readValue(ordered, Map.class);

        log.info("order: {}", objectMapper.writeValueAsString(map));

        JSONAssert.assertNotEquals(ordered, json, true);
        JSONAssert.assertEquals(ordered, json, false);
    }

    @Test
    @DisplayName("Test on a even more complex json-format ordering")
    void orderJson_more_complex() throws JsonProcessingException {
        String json = """
                {
                    "items": [
                        {
                            "type": "json",
                            "code": 3,
                            "any": true,
                            "list": [
                                {
                                    "type": "json",
                                    "code": 2
                                },
                                {
                                    "type": "json",
                                    "code": 0
                                }
                            ]
                        },
                        {
                            "type": "json",
                            "code": 3,
                            "any": true,
                            "list": [
                                {
                                    "type": "json",
                                    "code": 2
                                },
                                {
                                    "type": "json",
                                    "code": 0
                                }
                            ]
                        }
                    ]
                }
                """;
        String ordered = jsonListReorderer.order(json);
        ObjectMapper objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);

        Map<String, Object> map = objectMapper.readValue(ordered, Map.class);

        log.info("order: {}", objectMapper.writeValueAsString(map));

        JSONAssert.assertNotEquals(ordered, json, true);
        JSONAssert.assertEquals(ordered, json, false);
    }

}