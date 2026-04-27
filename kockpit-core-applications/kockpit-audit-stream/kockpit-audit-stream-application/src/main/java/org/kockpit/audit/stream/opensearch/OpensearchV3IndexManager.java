package org.kockpit.audit.stream.opensearch;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.generic.Body;
import org.opensearch.client.opensearch.generic.Requests;
import org.opensearch.client.opensearch.generic.Response;
import org.opensearch.client.opensearch.indices.*;
import org.opensearch.client.opensearch.indices.update_aliases.Action;
import org.opensearch.client.opensearch.indices.update_aliases.AddAction;
import org.opensearch.client.opensearch.indices.update_aliases.RemoveAction;

@RequiredArgsConstructor
@Slf4j
public class OpensearchV3IndexManager {

    private final OpenSearchClient client;

    private final boolean strictMode;

    @SneakyThrows
    public void ensureIndexExists(String indexName, String aliasWrite, String aliasRead, String indexPrefix, Integer ttl) {
        // create policy
        String policyId = "audit_ism_policy_" + indexPrefix;
        createISMPolicy(ttl, policyId, indexPrefix);
        // create template
        createIndexTemplate(indexPrefix, policyId, aliasWrite);
        // create index
        if (!client.indices().exists(ExistsRequest.of(e -> e.index(indexName))).value()) {
            log.info("Creating index {}", indexName);

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
    void createISMPolicy(Integer ttl, String policyId, String indexPrefix) {
        // Check if policy already exists
        try {
            Response response = client.generic().execute(Requests.builder()
                    .method("GET")
                    .endpoint("_plugins/_ism/policies/" + policyId)
                    .build());
            if (isOk(response.getStatus())) {
                log.trace("✅ Policy {} already exists, skipping policy creation, status {}", policyId, response.getStatus());
            }
        } catch (Exception e) {
            String policyJson = new String(this.getClass().getResourceAsStream("/opensearch/audit_ism_policy.json").readAllBytes())
                    .replace("${delete_min_index_age}", ttl+"d")
                    .replace("${index_pattern}", indexPrefix + "*");

            // Policy doesn't exist, create it
            doCreatePolicy(policyId, ttl, policyJson);
        }
    }

    private boolean isOk(int status) {
        return status / 100 == 2;
    }

    private void doCreatePolicy(String policyId, Integer ttl, String policyJson) {
        try {
            log.info("➡️ Creating ISM policy with ID: {} and TTL: {}d", policyId, ttl);
            log.trace("ISM Policy JSON being sent:\n{}", policyJson);

            Response response = client.generic().execute(Requests.builder()
                    .method("PUT")
                    .endpoint("_plugins/_ism/policies/" + policyId)
                    .json(policyJson)
                    .build());
            if (isOk(response.getStatus())) {
                log.info("✅ Created policy {}, ttl {}", policyId, ttl);
            } else {
                log.error("❌ Failed to create ISM policy {} with TTL {}: response {}: {}", policyId, ttl, response.getStatus(), response.getBody().map(Body::bodyAsString).orElse(null));
                throw new RuntimeException("❌ Policy creation failed with status " + response.getStatus());
            }
        } catch (Exception e) {
            log.error("❌ Failed to create ISM policy {} with TTL {}", policyId, ttl, e);
            if (strictMode) {
                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @SneakyThrows
    private boolean templateExists(String name) {
        try (Response response = client.generic().execute(Requests.builder()
                .method("HEAD")
                .endpoint("_index_template/" + name)
                .build())) {
            return isOk(response.getStatus());
        }
    }

    @SneakyThrows
    void createIndexTemplate(String indexPrefix, String policyId, String aliasWrite) {
        try {
            if (templateExists(indexPrefix) || templateExists(indexPrefix + "_template")) {
                log.trace("✅ Template {} already exists, skipping creation", indexPrefix);
                return;
            }

            log.info("➡️ Creating Template {} for policy {}", indexPrefix, policyId);
            String templateJson = new String(this.getClass().getResourceAsStream("/opensearch/audit_index_template.json").readAllBytes())
                    .replace("${index_pattern}", indexPrefix + "*")
                    .replace("${policy_id}", policyId)
                    .replace("${write_alias}", aliasWrite);

            try (Response response = client.generic().execute(Requests.builder()
                    .method("PUT")
                    .endpoint("_index_template/" + indexPrefix)
                    .json(templateJson)
                    .build())) {
                if (isOk(response.getStatus())) {
                    log.info("✅ Template created for indexPrefix {}", indexPrefix);
                } else {
                    log.error("❌ Create Template {} failed with status {}: {}", indexPrefix, response.getStatus(), response.getBody().map(Body::bodyAsString).orElse(null));
                    throw new RuntimeException("Create Template " + indexPrefix + " failed with status " + response.getStatus());
                }
            }
        } catch (Exception e) {
            log.error("❌ Failed to create template for indexPrefix {} and policyId {}", indexPrefix, policyId, e);
            if (strictMode) {
                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                } else {
                    throw new RuntimeException(e);
                }
            }
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
