/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The AWS Request Signing Interceptor Contributors require
 * contributions made to this file be licensed under the
 * Apache-2.0 license or a compatible open source license.
 */

package org.kockpit.aws.opensearch;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignedRequest;
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity;
import software.amazon.awssdk.regions.Region;

@RequiredArgsConstructor
class RequestSigner {
    /**
     * A service the client is connecting to.
     */
    private final String service;
    /**
     * A signer implementation.
     */
    private final HttpSigner<AwsCredentialsIdentity> signer;
    /**
     * The source of AWS credentials for signing.
     */
    private final AwsCredentialsProvider awsCredentialsProvider;
    /**
     * The signing region.
     */
    private final Region region;

    /**
     * Signs the {@code request} using
     * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-header-based-auth.html">
     * AWS Signature Version 4</a>.
     *
     * @param request to be signed
     * @return signed request
     * @see AwsV4HttpSigner#sign
     */
    SignedRequest signRequest(SdkHttpFullRequest request) {
        return signer.sign(r -> r.identity(awsCredentialsProvider.resolveCredentials())
                .request(request)
                .payload(request.contentStreamProvider().orElse(null))
                .putProperty(AwsV4HttpSigner.SERVICE_SIGNING_NAME, service)
                .putProperty(AwsV4HttpSigner.REGION_NAME, region.id()));
    }
}
