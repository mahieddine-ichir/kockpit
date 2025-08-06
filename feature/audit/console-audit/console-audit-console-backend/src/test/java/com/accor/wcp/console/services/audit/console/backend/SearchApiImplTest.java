// package com.accor.wcp.console.services.audit.console.backend;
//
// import static
// com.accor.wcp.console.services.audit.console.backend.AuditDataHelperTest.loadEmptySearchResponse;
// import static
// com.accor.wcp.console.services.audit.console.backend.AuditDataHelperTest.loadSearchResponse;
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;
//
// import com.accor.wcp.console.services.audit.console.backend.search.dto.AuditReport;
// import com.accor.wcp.console.services.audit.console.backend.search.dto.SearchQuery;
// import com.accor.wcp.console.services.audit.kengine.dynamodb.KEngineRegistryDocument;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.Collection;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;
// import org.elasticsearch.action.search.SearchRequest;
// import org.elasticsearch.action.search.SearchResponse;
// import org.elasticsearch.client.RequestOptions;
// import org.junit.jupiter.api.Test;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
//
// class SearchApiImplTest extends AbstractServiceBackendApiImplTest {
//
//  static final String KENGINE_REFERENTIAL_JSON_VALUE =
//
// "{\"name\":null,\"id\":0,\"ruleSpecifications\":[{\"id\":\"EnrichUser\",\"name\":\"EnrichUser\",\"details\":{\"code\":\"EnrichUser\",\"name\":\"EnrichUser\",\"description\":\"No Doc\"},\"ok\":null,\"ko\":null,\"lastly\":{\"id\":\"ROOT_lastly\",\"name\":\"ROOT_lastly\",\"details\":{\"code\":\"ROOT_lastly\",\"name\":\"ROOT_lastly\",\"description\":\"lastly\"},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_ADVANCED_NOTIFY\",\"name\":\"ACT_ADVANCED_NOTIFY\",\"description\":\"Launch subprocess of notification\"}],\"predicates\":[]},\"actions\":[{\"code\":\"ACT_ADVANCED_ENRICH_USER\",\"name\":\"ACT_ADVANCED_ENRICH_USER\",\"description\":\"Create an enriched user\"}],\"predicates\":[]},{\"id\":\"LoadCompany\",\"name\":\"LoadCompany\",\"details\":{\"code\":\"LoadCompany\",\"name\":\"LoadCompany\",\"description\":\"No Doc\"},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_ADVANCED_LOAD_COMPANY\",\"name\":\"ACT_ADVANCED_LOAD_COMPANY\",\"description\":\"Load a company\"}],\"predicates\":[]},{\"id\":\"LoadUser\",\"name\":\"LoadUser\",\"details\":{\"code\":\"LoadUser\",\"name\":\"LoadUser\",\"description\":\"No Doc\"},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_ADVANCED_LOAD_USER\",\"name\":\"ACT_ADVANCED_LOAD_USER\",\"description\":\"Load given user\"}],\"predicates\":[]},{\"id\":\"userNotificationRule\",\"name\":\"userNotificationRule\",\"details\":{\"code\":\"userNotificationRule\",\"name\":\"userNotificationRule\",\"description\":\"No Doc\"},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_NOTIFY_USER\",\"name\":\"ACT_NOTIFY_USER\",\"description\":\"Notify user\"}],\"predicates\":[]},{\"id\":\"hello1\",\"name\":\"hello1\",\"details\":{\"code\":\"hello1\",\"name\":\"hello1\",\"description\":\"No Doc\"},\"ok\":{\"id\":\"ROOT_then\",\"name\":\"ROOT_then\",\"details\":{\"code\":\"ROOT_then\",\"name\":\"ROOT_then\",\"description\":\"_then\"},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_HELLO_NAME\",\"name\":\"ACT_HELLO_NAME\",\"description\":\"Compute greetings for given name\"}],\"predicates\":[]},\"ko\":{\"id\":\"ROOT_otherwise\",\"name\":\"ROOT_otherwise\",\"details\":{\"code\":\"ROOT_otherwise\",\"name\":\"ROOT_otherwise\",\"description\":\"_otherwise\"},\"ok\":{\"id\":\"ROOT_otherwise_then\",\"name\":\"ROOT_otherwise_then\",\"details\":{\"code\":\"ROOT_otherwise_then\",\"name\":\"ROOT_otherwise_then\",\"description\":\"_then\"},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_HELLO_NAME\",\"name\":\"ACT_HELLO_NAME\",\"description\":\"Compute greetings for given name\"}],\"predicates\":[]},\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"com.accor.wcp.sample.kengine.hello.HelloRule.HelloDefault\",\"name\":\"com.accor.wcp.sample.kengine.hello.HelloRule.HelloDefault\",\"description\":\"Compute default greetings without name\"}],\"predicates\":[{\"code\":\"PRE_HAS_NAME\",\"name\":\"PRE_HAS_NAME\",\"description\":\"Has a name in inputs ?\"}]},\"lastly\":{\"id\":\"ROOT_lastly\",\"name\":\"ROOT_lastly\",\"details\":{\"code\":\"ROOT_lastly\",\"name\":\"ROOT_lastly\",\"description\":\"lastly\"},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"com.accor.wcp.sample.kengine.hello.HelloRule.RandomHello\",\"name\":\"com.accor.wcp.sample.kengine.hello.HelloRule.RandomHello\",\"description\":\"Compute random greetings\"}],\"predicates\":[]},\"actions\":[],\"predicates\":[{\"code\":\"PRE_HAS_NAME\",\"name\":\"PRE_HAS_NAME\",\"description\":\"Has a name in inputs ?\"}]},{\"id\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2\",\"name\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2\",\"details\":{\"code\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2\",\"name\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2\",\"description\":\"Another random rule!\"},\"ok\":{\"id\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_ok\",\"name\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_ok\",\"details\":{\"code\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_ok\",\"name\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_ok\",\"description\":null},\"ok\":{\"id\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_ok_ok\",\"name\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_ok_ok\",\"details\":{\"code\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_ok_ok\",\"name\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_ok_ok\",\"description\":null},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_SIMPLE_ACTION\",\"name\":\"ACT_SIMPLE_ACTION\",\"description\":\"A simple action saying hello\"}],\"predicates\":[]},\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_SIMPLE_ACTION\",\"name\":\"ACT_SIMPLE_ACTION\",\"description\":\"A simple action saying hello\"}],\"predicates\":[{\"code\":\"PRE_RANDOM_PREDICATE\",\"name\":\"PRE_RANDOM_PREDICATE\",\"description\":\"A random false/true predicate\"}]},\"ko\":{\"id\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_ko\",\"name\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_ko\",\"details\":{\"code\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_ko\",\"name\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_ko\",\"description\":null},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_SIMPLE_ACTION\",\"name\":\"ACT_SIMPLE_ACTION\",\"description\":\"A simple action saying hello\"}],\"predicates\":[]},\"lastly\":{\"id\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_lastly\",\"name\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_lastly\",\"details\":{\"code\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_lastly\",\"name\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2_lastly\",\"description\":null},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_SIMPLE_ACTION\",\"name\":\"ACT_SIMPLE_ACTION\",\"description\":\"A simple action saying hello\"}],\"predicates\":[]},\"actions\":[],\"predicates\":[{\"code\":\"PRE_RANDOM_PREDICATE\",\"name\":\"PRE_RANDOM_PREDICATE\",\"description\":\"A random false/true predicate\"}]},{\"id\":\"BR_CALLER_RULE\",\"name\":\"BR_CALLER_RULE\",\"details\":{\"code\":\"BR_CALLER_RULE\",\"name\":\"BR_CALLER_RULE\",\"description\":\"A synchronized caller flow\"},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_CALL_ACTION\",\"name\":\"ACT_CALL_ACTION\",\"description\":\"Call another flow from current flow execution\"},{\"code\":\"ACT_CALL_NOAUDIT_ACTION\",\"name\":\"ACT_CALL_NOAUDIT_ACTION\",\"description\":\"Call another flow from current flow execution with no audit\"}],\"predicates\":[]},{\"id\":\"BR_ERROR_RULE\",\"name\":\"BR_ERROR_RULE\",\"details\":{\"code\":\"BR_ERROR_RULE\",\"name\":\"BR_ERROR_RULE\",\"description\":\"Error rule\"},\"ok\":{\"id\":\"BR_ERROR_RULE_ok\",\"name\":\"BR_ERROR_RULE_ok\",\"details\":{\"code\":\"BR_ERROR_RULE_ok\",\"name\":\"BR_ERROR_RULE_ok\",\"description\":null},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[],\"predicates\":[]},\"ko\":{\"id\":\"BR_ERROR_RULE_ko\",\"name\":\"BR_ERROR_RULE_ko\",\"details\":{\"code\":\"BR_ERROR_RULE_ko\",\"name\":\"BR_ERROR_RULE_ko\",\"description\":null},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[],\"predicates\":[]},\"lastly\":null,\"actions\":[{\"code\":\"ACT_ERROR_ACTION\",\"name\":\"ACT_ERROR_ACTION\",\"description\":\"action in error\"}],\"predicates\":[{\"code\":\"PRE_ERROR_PREDICATE\",\"name\":\"PRE_ERROR_PREDICATE\",\"description\":\"predicate in error\"}]},{\"id\":\"BR_OPERATION_RULE\",\"name\":\"BR_OPERATION_RULE\",\"details\":{\"code\":\"BR_OPERATION_RULE\",\"name\":\"BR_OPERATION_RULE\",\"description\":\"Operation rule\"},\"ok\":{\"id\":\"BR_OPERATION_RULE_ok\",\"name\":\"BR_OPERATION_RULE_ok\",\"details\":{\"code\":\"BR_OPERATION_RULE_ok\",\"name\":\"BR_OPERATION_RULE_ok\",\"description\":null},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_LOAD_FROM_CACHE_ACTION\",\"name\":\"ACT_LOAD_FROM_CACHE_ACTION\",\"description\":\"load result from cache\"}],\"predicates\":[]},\"ko\":{\"id\":\"BR_OPERATION_RULE_ko\",\"name\":\"BR_OPERATION_RULE_ko\",\"details\":{\"code\":\"BR_OPERATION_RULE_ko\",\"name\":\"BR_OPERATION_RULE_ko\",\"description\":null},\"ok\":{\"id\":\"BR_OPERATION_RULE_ko_ok\",\"name\":\"BR_OPERATION_RULE_ko_ok\",\"details\":{\"code\":\"BR_OPERATION_RULE_ko_ok\",\"name\":\"BR_OPERATION_RULE_ko_ok\",\"description\":null},\"ok\":{\"id\":\"BR_OPERATION_RULE_ko_ok_ok\",\"name\":\"BR_OPERATION_RULE_ko_ok_ok\",\"details\":{\"code\":\"BR_OPERATION_RULE_ko_ok_ok\",\"name\":\"BR_OPERATION_RULE_ko_ok_ok\",\"description\":null},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_DO_OPERATION_ACTION\",\"name\":\"ACT_DO_OPERATION_ACTION\",\"description\":\"Do a simple operation\"}],\"predicates\":[]},\"ko\":{\"id\":\"BR_OPERATION_RULE_ko_ok_ko\",\"name\":\"BR_OPERATION_RULE_ko_ok_ko\",\"details\":{\"code\":\"BR_OPERATION_RULE_ko_ok_ko\",\"name\":\"BR_OPERATION_RULE_ko_ok_ko\",\"description\":null},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_SET_ERROR_VALUES_ACTION\",\"name\":\"ACT_SET_ERROR_VALUES_ACTION\",\"description\":\"Wrong values error\"}],\"predicates\":[]},\"lastly\":null,\"actions\":[],\"predicates\":[{\"code\":\"PRE_IS_VALID_VALUES_PREDICATE\",\"name\":\"PRE_IS_VALID_VALUES_PREDICATE\",\"description\":\"Check if the values are valid\"}]},\"ko\":{\"id\":\"BR_OPERATION_RULE_ko_ko\",\"name\":\"BR_OPERATION_RULE_ko_ko\",\"details\":{\"code\":\"BR_OPERATION_RULE_ko_ko\",\"name\":\"BR_OPERATION_RULE_ko_ko\",\"description\":null},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_SET_ERROR_OPERATION_ACTION\",\"name\":\"ACT_SET_ERROR_OPERATION_ACTION\",\"description\":\"Wrong operation symbol error\"}],\"predicates\":[]},\"lastly\":null,\"actions\":[],\"predicates\":[{\"code\":\"PRE_IS_VALID_OPERATION_PREDICATE\",\"name\":\"PRE_IS_VALID_OPERATION_PREDICATE\",\"description\":\"Check if the operation is valid\"}]},\"lastly\":null,\"actions\":[],\"predicates\":[{\"code\":\"PRE_OPERATION_IN_CACHE_PREDICATE\",\"name\":\"PRE_OPERATION_IN_CACHE_PREDICATE\",\"description\":\"Check if the operation is already in cache\"}]},{\"id\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE\",\"name\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE\",\"details\":{\"code\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE\",\"name\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE\",\"description\":\"A Simple rule!\"},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_SIMPLE_ACTION\",\"name\":\"ACT_SIMPLE_ACTION\",\"description\":\"A simple action saying hello\"}],\"predicates\":[]},{\"id\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE2\",\"name\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE2\",\"details\":{\"code\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE2\",\"name\":\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE2\",\"description\":\"A Simple rule bis!\"},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_SIMPLE_ACTION\",\"name\":\"ACT_SIMPLE_ACTION\",\"description\":\"A simple action saying hello\"}],\"predicates\":[]},{\"id\":\"BR_SLOW_RULE\",\"name\":\"BR_SLOW_RULE\",\"details\":{\"code\":\"BR_SLOW_RULE\",\"name\":\"BR_SLOW_RULE\",\"description\":\"A slow (3sec execution) rule!\"},\"ok\":null,\"ko\":null,\"lastly\":null,\"actions\":[{\"code\":\"ACT_SLOW_ACTION\",\"name\":\"ACT_SLOW_ACTION\",\"description\":\"Wait for 3sec\"}],\"predicates\":[]}],\"flowSpecifications\":[{\"id\":\"AdvancedFlow\",\"ruleSpecificationIds\":[\"LoadUser\",\"LoadCompany\",\"EnrichUser\"],\"code\":\"AdvancedFlow\",\"name\":\"AdvancedFlow\",\"description\":\"Advanced flow definition\"},{\"id\":\"userNotificationFlow\",\"ruleSpecificationIds\":[\"userNotificationRule\"],\"code\":\"userNotificationFlow\",\"name\":\"userNotificationFlow\",\"description\":\"Simple Fake user notification Flow definition\"},{\"id\":\"hello\",\"ruleSpecificationIds\":[\"hello1\"],\"code\":\"hello\",\"name\":\"hello\",\"description\":\"Simple HelloWorld Flow definition\"},{\"id\":\"FLOW_CALLER_FLOW\",\"ruleSpecificationIds\":[\"BR_CALLER_RULE\",\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE\"],\"code\":\"FLOW_CALLER_FLOW\",\"name\":\"FLOW_CALLER_FLOW\",\"description\":\"A caller flow in a flow\"},{\"id\":\"FLOW_ERROR_FLOW\",\"ruleSpecificationIds\":[\"BR_ERROR_RULE\"],\"code\":\"FLOW_ERROR_FLOW\",\"name\":\"FLOW_ERROR_FLOW\",\"description\":\"A flow in error\"},{\"id\":\"FLOW_OPERATION_FLOW\",\"ruleSpecificationIds\":[\"BR_OPERATION_RULE\"],\"code\":\"FLOW_OPERATION_FLOW\",\"name\":\"FLOW_OPERATION_FLOW\",\"description\":\"A operation flow\"},{\"id\":\"FLOW_SIMPLE_FLOW\",\"ruleSpecificationIds\":[\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE\",\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2\",\"BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE2\"],\"code\":\"FLOW_SIMPLE_FLOW\",\"name\":\"FLOW_SIMPLE_FLOW\",\"description\":\"A simple flow\"},{\"id\":\"FLOW_SUBFLOW_SLOW\",\"ruleSpecificationIds\":[\"BR_SLOW_RULE\"],\"code\":\"FLOW_SUBFLOW_SLOW\",\"name\":\"FLOW_SUBFLOW_SLOW\",\"description\":\"A SLOW sub flow\"}]}";
//
//  @Test
//  void should_return_no_result() {
//    String url = getSearchUrl("noexistingauditview");
//    Collection<SearchQuery> request = new ArrayList<>();
//
//    ResponseEntity<String> response = testRestTemplate.postForEntity(url, request, String.class);
//
//    assertThat(response.getBody()).isEqualTo("[]");
//    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//  }
//
//  @Test
//  void should_search_return_result_normal_case() throws IOException {
//    // Given
//    String url = getSearchUrl("auditviewname1");
//    Collection<SearchQuery> request = new ArrayList<>();
//    SearchResponse elasticSearchResponse = loadSearchResponse();
//    when(SpringForItConfiguration.mockElasticSearchClientWrapper.search(
//            any(SearchRequest.class), any(RequestOptions.class)))
//        .thenReturn(elasticSearchResponse);
//
//    // When
//    ResponseEntity<AuditReport[]> response =
//        testRestTemplate.postForEntity(url, request, AuditReport[].class);
//
//    // Then
//    assertThat(response.getBody()).isNotNull().hasSize(1);
//    AuditReport hit1 = response.getBody()[0];
//    assertThat(hit1)
//        .hasFieldOrPropertyWithValue("domain", "wcp")
//        .hasFieldOrPropertyWithValue("env", "local")
//        .hasFieldOrPropertyWithValue("version", "2.7.0.202210.2")
//        .hasFieldOrPropertyWithValue("appId", "wcpsamples");
//    assertThat(hit1.getIndexedKeyValues()).hasSize(23);
//    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//  }
//
//  @Test
//  void should_search_return_result_normal_case_multiple() throws IOException {
//    // Given
//    String url = getSearchUrl("auditviewname1");
//    Collection<SearchQuery> request = new ArrayList<>();
//    SearchResponse elasticSearchResponse =
// loadSearchResponse("/data/wcp-sample-audit-size8.json");
//    when(SpringForItConfiguration.mockElasticSearchClientWrapper.search(
//            any(SearchRequest.class), any(RequestOptions.class)))
//        .thenReturn(elasticSearchResponse);
//
//    // When
//    ResponseEntity<AuditReport[]> response =
//        testRestTemplate.postForEntity(url, request, AuditReport[].class);
//
//    // Then
//    assertThat(response.getBody()).isNotNull().hasSize(8);
//    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//  }
//
//  @Test
//  void should_getone_result_normal_case() throws IOException {
//    // Given
//    String url = getOneUrl("auditviewname1", "1");
//    SearchResponse elasticSearchResponse = loadSearchResponse();
//    when(SpringForItConfiguration.mockElasticSearchClientWrapper.search(
//            any(SearchRequest.class), any(RequestOptions.class)))
//        .thenReturn(elasticSearchResponse);
//    KEngineRegistryDocument registryDocument = new KEngineRegistryDocument();
//    registryDocument.setJsonValue(KENGINE_REFERENTIAL_JSON_VALUE);
//    when(kEngineRegistryDocumentRepository.findById("wcp-local-wcpsamples-1486806402"))
//        .thenReturn(Optional.of(registryDocument));
//
//    // When
//    ResponseEntity<AuditReport> response = testRestTemplate.getForEntity(url, AuditReport.class);
//
//    // Then
//    assertThat(response.getBody()).isNotNull();
//    List events = (List) response.getBody().getAudits().get(0).get("events");
//    Map event1Map = (Map) events.get(0);
//    Map executionEDTDTO = (Map) event1Map.get("executionEDTDTO");
//    assertThat(executionEDTDTO.get("referential")).isNotNull();
//    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//  }
//
//  @Test
//  void should_getone_result_not_found() throws IOException {
//    // Given
//    String url = getOneUrl("auditviewname1", "2");
//    when(SpringForItConfiguration.mockElasticSearchClientWrapper.search(
//            any(SearchRequest.class), any(RequestOptions.class)))
//        .thenReturn(loadEmptySearchResponse());
//
//    // When
//    ResponseEntity<AuditReport> response = testRestTemplate.getForEntity(url, AuditReport.class);
//
//    // Then
//    assertThat(response.getBody()).isNull();
//    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//  }
//
//  private String getSearchUrl(String auditViewName) {
//    return "http://localhost:"
//        + port
//        + "/api/services/it/test/audit/requests/"
//        + auditViewName
//        + "/_search";
//  }
//
//  private String getOneUrl(String auditViewName, String id) {
//    return "http://localhost:"
//        + port
//        + "/api/services/it/test/audit/requests/"
//        + auditViewName
//        + "/"
//        + id;
//  }
// }
