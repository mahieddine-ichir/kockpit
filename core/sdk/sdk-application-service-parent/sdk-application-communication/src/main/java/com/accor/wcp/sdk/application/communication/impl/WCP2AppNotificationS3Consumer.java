package com.accor.wcp.sdk.application.communication.impl;

import static com.accor.wcp.sdk.application.communication.impl.AwsStsProviderUtils.roleCredentialsProvider;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.accor.wcp.sdk.application.SdkApplicationProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * Internal SDK Wcp2App message consumer S3 built implementation. It polls S3 folder to find new
 * message(s) to treat.
 */
@Slf4j
class WCP2AppNotificationS3Consumer implements ApplicationListener<ApplicationStartedEvent> {
  private final CommunicationConfigurationProperties communicationConfigurationProperties;
  private final SdkApplicationProperties sdkApplicationProperties;
  private final WCP2AppNotificationProcessor processor;
  private final String bucket;
  private S3Client s3Client;
  private Instant lastConsumption;
  /**
   * At start notification polling interval is setted to 500ms (to quick start / read
   * notifications). Then at {@link #onApplicationEvent(ApplicationStartedEvent)}, interval is
   * setted back to configuration.
   */
  private int notificationPollingIntervalMs = 500;

  private boolean runningFlag;

  WCP2AppNotificationS3Consumer(
      CommunicationConfigurationProperties communicationConfigurationProperties,
      SdkApplicationProperties sdkApplicationProperties,
      WCP2AppNotificationProcessor processor) {
    this.communicationConfigurationProperties = communicationConfigurationProperties;
    this.sdkApplicationProperties = sdkApplicationProperties;
    this.processor = processor;
    // Initialize lastConsumption with previous second
    this.lastConsumption = Instant.now().minus(1, ChronoUnit.SECONDS);
    this.bucket = communicationConfigurationProperties.getWcp2appBucket();
  }

  @PostConstruct
  void init() {
    initS3Client();

    log.info(
        "WCP SDK - Start consuming notifications from configuration : {}",
        communicationConfigurationProperties);

    initPollingThread();
  }

  @PreDestroy
  void preDestroy() {
    log.info("Stopping Notification Consumer");
    this.runningFlag = false;
  }

  private void initPollingThread() {
    this.runningFlag = true;
    CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(this::consume);
    completableFuture.whenComplete((unused,throwable) -> log.info("Polling S3 Consumer should stop only when application stops! throwable: {}", (Object) throwable));
  }

  private void initS3Client() {
    if (isNotEmpty(communicationConfigurationProperties.getKinesisEndpoint())) {
      s3Client =
          S3Client.builder()
              .region(Region.EU_WEST_1)
              .endpointOverride(
                  URI.create(communicationConfigurationProperties.getKinesisEndpoint()))
              .httpClientBuilder(ApacheHttpClient.builder())
              .forcePathStyle(true)
              .build();
    } else {
      // Normal AWS env
      AwsCredentialsProvider kinesisCredentialProvider = getCredentialProvider();
      Region region = Region.EU_WEST_1;
      s3Client =
          S3Client.builder()
              .region(region)
              .credentialsProvider(kinesisCredentialProvider)
              .httpClientBuilder(ApacheHttpClient.builder())
              .build();
    }
  }

  private void consume() {
    Thread.currentThread().setName("wcpsdk-comm-wcp2app");

    try {
      while (runningFlag) {
        doConsumeAndTreat();
      }
    } catch (InterruptedException e) {
      // Stopping
      log.info("Interrupting WCP2APP communication.", e);
    }
  }

  private void doConsumeAndTreat() throws InterruptedException {
    String prefix = getPrefix();
    ListObjectsRequest request =
            ListObjectsRequest.builder().bucket(bucket).prefix(prefix).build();
    log.debug("Consume on s3 with prefix: {}", prefix);

    List<S3Object> s3Objects = null;
    try {
      List<S3Object> s3ObjectList = s3Client.listObjects(request).contents();

      s3Objects = s3ObjectList.stream().filter(this::isNewer).toList();

      // Nothing
      if (!s3Objects.isEmpty()) {
        lastConsumption = Instant.now();
        log.debug("Filtered s3Objects to treat: {}", s3Objects);
      }

      // Treat
      treatNewNotifications(s3Objects);

      Thread.sleep(notificationPollingIntervalMs);
    } catch (SdkClientException e) {
      log.error("Error managing  WCP2APP notification communication. Stopping consume loop", e);
    } catch (InterruptedException e) {
      log.error("Error InterruptedException: objects: {}, exception:{}", s3Objects, e, e);
      throw e;
    } catch (Exception e) {
      log.error("Error consuming / treating notification(s): {}", s3Objects);
    }
  }

  private String getPrefix() {
    String domain = sdkApplicationProperties.getDomain();
    String applicationId = sdkApplicationProperties.getApplicationId();
    String applicationEnv = sdkApplicationProperties.getApplicationEnv();
    // Create prefix (domain/env)
    String prefix = domain + "/" + applicationEnv;
    if (nonNull(applicationId)) {
      prefix += "/" + applicationId;
    } else {
      prefix += "/all";
    }
    prefix += "/";
    return prefix;
  }

  private boolean isNewer(S3Object s3Object) {
    return s3Object.lastModified().isAfter(lastConsumption);
  }

  private void treatNewNotifications(List<S3Object> objects) {
    for (S3Object s3Object : objects) {
      // Treat
      try {
        ResponseBytes<GetObjectResponse> s3ClientObject =
            s3Client.getObject(
                GetObjectRequest.builder().bucket(bucket).key(s3Object.key()).build(),
                ResponseTransformer.toBytes());
        processor.process(s3Object, s3ClientObject.asByteArray());
      } catch (IOException e) {
        log.warn("Error processing WCP2APP I/O notification: {}", s3Object, e);
      } catch (Exception e) {
        log.warn("Error processing WCP2APP notification: {}", s3Object, e);
      }
    }
  }

  protected AwsCredentialsProvider getCredentialProvider() {
    String roleSessionName = "wcp-sdk-application-session-" + Math.random();
    return roleCredentialsProvider(
        communicationConfigurationProperties.getKinesisAssumeRoleName(),
        roleSessionName,
        Region.of(communicationConfigurationProperties.getAwsRegion()));
  }

  @Override
  public void onApplicationEvent(ApplicationStartedEvent event) {
    // After application started, set the real notification polling interval
    notificationPollingIntervalMs =
        communicationConfigurationProperties.getNotificationPollingIntervalMs();
  }
}
