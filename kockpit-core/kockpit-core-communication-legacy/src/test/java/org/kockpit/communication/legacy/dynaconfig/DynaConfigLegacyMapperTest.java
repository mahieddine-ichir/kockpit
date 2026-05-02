package org.kockpit.communication.legacy.dynaconfig;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kockpit.communication.KeyValue;
import org.kockpit.communication.Message;

import static org.assertj.core.api.Assertions.assertThat;

class DynaConfigLegacyMapperTest {

    DynaConfigLegacyMapper dynaConfigLegacyMapper = new DynaConfigLegacyMapper();

    @Test
    @SneakyThrows
    @DisplayName("On legacy S3 message with __type__ fields, should map to Message with key-values")
    void toMessage_from_legacy_broadcast() {
        String json = """
                {
                  "internalType": "BROADCAST",
                  "messageId": "d12fc6dd-2a7e-4c89-9137-3b3ea5084811",
                  "serviceId": "dynaconfig",
                  "domain": "wcplatform",
                  "env": "dev",
                  "applicationId": "wcpsamples",
                  "message": {
                    "__type__": "com.accor.wcp.sdk.service.dynaconfig.communication.InstanceInitPropertiesUpdateRequestDto",
                    "updates": [
                      {"__type__": "com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageRequestDto", "propertyName": "dyna.property.backend.call.timeout", "newValue": "1"},
                      {"__type__": "com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageRequestDto", "propertyName": "application.client.axa.basepath", "newValue": null},
                      {"__type__": "com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageRequestDto", "propertyName": "application.client.apim.aps.timeout", "newValue": "1012"},
                      {"__type__": "com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageRequestDto", "propertyName": "application.client.wcxssinsurancequotation.timeout", "newValue": null},
                      {"__type__": "com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageRequestDto", "propertyName": "application.client.apim.aps.x-custom-header", "newValue": "custom2"},
                      {"__type__": "com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageRequestDto", "propertyName": "application.wcpsamples.testing.display.for.a.long.property.name.hello-this-is-a-long-property-name-to-test-if-it-is-correctly-displayed", "newValue": null},
                      {"__type__": "com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageRequestDto", "propertyName": "ff.backend.new2022", "newValue": "true"},
                      {"__type__": "com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageRequestDto", "propertyName": "sample.resttemplate.timeout", "newValue": "5001"},
                      {"__type__": "com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageRequestDto", "propertyName": "application.client.axa.timeout", "newValue": "7000"},
                      {"__type__": "com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageRequestDto", "propertyName": "ff.backend.test.nullvalue", "newValue": "default from WCP"}
                    ]
                  }
                }
                """;

        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DynaConfigLegacyMessage legacy = objectMapper.readValue(json, DynaConfigLegacyMessage.class);

        Message message = dynaConfigLegacyMapper.toMessage(legacy);

        assertThat(message.getId()).isEqualTo("d12fc6dd-2a7e-4c89-9137-3b3ea5084811");
        assertThat(message.getService()).isEqualTo("dynaconfig");
        assertThat(message.getType()).isEqualTo("update");
        assertThat(message.getDomain()).isEqualTo("wcplatform");
        assertThat(message.getEnv()).isEqualTo("dev");
        assertThat(message.getAppId()).isEqualTo("wcpsamples");
        assertThat(message.getKeyValues()).containsExactly(
                new KeyValue("dyna.property.backend.call.timeout", "1"),
                new KeyValue("application.client.axa.basepath", null),
                new KeyValue("application.client.apim.aps.timeout", "1012"),
                new KeyValue("application.client.wcxssinsurancequotation.timeout", null),
                new KeyValue("application.client.apim.aps.x-custom-header", "custom2"),
                new KeyValue("application.wcpsamples.testing.display.for.a.long.property.name.hello-this-is-a-long-property-name-to-test-if-it-is-correctly-displayed", null),
                new KeyValue("ff.backend.new2022", "true"),
                new KeyValue("sample.resttemplate.timeout", "5001"),
                new KeyValue("application.client.axa.timeout", "7000"),
                new KeyValue("ff.backend.test.nullvalue", "default from WCP")
        );
    }

    @SneakyThrows
    private String toJson(DynaConfigLegacyMessage mapped) {
        return new ObjectMapper().writeValueAsString(mapped);
    }
}
