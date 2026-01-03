package org.kockpit.audit.stream.opensearch;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.opensearch.client.Request;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Response;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;

@RequiredArgsConstructor
@Slf4j
public class OpensearchV3IndexManager {

    private final RestHighLevelClient client;

    @SneakyThrows
    public void ensureIndexExists(String indexName, String aliasWrite, String aliasRead, String indexPrefix, Integer ttl) {
        // create policy
        String policyId = "audit_ism_policy_" + indexPrefix;
        createISMPolicy(ttl, policyId);
        // create template
        createIndexTemplate(indexPrefix, policyId);
        // create index
        if (!client.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT)) {

            CreateIndexRequest request = new CreateIndexRequest(indexName);
            client.indices().create(request, RequestOptions.DEFAULT);

            // Attach write alias (only current index)
            attachWriteAlias(indexName, aliasWrite, indexPrefix);

            // Attach read alias (all logs-* indices)
            attachReadAlias(indexName, aliasRead);
        }
    }

    @SneakyThrows
    void createISMPolicy(Integer ttl, String policyId) {
        String policyJson = new String(this.getClass().getResourceAsStream("/opensearch/audit_ism_policy.json").readAllBytes())
                .replace("${delete_min_index_age}", ttl+"d");

        // Check if policy already exists
        try {
            Request getRequest = new Request("GET", "_plugins/_ism/policies/"+policyId);
            Response getResponse = client.getLowLevelClient().performRequest(getRequest);
            log.trace("Policy {} already exists, skipping policy creation, status {}", policyId, getResponse.getStatusLine());

            // Policy exists, update it with seq_no and primary_term
            /* fixme -> response misses data
            log.trace("Policy {} already exists, updating it", policyId);
            Request updateRequest = new Request("PUT", "_plugins/_ism/policies/"+policyId+"?if_seq_no="+getSeqNo(getResponse)+"&if_primary_term="+getPrimaryTerm(getResponse));
            updateRequest.setJsonEntity(policyJson);
            Response updateResponse = client.getLowLevelClient().performRequest(updateRequest);
            log.info("Updated policy {} -> response = {}", policyId, updateResponse.getStatusLine());
             */
        } catch (Exception e) {
            // Policy doesn't exist, create it
            Request createRequest = new Request("PUT", "_plugins/_ism/policies/"+policyId);
            createRequest.setJsonEntity(policyJson);
            Response createResponse = client.getLowLevelClient().performRequest(createRequest);
            log.info("Created policy {} -> response = {}", policyId, createResponse.getStatusLine());
        }
    }

    @SneakyThrows
    private long getSeqNo(Response response) {
        String responseBody = new String(response.getEntity().getContent().readAllBytes());
        return Long.parseLong(responseBody.split("\"_seq_no\":")[1].split(",")[0]);
    }

    @SneakyThrows
    private long getPrimaryTerm(Response response) {
        String responseBody = new String(response.getEntity().getContent().readAllBytes());
        return Long.parseLong(responseBody.split("\"_primary_term\":")[1].split(",")[0]);
    }

    @SneakyThrows
    public void createIndexTemplate(String indexPrefix, String policyId) {
        String templateJson = new String(this.getClass().getResourceAsStream("/opensearch/audit_index_template.json").readAllBytes())
                .replace("${index_pattern}", indexPrefix+"*")
                .replace("${policy_id}", policyId);

        Request request = new Request("PUT", "_index_template/"+indexPrefix+"_template");
        request.setJsonEntity(templateJson);

        Response response = client.getLowLevelClient().performRequest(request);
        log.debug("Created template {} -> response = {}", indexPrefix, response.getStatusLine());
    }

    @SneakyThrows
    private void attachWriteAlias(String indexName, String aliasWrite, String indexPrefix) {
        // First try to remove the alias from previous indices (ignore if it doesn't exist)
        try {
            IndicesAliasesRequest removeRequest = new IndicesAliasesRequest();
            removeRequest.addAliasAction(new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                    .index(indexPrefix + "-*")
                    .alias(aliasWrite));
            client.indices().updateAliases(removeRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.debug("No previous alias to remove for {}: {}", aliasWrite, e.getMessage());
        }

        // Always add write alias to the current index as the write index
        IndicesAliasesRequest addRequest = new IndicesAliasesRequest();
        addRequest.addAliasAction(new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                .index(indexName)
                .alias(aliasWrite)
                .writeIndex(true));
        client.indices().updateAliases(addRequest, RequestOptions.DEFAULT);
    }

    @SneakyThrows
    private void attachReadAlias(String indexName, String aliasRead) {
        IndicesAliasesRequest request = new IndicesAliasesRequest();
        request.addAliasAction(new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                .index(indexName)
                .alias(aliasRead));
        client.indices().updateAliases(request, RequestOptions.DEFAULT);
    }
}
