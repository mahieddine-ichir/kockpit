package org.kockpit.kinesis.s3;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test migration Spring Boot 4.1 : verifie que le contexte Spring demarre
 * sans erreur d'auto-configuration (profil Maven par defaut).
 */
@SpringBootTest(properties = {
        "KINESIS_STREAM_NAMES=smoke-stream",
        "S3_BUCKET=smoke-bucket"
})
class ApplicationSmokeTest {

    @Test
    void contextLoads() {
    }
}
