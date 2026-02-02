package org.kockpit.ai.mcp.server.signing;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.apache.hc.core5.http.io.entity.BufferedHttpEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.auth.spi.signer.SignedRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static software.amazon.awssdk.http.ContentStreamProvider.fromInputStreamSupplier;
import static software.amazon.awssdk.http.auth.aws.signer.SignerConstant.X_AMZ_CONTENT_SHA256;

@RequiredArgsConstructor
public final class AwsRequestSigningApacheV5Interceptor implements ExecChainHandler, AsyncExecChainHandler {

    private final RequestSigner signer;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(HttpRequest request,
                        AsyncEntityProducer entityProducer,
                        AsyncExecChain.Scope scope,
                        AsyncExecChain execChain,
                        AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
        signRequest(request, getContentStreamSupplier(scope.originalRequest));
        execChain.proceed(request, entityProducer, scope, asyncExecCallback);
    }

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest classicHttpRequest,
                                       ExecChain.Scope scope,
                                       ExecChain execChain) throws IOException, HttpException {
        signRequest(classicHttpRequest, getContentStreamSupplier(classicHttpRequest));
        return execChain.proceed(classicHttpRequest, scope);
    }

    private void signRequest(HttpRequest request,
                             Supplier<InputStream> contentStreamSupplier) throws IOException {
        // copy Apache HttpRequest to AWS request
        SdkHttpFullRequest.Builder requestBuilder = SdkHttpFullRequest.builder()
                .method(SdkHttpMethod.fromValue(request.getMethod()))
                .uri(buildUri(request));

        if (contentStreamSupplier != null) {
            requestBuilder.contentStreamProvider(fromInputStreamSupplier(contentStreamSupplier));
        }

        Map<String, List<String>> headers = headerArrayToMap(request.getHeaders());
        // adds a hash of the request payload when signing
        headers.put(X_AMZ_CONTENT_SHA256, singletonList("required"));
        requestBuilder.headers(headers);
        SignedRequest signedRequest = signer.signRequest(requestBuilder.build());

        // copy everything back
        request.setHeaders(mapToHeaderArray(signedRequest.request().headers()));
    }

    private static Supplier<InputStream> getContentStreamSupplier(HttpRequest request) throws IOException {
        if (request instanceof ClassicHttpRequest classicHttpRequest) {
            if (classicHttpRequest.getEntity() == null) {
                return null;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            classicHttpRequest.getEntity().writeTo(outputStream);
            if (!classicHttpRequest.getEntity().isRepeatable()) {
                // copy back the entity, so it can be read again
                BasicHttpEntity entity = new BasicHttpEntity(new ByteArrayInputStream(outputStream.toByteArray()),
                        ContentType.parse(classicHttpRequest.getEntity().getContentType()));
                // wrap into repeatable entity to support retries
                classicHttpRequest.setEntity(new BufferedHttpEntity(entity));
            }
            return () -> new ByteArrayInputStream(outputStream.toByteArray());
        } else if (request instanceof SimpleHttpRequest simpleHttpRequest) {
            if (simpleHttpRequest.getBody() == null) {
                return null;
            }
            return () -> new ByteArrayInputStream(simpleHttpRequest.getBodyBytes());
        }

        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    private static URI buildUri(HttpRequest request) throws IOException {
        try {
            return request.getUri();
        } catch (URISyntaxException ex) {
            throw new IOException("Invalid URI", ex);
        }
    }

    private static Map<String, List<String>> headerArrayToMap(Header[] headers) {
        Map<String, List<String>> headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Header header : headers) {
            if (!skipHeader(header)) {
                headersMap.put(header.getName(), headersMap.getOrDefault(header.getName(),
                        new LinkedList<>(singletonList(header.getValue()))));
            }
        }
        return headersMap;
    }

    private static boolean skipHeader(Header header) {
        return (HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(header.getName())
                && "0".equals(header.getValue())) // Strip Content-Length: 0
                || HttpHeaders.HOST.equalsIgnoreCase(header.getName()); // Host comes from endpoint
    }

    private static Header[] mapToHeaderArray(Map<String, List<String>> mapHeaders) {
        Header[] headers = new Header[mapHeaders.size()];
        int i = 0;
        for (Map.Entry<String, List<String>> headerEntry : mapHeaders.entrySet()) {
            for (String value : headerEntry.getValue()) {
                headers[i++] = new BasicHeader(headerEntry.getKey(), value);
            }
        }
        return headers;
    }
}
