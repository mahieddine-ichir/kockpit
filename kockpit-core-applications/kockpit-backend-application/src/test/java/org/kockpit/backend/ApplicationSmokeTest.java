package org.kockpit.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test migration Spring Boot 4.1 : verifie que le contexte Spring demarre
 * sans erreur d'auto-configuration (profil Maven par defaut).
 */
@SpringBootTest(properties = {
        "FS_STORAGE_PATH=target/smoke-data",
        "FS_MANIFESTS_PATH=target/smoke-manifests"
})
class ApplicationSmokeTest {

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
