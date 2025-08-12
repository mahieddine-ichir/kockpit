package com.accor.wcp.services.auditstream.notification.darkcanary.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class DarkCanaryConfigurationTest {

    @Test
    void loadTest_checkDefaultRate() throws Exception {
        DarkCanaryConfiguration configuration = new ObjectMapper().readValue(this.getClass().getResourceAsStream("/darkcanary_testing/samples-config.json"), DarkCanaryConfiguration.class);

        Assertions.assertEquals("x-wcp-origin", configuration.getHeaders().get(0).getKey());
        Assertions.assertEquals(List.of("WCP-CALLER"), configuration.getHeaders().get(0).getValues());
    }

    @Test
    void loadTest_with_definedRate() throws Exception {
        DarkCanaryConfiguration configuration = new ObjectMapper().readValue(this.getClass().getResourceAsStream("/darkcanary_testing/samples-config_rateOf20.json"), DarkCanaryConfiguration.class);

        Assertions.assertEquals(.2d, configuration.getCallRate());

        Assertions.assertEquals("x-wcp-origin", configuration.getHeaders().get(0).getKey());
        Assertions.assertEquals(List.of("WCP-CALLER"), configuration.getHeaders().get(0).getValues());
    }

}