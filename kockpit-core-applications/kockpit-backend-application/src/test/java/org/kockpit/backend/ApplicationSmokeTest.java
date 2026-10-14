package org.kockpit.backend;

import org.junit.jupiter.api.Test;
import org.kockpit.features.manifest.services.backend.ManifestBackendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test migration Spring Boot 4.1 : verifie que le contexte Spring demarre
 * sans erreur d'auto-configuration (profil Maven par defaut, azure ou aws).
 *
 * <p>Proprietes sans valeur par defaut, requises seulement sous les profils Maven
 * {@code azure} (compte de developpement Azurite) et {@code aws}. Le service de manifests
 * est mocke : l'application liste les manifests au demarrage, ce qui sous ces profils
 * appellerait le stockage (Blob, S3).
 */
@SpringBootTest(properties = {
        "FS_STORAGE_PATH=target/smoke-data",
        "FS_MANIFESTS_PATH=target/smoke-manifests",
        "STORAGE_ACCOUNT=devstoreaccount1",
        "STORAGE_KEY=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==",
        "MANIFESTS_STORAGE_ACCOUNT=devstoreaccount1",
        "MANIFESTS_STORAGE_KEY=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==",
        "kockpit.aws.s3.bucket=smoke-test-bucket",
        "kockpit.manifests.aws.region=eu-west-1",
        "kockpit.aws.region=eu-west-1",
        "kockpit.audit.notification.kinesis.stream_name=smoke-test"
})
class ApplicationSmokeTest {

    @MockitoBean
    ManifestBackendService manifestBackendService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    /**
     * OpensearchAutoConfiguration expose un bean "opensearch-objectMapper". Depuis le passage
     * a Jackson 3 il a le meme type que le mapper auto-configure par Boot : ce test verifie
     * qu'une injection par type resout bien celui de Boot (expose en @Primary) et non celui
     * du module OpenSearch, qui n'a ni les customizers ni la configuration spring.jackson.*.
     */
    @Test
    void boot_object_mapper_wins_type_injection() {
        assertThat(objectMapper).isInstanceOf(JsonMapper.class);
    }
}
