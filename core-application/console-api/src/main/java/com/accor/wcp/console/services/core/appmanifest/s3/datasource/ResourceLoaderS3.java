package com.accor.wcp.console.services.core.appmanifest.s3.datasource;

import com.accor.wcp.console.sdk.notification.NotificationLevelType;
import com.accor.wcp.console.sdk.notification.WCPConsoleUserNotificationService;
import com.accor.wcp.console.sdk.notification.impl.DefaultUserNotification;
import com.accor.wcp.console.services.core.appmanifest.manifest.Manifest;
import java.time.Instant;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
@Slf4j
public class ResourceLoaderS3 {
  private final S3Client amazonS3;
  private final String manifestBucketName;
  private final WCPConsoleUserNotificationService userNotificationService;
  private Map<String, String> notifications = new HashMap<>();

  public ResourceLoaderS3(
      S3Client amazonS3,
      @Value("${application.manifest.bucket-name}") String bucketName,
      WCPConsoleUserNotificationService userNotificationService) {
    this.amazonS3 = amazonS3;
    this.manifestBucketName = bucketName;
    this.userNotificationService = userNotificationService;
  }

  public Collection<Manifest> getBucketObjects() {
    List<Manifest> manifests = new ArrayList<>();
    for (S3Object object : this.listBucketObjects()) {
      JSONObject manifestContent = this.retrieveObjectAsJSON(object.key());
      if (manifestContent != null) {
        Manifest manifest =
            new Manifest(object.key(), object.lastModified(), manifestContent, "S3");
        manifests.add(manifest);
      }
    }
    return manifests;
  }

  private JSONObject retrieveObjectAsJSON(String objectKey) {
    try {
      return new JSONObject(new String(retrieveObjectAsInputStream(objectKey).readAllBytes()));
    } catch (Exception e) {
      log.warn(
          "An error occurred while parsing manifest : {} with error = {} ",
          objectKey,
          e.toString());

      DefaultUserNotification notification =
          DefaultUserNotification.builder()
              .applicationId("core")
              .date(new Date())
              .description(e.toString() + " in " + "'" + objectKey + "'")
              .level(NotificationLevelType.WARNING)
              .build();

      if (notifications.containsKey(objectKey)) {
        userNotificationService.update(notifications.get(objectKey), notification);
      } else {
        String notificationId = userNotificationService.create("core", notification);
        notifications.put(objectKey, notificationId);
      }
      return null;
    }
  }

  private ResponseInputStream retrieveObjectAsInputStream(String objectKey) {
    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().bucket(manifestBucketName).key(objectKey).build();
    return amazonS3.getObject(getObjectRequest);
  }

  private List<S3Object> listBucketObjects() {
    ListObjectsRequest listObjectsRequest =
        ListObjectsRequest.builder().bucket(manifestBucketName).build();
    return amazonS3.listObjects(listObjectsRequest).contents();
  }

  public Collection<Manifest> getBucketObjectsWithLastModificationGreaterThan(Instant timestamp) {
    List<Manifest> manifests = new ArrayList<>();
    for (S3Object object : this.listBucketObjectsKeyWithLastModificationGreaterThan(timestamp)) {
      Manifest manifest =
          new Manifest(
              object.key(), object.lastModified(), this.retrieveObjectAsJSON(object.key()), "S3");
      manifests.add(manifest);
    }
    return manifests;
  }

  private List<S3Object> listBucketObjectsKeyWithLastModificationGreaterThan(Instant instant) {
    ListObjectsRequest listObjectsRequest =
        ListObjectsRequest.builder().bucket(manifestBucketName).build();
    return amazonS3.listObjects(listObjectsRequest).contents().stream()
        .filter(s3Object -> s3Object.lastModified().isAfter(instant))
        .toList();
  }
}
