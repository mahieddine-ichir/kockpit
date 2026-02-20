package org.kockpit.audit.stream.opensearch;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.opensearch.requests.CreatePolicyRequest;
import org.kockpit.audit.stream.opensearch.requests.CreateTemplateRequest;
import org.kockpit.audit.stream.opensearch.requests.GetPolicyRequest;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.generic.Response;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.UpdateAliasesRequest;
import org.opensearch.client.opensearch.indices.update_aliases.Action;
import org.opensearch.client.opensearch.indices.update_aliases.AddAction;
import org.opensearch.client.opensearch.indices.update_aliases.RemoveAction;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Slf4j
public class OpensearchV3IndexManager {

    private final OpenSearchClient client;

    @SneakyThrows
    public void ensureIndexExists(String indexName, String aliasWrite, String aliasRead, String indexPrefix, Integer ttl) {
        // create policy
        String policyId = "audit_ism_policy_" + indexPrefix;
        createISMPolicy(ttl, policyId);
        // create template
        createIndexTemplate(indexPrefix, policyId);
        // create index
        if (!client.indices().exists(ExistsRequest.of(e -> e.index(indexName))).value()) {
            log.info("Creating index name: {}", indexName);

            CreateIndexRequest request = CreateIndexRequest.of(c ->
                    c.index(indexName)
            );
            client.indices().create(request);

            // Attach write alias (only current index)
            attachWriteAlias(indexName, aliasWrite, indexPrefix);

            // Attach read alias (all logs-* indices)
            attachReadAlias(indexName, aliasRead);
        }
    }

    @SneakyThrows
    void createISMPolicy(Integer ttl, String policyId) {
        // Check if policy already exists
        try {
            GetPolicyRequest getPolicyRequest = new GetPolicyRequest(policyId);
            Response response = client.generic().execute(getPolicyRequest);
            log.trace("Policy {} already exists, skipping policy creation, status {}", policyId, response.getStatus());
        } catch (Exception e) {
            String policyJson = new String(this.getClass().getResourceAsStream("/opensearch/audit_ism_policy.json").readAllBytes())
                    .replace("${delete_min_index_age}", ttl+"d");

            // Policy doesn't exist, create it
            doCreatePolicy(policyId, ttl, policyJson);
        }
    }

    private void doCreatePolicy(String policyId, Integer ttl, String policyJson) {
        try {
            log.info("➡️ Creating ISM policy with ID: {} and TTL: {}d", policyId, ttl);
            log.trace("ISM Policy JSON being sent:\n{}", policyJson);

            CreatePolicyRequest createPolicyRequest = new CreatePolicyRequest(policyId, policyJson.getBytes(StandardCharsets.UTF_8));
            try (Response createResponse = client.generic().execute(createPolicyRequest)) {
                log.info("✅ Created policy {} -> response = {}", policyId, createResponse.getStatus());
            }
        } catch (Exception e) {
            log.error("❌ Failed to create ISM policy {} with TTL {}d: {}", policyId, ttl, e.getMessage(), e);
        }
    }

    @SneakyThrows
    public void createIndexTemplate(String indexPrefix, String policyId) {
        try {
            log.info("➡️ Creating Template {} for policy {}", indexPrefix, policyId);
            String templateJson = new String(this.getClass().getResourceAsStream("/opensearch/audit_index_template.json").readAllBytes())
                    .replace("${index_pattern}", indexPrefix + "*")
                    .replace("${policy_id}", policyId);

            try (Response response = client.generic().execute(new CreateTemplateRequest(indexPrefix, templateJson.getBytes(StandardCharsets.UTF_8)))) {
                log.debug("Created template {} -> response = {}", indexPrefix, response.getStatus());
            }
        } catch (Exception e) {
            log.error("❌ Failed to create template for indexPrefix {} and policyId {}", indexPrefix, policyId, e);
        }
    }

    @SneakyThrows
    private void attachWriteAlias(String indexName, String aliasWrite, String indexPrefix) {
        // First try to remove the alias from previous indices (ignore if it doesn't exist)
        try {
            UpdateAliasesRequest removeRequest = UpdateAliasesRequest.of(u -> u
                    .actions(Action.of(a -> a
                            .remove(RemoveAction.of(r -> r
                                    .indices(indexPrefix + "-*")
                                    .aliases(aliasWrite)
                            ))
                    ))
            );
            client.indices().updateAliases(removeRequest);
        } catch (Exception e) {
            log.debug("No previous alias to remove for {}: {}", aliasWrite, e.getMessage());
        }

        // Always add write alias to the current index as the write index
        UpdateAliasesRequest addRequest = UpdateAliasesRequest.of(u -> u
                .actions(Action.of(a -> a
                        .add(AddAction.of(add -> add
                                .indices(indexName)
                                .aliases(aliasWrite)
                                .isWriteIndex(true)
                        ))
                ))
        );
        client.indices().updateAliases(addRequest);
    }

    @SneakyThrows
    private void attachReadAlias(String indexName, String aliasRead) {
        UpdateAliasesRequest request = UpdateAliasesRequest.of(u -> u
                .actions(Action.of(a -> a
                        .add(AddAction.of(add -> add
                                .indices(indexName)
                                .aliases(aliasRead)
                        ))
                ))
        );
        client.indices().updateAliases(request);
    }
}
