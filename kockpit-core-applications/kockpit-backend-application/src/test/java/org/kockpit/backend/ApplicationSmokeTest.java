package org.kockpit.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test migration Spring Boot 4.1 : verifie que le contexte Spring demarre
 * sans erreur d'auto-configuration (profil Maven par defaut).
 */
@SpringBootTest(properties = {
        "FS_STORAGE_PATH=target/smoke-data",
        "FS_MANIFESTS_PATH=target/smoke-manifests"
})
class ApplicationSmokeTest {

    @Test
    void contextLoads() {
    }
}
