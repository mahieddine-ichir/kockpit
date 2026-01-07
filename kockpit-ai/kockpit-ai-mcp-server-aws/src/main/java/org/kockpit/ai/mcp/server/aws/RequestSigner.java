package org.kockpit.ai.mcp.server.aws;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignRequest;
import software.amazon.awssdk.http.auth.spi.signer.SignedRequest;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;
import software.amazon.awssdk.regions.Region;

@RequiredArgsConstructor
class RequestSigner {

    private final AwsV4HttpSigner signer;
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final String serviceName;
    private final Region region;

    SdkHttpFullRequest signRequest(SdkHttpFullRequest request) {
        // AwsCredentials already implements AwsCredentialsIdentity
        AwsCredentialsIdentity credentials = awsCredentialsProvider.resolveCredentials();

        SignRequest<AwsCredentialsIdentity> identitySignRequest = SignRequest.builder(credentials)
                .request(request)
                .putProperty(AwsV4HttpSigner.SERVICE_SIGNING_NAME, serviceName)
                .putProperty(AwsV4HttpSigner.REGION_NAME, region.id())
                .build();

        SignedRequest signedRequest = this.signer.sign(identitySignRequest);
        return (SdkHttpFullRequest) signedRequest.request();
    }
}
