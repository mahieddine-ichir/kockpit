package org.kockpit.ai.mcp.server.signing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.regions.Region;

@AutoConfiguration
public class AwsSigningConfiguration {

    @Bean
    AwsRequestSigningApacheV5Interceptor awsSigningRequestInterceptor(
            @Value("${aws.service.name}") String serviceName,
            @Value("${aws.region}") String region
    ) {
        RequestSigner requestSigner = new RequestSigner(serviceName, AwsV4HttpSigner.create(), DefaultCredentialsProvider.builder().build(), Region.of(region));
        return new AwsRequestSigningApacheV5Interceptor(requestSigner);
    }
}
