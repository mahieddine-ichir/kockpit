package org.kockpit.audit.stream.kinesis.coordination;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class DynamoDbShardCoordinator {

    private final DynamoDbAsyncClient dynamoDbClient;
    private final String tableName;
    private final String applicationName;
    private final String workerId;
    private final Duration leaseDuration;

    public CompletableFuture<Void> ensureTableExists() {
        return dynamoDbClient.describeTable(DescribeTableRequest.builder()
                .tableName(tableName)
                .build())
                .thenApply(response -> {
                    log.info("✅ DynamoDB table already exists: {}", tableName);
                    return (Void) null;
                })
                .exceptionallyCompose(throwable -> {
                    log.info("✅ Creating DynamoDB table: {}", tableName);
                    return createTable();
                });
    }

    private CompletableFuture<Void> createTable() {
        CreateTableRequest createRequest = CreateTableRequest.builder()
                .tableName(tableName)
                .keySchema(KeySchemaElement.builder()
                        .attributeName("shardId")
                        .keyType(KeyType.HASH)
                        .build())
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("shardId")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        return dynamoDbClient.createTable(createRequest)
                .thenRun(() -> log.info("✅ Created DynamoDB table: {}", tableName))
                .exceptionally(throwable -> {
                    log.error("❌ Failed to create DynamoDB table: {}", tableName, throwable);
                    throw new RuntimeException("Failed to create coordination table", throwable);
                });
    }

    public CompletableFuture<List<String>> acquireAvailableShards(List<String> allShardIds) {
        List<CompletableFuture<String>> acquisitionFutures = allShardIds.stream()
                .map(this::tryAcquireShard)
                .collect(Collectors.toList());

        return CompletableFuture.allOf(acquisitionFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> acquisitionFutures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

    private CompletableFuture<String> tryAcquireShard(String shardId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(leaseDuration);

        Map<String, AttributeValue> item = Map.of(
                "shardId", AttributeValue.builder().s(shardId).build(),
                "applicationName", AttributeValue.builder().s(applicationName).build(),
                "workerId", AttributeValue.builder().s(workerId).build(),
                "leaseExpiry", AttributeValue.builder().s(expiry.toString()).build(),
                "lastHeartbeat", AttributeValue.builder().s(now.toString()).build(),
                "leaseCounter", AttributeValue.builder().n("1").build()
        );

        // Try to acquire lease with conditional check
        PutItemRequest putRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .conditionExpression("attribute_not_exists(shardId) OR leaseExpiry < :now")
                .expressionAttributeValues(Map.of(
                        ":now", AttributeValue.builder().s(now.toString()).build()
                ))
                .build();

        return dynamoDbClient.putItem(putRequest)
                .thenApply(response -> {
                    log.debug("✅ Acquired lease for shard: {}", shardId);
                    return shardId;
                })
                .exceptionally(throwable -> {
                    if (throwable.getCause() instanceof ConditionalCheckFailedException) {
                        log.debug("Shard {} is already leased by another worker", shardId);
                    } else {
                        log.warn("⚠️ Failed to acquire lease for shard {}: {}", shardId, throwable.getMessage());
                    }
                    return null;
                });
    }

    public CompletableFuture<Void> renewLease(String shardId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(leaseDuration);

        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("shardId", AttributeValue.builder().s(shardId).build()))
                .updateExpression("SET leaseExpiry = :expiry, lastHeartbeat = :heartbeat, leaseCounter = leaseCounter + :inc")
                .conditionExpression("applicationName = :appName AND workerId = :workerId")
                .expressionAttributeValues(Map.of(
                        ":expiry", AttributeValue.builder().s(expiry.toString()).build(),
                        ":heartbeat", AttributeValue.builder().s(now.toString()).build(),
                        ":appName", AttributeValue.builder().s(applicationName).build(),
                        ":workerId", AttributeValue.builder().s(workerId).build(),
                        ":inc", AttributeValue.builder().n("1").build()
                ))
                .build();

        return dynamoDbClient.updateItem(updateRequest)
                .thenRun(() -> log.debug("✅ Renewed lease for shard: {}", shardId))
                .exceptionally(throwable -> {
                    if (throwable.getCause() instanceof ConditionalCheckFailedException) {
                        log.warn("⚠️ Lost lease ownership for shard: {}", shardId);
                    } else {
                        log.error("❌ Failed to renew lease for shard {}: {}", shardId, throwable.getMessage());
                    }
                    return null;
                });
    }

    public CompletableFuture<Void> releaseLease(String shardId) {
        DeleteItemRequest deleteRequest = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("shardId", AttributeValue.builder().s(shardId).build()))
                .conditionExpression("applicationName = :appName AND workerId = :workerId")
                .expressionAttributeValues(Map.of(
                        ":appName", AttributeValue.builder().s(applicationName).build(),
                        ":workerId", AttributeValue.builder().s(workerId).build()
                ))
                .build();

        return dynamoDbClient.deleteItem(deleteRequest)
                .thenRun(() -> log.info("✅ Released lease for shard: {}", shardId))
                .exceptionally(throwable -> {
                    log.error("❌ Failed to release lease for shard {}: {}", shardId, throwable.getMessage());
                    return null;
                });
    }

    public CompletableFuture<List<ShardLease>> getAllLeases() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();

        return dynamoDbClient.scan(scanRequest)
                .thenApply(response -> response.items().stream()
                        .map(this::mapToShardLease)
                        .collect(Collectors.toList()))
                .exceptionally(throwable -> {
                    log.error("❌ Failed to scan leases: {}", throwable.getMessage());
                    return Collections.emptyList();
                });
    }

    private ShardLease mapToShardLease(Map<String, AttributeValue> item) {
        return ShardLease.builder()
                .shardId(item.get("shardId").s())
                .applicationName(item.get("applicationName").s())
                .workerId(item.get("workerId").s())
                .leaseExpiry(Instant.parse(item.get("leaseExpiry").s()))
                .lastHeartbeat(Instant.parse(item.get("lastHeartbeat").s()))
                .sequenceNumber(item.containsKey("sequenceNumber") ? item.get("sequenceNumber").s() : null)
                .leaseCounter(Long.parseLong(item.get("leaseCounter").n()))
                .build();
    }
}
