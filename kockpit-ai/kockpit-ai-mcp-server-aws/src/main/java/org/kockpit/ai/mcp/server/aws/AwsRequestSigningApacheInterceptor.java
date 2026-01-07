package org.kockpit.ai.mcp.server.aws;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@RequiredArgsConstructor
public class AwsRequestSigningApacheInterceptor implements org.apache.hc.core5.http.HttpRequestInterceptor {

    private final RequestSigner signer;

    private Map<String, List<String>> headerArrayToMap(org.apache.hc.core5.http.Header[] headers) {
        Map<String, List<String>> headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for(org.apache.hc.core5.http.Header header : headers) {
            if (!skipHeader(header)) {
                headersMap.put(header.getName(), headersMap.getOrDefault(header.getName(), new LinkedList<>(Collections.singletonList(header.getValue()))));
            }
        }

        return headersMap;
    }

    private boolean skipHeader(Header header) {
        return "Content-Length".equalsIgnoreCase(header.getName()) && "0".equals(header.getValue()) || "Host".equalsIgnoreCase(header.getName());
    }

    private org.apache.hc.core5.http.Header[] mapToHeaderArray(Map<String, List<String>> mapHeaders) {
        org.apache.hc.core5.http.Header[] headers = new org.apache.hc.core5.http.Header[mapHeaders.size()];
        int i = 0;

        for (Map.Entry<String, List<String>> headerEntry : mapHeaders.entrySet()) {
            for (String value : headerEntry.getValue()) {
                headers[i++] = new BasicHeader(headerEntry.getKey(), value);
            }
        }

        return headers;
    }

    private URI buildUri(org.apache.hc.core5.http.protocol.HttpContext context, URI uri) throws IOException {
        try {
            URIBuilder uriBuilder = new URIBuilder(uri);
            HttpHost host = (HttpHost)context.getAttribute("http.target_host");
            if (host != null) {
                uriBuilder.setHost(host.getHostName());
                uriBuilder.setScheme(host.getSchemeName());
                uriBuilder.setPort(host.getPort());
            }

            return uriBuilder.build();
        } catch (URISyntaxException ex) {
            throw new IOException("Invalid URI", ex);
        }
    }

    @SneakyThrows
    @Override
    public void process(org.apache.hc.core5.http.HttpRequest httpRequest, EntityDetails entityDetails, org.apache.hc.core5.http.protocol.HttpContext httpContext) throws org.apache.hc.core5.http.HttpException, IOException {
        URI requestUri = buildUri(httpContext, httpRequest.getUri());
        SdkHttpFullRequest.Builder requestBuilder = SdkHttpFullRequest.builder().method(SdkHttpMethod.fromValue(httpRequest.getMethod())).uri(requestUri);
        if (httpRequest instanceof HttpEntityEnclosingRequest httpEntityEnclosingRequest) {
            if (httpEntityEnclosingRequest.getEntity() != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                httpEntityEnclosingRequest.getEntity().writeTo(outputStream);
                if (!httpEntityEnclosingRequest.getEntity().isRepeatable()) {
                    BasicHttpEntity entity = new BasicHttpEntity();
                    entity.setContent(new ByteArrayInputStream(outputStream.toByteArray()));
                    httpEntityEnclosingRequest.setEntity(new BufferedHttpEntity(entity));
                }

                requestBuilder.contentStreamProvider(() -> new ByteArrayInputStream(outputStream.toByteArray()));
            }
        }

        Map<String, List<String>> headers = headerArrayToMap(httpRequest.getHeaders());
        headers.put("x-amz-content-sha256", Collections.singletonList("required"));
        requestBuilder.headers(headers);
        SdkHttpFullRequest signedRequest = this.signer.signRequest(requestBuilder.build());
        httpRequest.setHeaders(mapToHeaderArray(signedRequest.headers()));
    }
}
