package org.kockpit.audit.stream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test migration Spring Boot 4.1 : verifie que le contexte Spring demarre
 * sans erreur d'auto-configuration (profil Maven par defaut ou aws).
 *
 * <p>Proprietes sans valeur par defaut, requises seulement sous le profil Maven {@code aws}
 * (beans supplementaires apportes par kockpit-aws-opensearch-request-signing et
 * kockpit-audit-notification-kinesis) ; en production elles viennent des variables
 * d'environnement injectees par la tache ECS (KOCKPIT_AWS_REGION, KINESIS_STREAM_NAME,
 * KINESIS_APP_NAME) :
 * <ul>
 *   <li>{@code kockpit.aws.region} : region OpenSearch/Kinesis</li>
 *   <li>{@code KINESIS_STREAM_NAME} : nom du stream Kinesis (audit + notification)</li>
 *   <li>{@code KINESIS_APP_NAME} : nom d'application KCL (table de lease DynamoDB)</li>
 * </ul>
 */
@SpringBootTest(properties = {
        "OPENSEARCH_ENDPOINTS=http://localhost:9200",
        "kockpit.audit.trace.enabled=true",
        "kockpit.aws.region=eu-west-1",
        "KINESIS_STREAM_NAME=smoke-test-stream",
        "KINESIS_APP_NAME=smoke-test-app"
})
class ApplicationSmokeTest {

    @Test
    void contextLoads() {
    }
}
