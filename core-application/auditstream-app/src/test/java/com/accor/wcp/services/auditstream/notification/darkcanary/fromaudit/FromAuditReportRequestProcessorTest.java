package com.accor.wcp.services.auditstream.notification.darkcanary.fromaudit;

/*
@SpringBootTest
@ActiveProfiles("opensearch")
class FromAuditReportRequestProcessorTest {

    @Autowired
    private FromAuditReportRequestProcessor fromAuditReportRequestProcessor;

    @BeforeEach
    void init() throws IOException {
        IndicesClient mock = Mockito.mock(IndicesClient.class);
        Mockito.when(mock.exists(Mockito.any(GetIndexRequest.class), Mockito.same(RequestOptions.DEFAULT))).thenReturn(true);
        Mockito.when(restHighLevelClient.indices()).thenReturn(mock);
    }

    @Test
    void process_on_requests_should_be_produce_index_document_with_no_difference() {

        List<AuditReportRequest> requests = List.of(
                req1(), req1()
        );
        List<DarkCanaryIndexDocument> darkCanaryIndexDocuments = fromAuditReportRequestProcessor.process(requests);

        Assertions.assertThat(darkCanaryIndexDocuments).hasSize(1);
    }

    @SneakyThrows
    private @NotNull AuditReportRequest req1() {
        return new ObjectMapper().readValue(this.getClass().getResourceAsStream("/darkcanary_testing/audit-sample.json"), AuditReportRequest.class);
    }
}

 */