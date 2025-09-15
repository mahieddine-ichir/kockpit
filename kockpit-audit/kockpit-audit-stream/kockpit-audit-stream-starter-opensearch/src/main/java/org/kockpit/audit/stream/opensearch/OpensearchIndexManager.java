package org.kockpit.audit.stream.opensearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.opensearch.OpenSearchStatusException;
import org.opensearch.action.admin.indices.alias.Alias;
import org.opensearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.*;
import org.opensearch.client.indices.*;
import org.opensearch.cluster.metadata.AliasMetadata;
import org.opensearch.cluster.metadata.ComposableIndexTemplate;
import org.opensearch.cluster.metadata.Template;
import org.opensearch.common.compress.CompressedXContent;
import org.opensearch.common.settings.Settings;
import org.opensearch.core.rest.RestStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.kockpit.audit.stream.opensearch.OpensearchHelper.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class OpensearchIndexManager implements IndexManager {

    private final RestHighLevelClient restHighLevelClient;

    private final ObjectMapper objectMapper;

    @Value("${kockpit.audit.stream.opensearch.index_suffix}")
    private String indexSuffix;

    @Value("${kockpit.audit.stream.opensearch.index_mapping_file:index-template.json}")
    private String indexMappingFile;

    private final Set<String> aliasWriteAlreadyInit = new HashSet<>();

    @Override
    public String getWriteAlias(String domain, String env, int ttl) {
        String indexPrefix = getIndexPrefix(domain, indexSuffix, env, ttl);
        String aliasWrite = getAliasWrite(domain, indexSuffix, env, ttl);
        String aliasRead = getAliasRead(domain, indexSuffix, env);

        // Prepare policy, index template, index and alias if not already done
        autoCreateWriteAlias(ttl, indexPrefix, aliasWrite, aliasRead, indexMappingFile);

        return aliasWrite;
    }

    @SneakyThrows
    private GetComposableIndexTemplatesResponse getIndexTemplate(String indexTemplateName) {
        GetComposableIndexTemplateRequest request =
                new GetComposableIndexTemplateRequest(indexTemplateName);
        try {
            return restHighLevelClient.indices().getIndexTemplate(request, RequestOptions.DEFAULT);
        } catch (OpenSearchStatusException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                return null;
            } else {
                throw new IOException("Error while check template exist, status : " + e.status(), e);
            }
        }
    }

    @SneakyThrows
    private void createIndexTemplate(String indexPrefix, String aliasWrite, String aliasRead, String indexMappingFile) {
        String templateName = "index_template_" + indexPrefix;
        String indexPattern = indexPrefix + "*";
        log.debug(
                "Creating index template {} version {} for apply on index pattern {}",
                templateName,
                INDEX_TEMPLATE_VERSION,
                indexPattern);

        GetComposableIndexTemplatesResponse actualIndexTemplate = getIndexTemplate(templateName);

        Boolean indexExists = Optional.ofNullable(actualIndexTemplate)
                .map(GetComposableIndexTemplatesResponse::getIndexTemplates)
                .map(stringComposableIndexTemplateMap -> stringComposableIndexTemplateMap.get(templateName))
                .map(ComposableIndexTemplate::version)
                .map(version -> version.equals(INDEX_TEMPLATE_VERSION))
                .orElse(false);

        if (indexExists) {
            log.info(
                    "Index template {} version {} already exist -> skip creation",
                    templateName,
                    INDEX_TEMPLATE_VERSION);
            return;
        }

        Settings settings = OpensearchHelper.buildSettings(aliasWrite);

        String mapping = new String(this.getClass().getClassLoader().getResourceAsStream(indexMappingFile)
                .readAllBytes(), StandardCharsets.UTF_8);
        CompressedXContent mappings = new CompressedXContent(mapping);

        // Alias use for read (same for all ttl)
        Map<String, AliasMetadata> aliases = new HashMap<>();
        AliasMetadata aliasReadMetadata = AliasMetadata.builder(aliasRead).build();
        aliases.put(aliasRead, aliasReadMetadata);

        Template template = new Template(settings, mappings, aliases);

        ComposableIndexTemplate indexTemplate =
                new ComposableIndexTemplate(
                        Collections.singletonList(indexPattern),
                        template,
                        emptyList(),
                        INDEX_TEMPLATE_VERSION,
                        INDEX_TEMPLATE_VERSION,
                        new HashMap<>());

        PutComposableIndexTemplateRequest putComposableIndexTemplateRequest =
                new PutComposableIndexTemplateRequest()
                        .name(templateName)
                        // If false -> update
                        .create(Objects.isNull(actualIndexTemplate))
                        .indexTemplate(indexTemplate);

        try {
            AcknowledgedResponse putTemplateResponse =
                    restHighLevelClient
                            .indices()
                            .putIndexTemplate(putComposableIndexTemplateRequest, RequestOptions.DEFAULT);

            log.info(
                    "Template {} created -> acknowledge : {}",
                    templateName,
                    putTemplateResponse.isAcknowledged());
        } catch (IOException e) {
            throw new IOException(
                    "Error create index template version " + INDEX_TEMPLATE_VERSION + " " + templateName, e);
        }
    }

    @SneakyThrows
    private void autoCreateWriteAlias(int ttl, String indexPrefix, String aliasWrite, String aliasRead, String indexMappingFile) {
        // Already created?
        if (aliasWriteAlreadyInit.contains(aliasWrite)) {
            return;
        }

        createLifecyclePolicy(indexPrefix, ttl);
        createIndexTemplate(indexPrefix, aliasWrite, aliasRead, indexMappingFile);
        createIndex(indexPrefix, aliasWrite, aliasRead);
        aliasWriteAlreadyInit.add(aliasWrite);
    }

    @SneakyThrows
    private void createLifecyclePolicy(String indexPrefix, int ttl) {
        String policyName = "policy_" + indexPrefix;
        String indexPattern = indexPrefix + "*";

        log.info(
                "Creating policy {} version {} for apply on index pattern {}",
                policyName,
                POLICY_VERSION,
                indexPattern);

        GetOpendistroPolicyResponse getPolicyResponse = getLifecyclePolicy(policyName);

        if (nonNull(getPolicyResponse)
                && getPolicyResponse.getPolicy().getSchemaVersion() == POLICY_VERSION) {
            log.info("Policy {} version {} already exist -> skip creation", policyName, POLICY_VERSION);
            return;
        }

        String policyTemplate = new String(this.getClass().getResourceAsStream("/audit-policy.json").readAllBytes(),
                StandardCharsets.UTF_8);

        policyTemplate = policyTemplate
                .replace("${version}", String.valueOf(POLICY_VERSION))
                .replace("${index_patterns}", indexPattern)
                // Rollover after 1 day or 30gb
                .replace("${rollover_min_index_age}", "1d")
                .replace("${rollover_min_size}", "30gb")
                // Delete after ttl day
                .replace("${delete_min_index_age}", ttl + "d");

        Request request = new Request("PUT", "/_opendistro/_ism/policies/" + policyName);

        if (nonNull(getPolicyResponse)) {
            // Mandatory param to update
            request.addParameter("if_seq_no", getPolicyResponse.getSeqNo());
            request.addParameter("if_primary_term", getPolicyResponse.getPrimaryTerm());
        }

        request.setJsonEntity(policyTemplate);

        Response response = restHighLevelClient.getLowLevelClient().performRequest(request);
        log.info("lifecycle policy {} created -> status code {}", policyName, response.getStatusLine().getStatusCode());
    }

    private GetOpendistroPolicyResponse getLifecyclePolicy(String policyName) throws IOException {
        try {
            Request request = new Request("GET", "/_opendistro/_ism/policies/" + policyName);
            Response response = restHighLevelClient.getLowLevelClient().performRequest(request);
            return objectMapper.readValue(
                    EntityUtils.toString(response.getEntity()), GetOpendistroPolicyResponse.class);
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return null;
            } else {
                throw new IOException("Error while check policy exist ", e);
            }
        }
    }

    /**
     * Create Index for rollover, we have to use xxx-2021.11.09-1 for name index + write alias with
     * "is_write_index": true for rollover feature
     */
    @SneakyThrows
    private void createIndex(String indexPrefix, String aliasWrite, String aliasRead) {
        String indexName = "<" + indexPrefix + "-{now/d}-1>";
        log.info(
                "Creating index {} with write alias {}, read alias {}", indexName, aliasWrite, aliasRead);

        if (checkAliasExist(aliasWrite)) {
            log.info("Write alias {} already exist -> skip {} index creation", aliasWrite, indexName);
            return;
        }

        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName)
                .alias(new Alias(aliasWrite).writeIndex(true))
                .alias(new Alias(aliasRead));

        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        log.info("Index {} created -> nodes acknowledge {}, shards acknowledge {}",
                indexName,
                createIndexResponse.isAcknowledged(),
                createIndexResponse.isShardsAcknowledged());
    }

    @SneakyThrows
    private boolean checkAliasExist(String alias) {
        GetAliasesRequest request = new GetAliasesRequest(alias);
        return restHighLevelClient.indices().existsAlias(request, RequestOptions.DEFAULT);
    }

}
