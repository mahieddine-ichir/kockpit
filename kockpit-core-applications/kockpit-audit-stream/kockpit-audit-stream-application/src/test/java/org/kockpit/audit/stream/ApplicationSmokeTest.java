package org.kockpit.audit.stream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test migration Spring Boot 4.1 : verifie que le contexte Spring demarre
 * sans erreur d'auto-configuration (profil Maven par defaut).
 */
@SpringBootTest(properties = {
        "OPENSEARCH_ENDPOINTS=http://localhost:9200",
        "kockpit.audit.trace.enabled=true"
})
class ApplicationSmokeTest {

    @Test
    void contextLoads() {
    }
}
