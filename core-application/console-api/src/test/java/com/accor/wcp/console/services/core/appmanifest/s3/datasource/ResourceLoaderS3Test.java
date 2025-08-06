package com.accor.wcp.console.services.core.appmanifest.s3.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.accor.wcp.console.sdk.notification.WCPConsoleUserNotificationService;
import com.accor.wcp.console.services.core.appmanifest.manifest.Manifest;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

@ExtendWith(MockitoExtension.class)
class ResourceLoaderS3Test {

  @Mock private S3Client amazonS3;
  private ResourceLoaderS3 underTest;
  @Mock private WCPConsoleUserNotificationService userNotificationService;

  @BeforeEach
  void setUp() {
    underTest = new ResourceLoaderS3(amazonS3, "bucket-name", userNotificationService);
  }

  @Test
  void should_return_list_of_json_object() {
    String resourceContent = "{\"expected\":\"content\"}";

    S3Object s3Object1 = S3Object.builder().key("resource").build();
    ListObjectsResponse objectListing = ListObjectsResponse.builder().contents(s3Object1).build();
    AbortableInputStream abortableInputStream =
        AbortableInputStream.create(
            new ByteArrayInputStream(resourceContent.getBytes(StandardCharsets.UTF_8)));
    ResponseInputStream<GetObjectResponse> responseInputStream =
        new ResponseInputStream(GetObjectResponse.builder().build(), abortableInputStream);

    when(amazonS3.listObjects(ListObjectsRequest.builder().bucket("bucket-name").build()))
        .thenReturn(objectListing);

    when(amazonS3.getObject(
            GetObjectRequest.builder().bucket("bucket-name").key("resource").build()))
        .thenReturn(responseInputStream);

    Collection<Manifest> resources = underTest.getBucketObjects();

    assertThat(resources.size()).isEqualTo(1);
  }

  @Test
  void should_return_empty_list_when_json_content_is_unparsable() {
    String resourceContent = "invalidJSON{\"expected\":\"content\"}";
    S3Object s3Object1 = S3Object.builder().key("resource").build();
    ListObjectsResponse objectListing = ListObjectsResponse.builder().contents(s3Object1).build();
    AbortableInputStream abortableInputStream =
        AbortableInputStream.create(
            new ByteArrayInputStream(resourceContent.getBytes(StandardCharsets.UTF_8)));
    ResponseInputStream<GetObjectResponse> responseInputStream =
        new ResponseInputStream(GetObjectResponse.builder().build(), abortableInputStream);

    when(amazonS3.listObjects(ListObjectsRequest.builder().bucket("bucket-name").build()))
        .thenReturn(objectListing);
    when(amazonS3.getObject(
            GetObjectRequest.builder().bucket("bucket-name").key("resource").build()))
        .thenReturn(responseInputStream);

    Collection<Manifest> resources = underTest.getBucketObjects();

    assertThat(resources.size()).isEqualTo(0);
  }
}
