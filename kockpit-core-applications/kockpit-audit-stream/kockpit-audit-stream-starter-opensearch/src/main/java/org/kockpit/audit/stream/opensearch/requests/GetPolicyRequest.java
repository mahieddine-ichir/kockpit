package org.kockpit.audit.stream.opensearch.requests;

import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.generic.Body;
import org.opensearch.client.opensearch.generic.Request;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class GetPolicyRequest implements Request {

    private final String policyId;

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public String getEndpoint() {
        return "_plugins/_ism/policies/"+policyId;
    }

    @Override
    public Map<String, String> getParameters() {
        return Map.of();
    }

    @Override
    public Collection<Map.Entry<String, String>> getHeaders() {
        return List.of(
                Map.entry("content-type", "application/json")
        );
    }

    @Override
    public Optional<Body> getBody() {
        return Optional.empty();
    }
}
