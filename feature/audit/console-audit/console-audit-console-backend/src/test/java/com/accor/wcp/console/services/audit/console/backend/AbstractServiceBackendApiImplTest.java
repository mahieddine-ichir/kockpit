package com.accor.wcp.console.services.audit.console.backend;

import static org.mockito.Mockito.when;

import com.accor.wcp.console.sdk.notification.WCPConsoleUserNotificationService;
import com.accor.wcp.console.services.audit.kengine.KEngineRegistryDocumentRepository;
import com.accor.wcp.console.services.audit.kengine.KEngineRegistryWriteRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.sns.SnsClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {ServiceAuditConsoleBackendItApplication.class})
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
abstract class AbstractServiceBackendApiImplTest {

  @LocalServerPort protected int port;

  @Autowired protected TestRestTemplate testRestTemplate;
  @Autowired protected MockMvc mockMvc;

  @MockBean protected KEngineRegistryWriteRepository kEngineRegistryWriteRepository;

  @MockBean protected SnsClient snsClient;

  @MockBean protected CloudWatchClient cloudWatchClient;

  @MockBean protected WCPConsoleUserNotificationService wcpConsoleUserNotificationService;

  @MockBean protected KEngineRegistryDocumentRepository kEngineRegistryDocumentRepository;

  @MockBean protected RestHighLevelClient restHighLevelClient;

  @Autowired protected AuditServiceActivator auditServiceActivator;

  @BeforeEach
  void init() throws IOException {
    GetMetricDataResponse getMetricDataResponse = GetMetricDataResponse.builder().build();
    when(cloudWatchClient.getMetricData(ArgumentMatchers.any(GetMetricDataRequest.class)))
        .thenReturn(getMetricDataResponse);

    // Init auditServiceActivator

    HashMap<String, Object> auditSettings =
        new ObjectMapper()
            .readValue(
                getClass().getResourceAsStream("/audit-settings.json"),
                new TypeReference<HashMap<String, Object>>() {});

    List<Map<String, Object>> auditManifestSettings = new ArrayList<>();
    auditManifestSettings.add(auditSettings);
    AppManifestMock testItApplicationManifest = AppManifestMock.builder().build();
    testItApplicationManifest
        .getServiceDataMap()
        .put(auditServiceActivator.getServiceId(), auditManifestSettings);
    auditServiceActivator.load(List.of(testItApplicationManifest));
  }
}
