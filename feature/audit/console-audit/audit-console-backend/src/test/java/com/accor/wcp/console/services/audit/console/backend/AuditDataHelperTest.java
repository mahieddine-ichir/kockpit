package com.accor.wcp.console.services.audit.console.backend;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.common.xcontent.json.JsonXContent;
import org.opensearch.core.xcontent.NamedXContentRegistry;
import org.opensearch.core.xcontent.XContentParser;

class AuditDataHelperTest {

  static SearchResponse loadEmptySearchResponse() throws IOException {
    return loadSearchResponse("/data/wcp-sample-empty.json");
  }

  static SearchResponse loadSearchResponse() throws IOException {
    return loadSearchResponse("/data/wcp-sample-audit1.json");
  }

  static SearchResponse loadSearchResponse(String fullFilename) throws IOException {
    InputStream resourceAsStream = AuditDataHelperTest.class.getResourceAsStream(fullFilename);

    NamedXContentRegistry registry = NamedXContentRegistry.EMPTY;
    XContentParser parser =
        JsonXContent.jsonXContent.createParser(registry, null, resourceAsStream);
    return SearchResponse.fromXContent(parser);
  }

  @Test
  void should_load_es_searchresponse() throws IOException {
    SearchResponse searchResponse = loadSearchResponse();
    assertThat(searchResponse).isNotNull();
  }
}
