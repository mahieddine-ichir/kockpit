/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The AWS Request Signing Interceptor Contributors require
 * contributions made to this file be licensed under the
 * Apache-2.0 license or a compatible open source license.
 */

package io.github.acm19.aws.interceptor.http;

import java.util.Objects;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.signer.AwsSignerExecutionAttribute;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.signer.Signer;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.regions.Region;

class RequestSigner {
  /** A service the client is connecting to. */
  private final String service;
  /** A signer implementation. */
  private final Signer signer;
  /** The source of AWS credentials for signing. */
  private final AwsCredentialsProvider awsCredentialsProvider;
  /** The signing region. */
  private final Region region;

  /**
   * @param service
   * @param signer
   * @param awsCredentialsProvider
   * @param region
   */
  RequestSigner(
      String service, Signer signer, AwsCredentialsProvider awsCredentialsProvider, Region region) {
    this.service = service;
    this.signer = signer;
    this.awsCredentialsProvider = awsCredentialsProvider;
    this.region = Objects.requireNonNull(region);
  }

  /**
   * Signs the {@code request} using <a
   * href="https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-header-based-auth.html">AWS
   * Signature Version 4</a>.
   *
   * @param request to be signed
   * @return signed request
   * @see Signer#sign
   */
  SdkHttpFullRequest signRequest(SdkHttpFullRequest request) {
    ExecutionAttributes attributes = new ExecutionAttributes();
    attributes.putAttribute(
        AwsSignerExecutionAttribute.AWS_CREDENTIALS, awsCredentialsProvider.resolveCredentials());
    attributes.putAttribute(AwsSignerExecutionAttribute.SERVICE_SIGNING_NAME, service);
    attributes.putAttribute(AwsSignerExecutionAttribute.SIGNING_REGION, region);

    // sign it
    return signer.sign(request, attributes);
  }
}
