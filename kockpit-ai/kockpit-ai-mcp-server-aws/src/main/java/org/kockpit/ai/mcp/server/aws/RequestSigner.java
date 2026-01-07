package org.kockpit.ai.mcp.server.aws;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.signer.AwsSignerExecutionAttribute;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.signer.Signer;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.regions.Region;

@RequiredArgsConstructor
class RequestSigner {
    private final String service;
    private final Signer signer;
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final Region region;

    SdkHttpFullRequest signRequest(SdkHttpFullRequest request) {
        ExecutionAttributes attributes = new ExecutionAttributes();
        attributes.putAttribute(AwsSignerExecutionAttribute.AWS_CREDENTIALS, this.awsCredentialsProvider.resolveCredentials());
        attributes.putAttribute(AwsSignerExecutionAttribute.SERVICE_SIGNING_NAME, this.service);
        attributes.putAttribute(AwsSignerExecutionAttribute.SIGNING_REGION, this.region);
        return this.signer.sign(request, attributes);
    }
}
