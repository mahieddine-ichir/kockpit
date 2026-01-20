# AWS OpenSearch v3 requests signing
This Spring Boot auto-dependency, exposes (as a Spring `@Bean`) an `AsyncExecChainHandler` that signs Http Requests (Apache http5)
for AWS OpenSearch Service.

This `AsyncExecChainHandler` is intended to be injected to the OpenSearch `RestClientBuilder` as follows:

```java
    @SneakyThrows
    @Bean
    RestHighLevelClient openSearchIndexer(List<AsyncExecChainHandler> handlers) {
        // omitted code: initialize RestClientBuilder
        HttpHost[] httpHosts; // initialize
        RestClientBuilder builder = RestClient.builder(httpHosts);
        
        if (! CollectionUtils.isEmpty(handlers)) {
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                for (AsyncExecChainHandler handler : handlers) {
                    log.info("adding interceptor {}", handler.getClass().getName());
                    httpClientBuilder.addExecInterceptorLast(handler.getClass().getName(), handler);
                }
                return httpClientBuilder;
            });
        }
        return new RestHighLevelClient(builder);
    }
```

_see `AwsSigningRestConfiguration.java`_
