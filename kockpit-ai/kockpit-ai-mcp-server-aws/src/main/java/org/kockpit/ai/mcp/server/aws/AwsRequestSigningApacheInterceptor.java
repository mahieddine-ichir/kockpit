package org.kockpit.ai.mcp.server.aws;

import lombok.RequiredArgsConstructor;
import org.apache.http.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@RequiredArgsConstructor
public class AwsRequestSigningApacheInterceptor implements HttpRequestInterceptor {

    private final RequestSigner signer;

    public void process(HttpRequest request, HttpContext context) throws IOException {
        URI requestUri = buildUri(context, request.getRequestLine().getUri());
        SdkHttpFullRequest.Builder requestBuilder = SdkHttpFullRequest.builder().method(SdkHttpMethod.fromValue(request.getRequestLine().getMethod())).uri(requestUri);
        if (request instanceof HttpEntityEnclosingRequest httpEntityEnclosingRequest) {
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

        Map<String, List<String>> headers = headerArrayToMap(request.getAllHeaders());
        headers.put("x-amz-content-sha256", Collections.singletonList("required"));
        requestBuilder.headers(headers);
        SdkHttpFullRequest signedRequest = this.signer.signRequest(requestBuilder.build());
        request.setHeaders(mapToHeaderArray(signedRequest.headers()));
    }

    private static Map<String, List<String>> headerArrayToMap(Header[] headers) {
        Map<String, List<String>> headersMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);

        for(Header header : headers) {
            if (!skipHeader(header)) {
                headersMap.put(header.getName(), headersMap.getOrDefault(header.getName(), new LinkedList(Collections.singletonList(header.getValue()))));
            }
        }

        return headersMap;
    }

    private static boolean skipHeader(Header header) {
        return "Content-Length".equalsIgnoreCase(header.getName()) && "0".equals(header.getValue()) || "Host".equalsIgnoreCase(header.getName());
    }

    private static Header[] mapToHeaderArray(Map<String, List<String>> mapHeaders) {
        Header[] headers = new Header[mapHeaders.size()];
        int i = 0;

        for(Map.Entry<String, List<String>> headerEntry : mapHeaders.entrySet()) {
            for(String value : headerEntry.getValue()) {
                headers[i++] = new BasicHeader(headerEntry.getKey(), value);
            }
        }

        return headers;
    }

    static URI buildUri(HttpContext context, String uri) throws IOException {
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
}
