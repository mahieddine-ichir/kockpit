package org.kockpit.ai.mcp.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test migration Spring Boot 4.1 : verifie que le contexte Spring demarre
 * sans erreur d'auto-configuration (profil Maven par defaut).
 */
@SpringBootTest(properties = {
        "kockpit.aws.region=eu-west-1",
        "aws.service.name=es",
        "aws.region=eu-west-1"
})
class ApplicationSmokeTest {

    @Test
    void contextLoads() {
    }
}
