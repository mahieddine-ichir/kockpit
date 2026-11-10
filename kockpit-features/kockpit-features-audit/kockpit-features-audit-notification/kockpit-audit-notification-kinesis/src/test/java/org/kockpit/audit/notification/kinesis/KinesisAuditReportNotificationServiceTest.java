package org.kockpit.audit.notification.kinesis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kockpit.audit.api.AuditReportWrapper;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResultEntry;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KinesisAuditReportNotificationServiceTest {

    @Mock KinesisAsyncClient kinesisAsyncClient;
    @Mock RecordTransformer recordTransformer;
    @Mock AuditReportWrapper first;
    @Mock AuditReportWrapper second;

    @BeforeEach
    void setUp() {
        when(recordTransformer.apply(first)).thenReturn(entry("first"));
        when(recordTransformer.apply(second)).thenReturn(entry("second"));
    }

    @Test
    void should_send_once_when_every_record_is_accepted() {
        when(kinesisAsyncClient.putRecords(any(PutRecordsRequest.class))).thenReturn(completed(accepted(), accepted()));

        withRetries(3).notify(List.of(first, second));

        verify(kinesisAsyncClient, times(1)).putRecords(any(PutRecordsRequest.class));
    }

    @Test
    void should_retry_only_the_records_rejected_by_kinesis() {
        when(kinesisAsyncClient.putRecords(any(PutRecordsRequest.class)))
                .thenReturn(completed(accepted(), throttled()))
                .thenReturn(completed(accepted()));

        withRetries(3).notify(List.of(first, second));

        ArgumentCaptor<PutRecordsRequest> requests = ArgumentCaptor.forClass(PutRecordsRequest.class);
        verify(kinesisAsyncClient, times(2)).putRecords(requests.capture());
        assertThat(requests.getAllValues().get(0).records()).hasSize(2);
        assertThat(requests.getAllValues().get(1).records())
                .singleElement()
                .satisfies(entry -> assertThat(entry.data().asUtf8String()).isEqualTo("second"));
    }

    @Test
    void should_stop_retrying_after_the_configured_number_of_retries() {
        when(kinesisAsyncClient.putRecords(any(PutRecordsRequest.class))).thenReturn(completed(throttled()));

        withRetries(2).notify(List.of(first));

        verify(kinesisAsyncClient, times(3)).putRecords(any(PutRecordsRequest.class));
    }

    @Test
    void should_not_retry_when_retries_are_disabled() {
        when(kinesisAsyncClient.putRecords(any(PutRecordsRequest.class))).thenReturn(completed(throttled()));

        withRetries(0).notify(List.of(first));

        verify(kinesisAsyncClient, times(1)).putRecords(any(PutRecordsRequest.class));
    }

    @Test
    void should_not_retry_a_failed_request() {
        when(kinesisAsyncClient.putRecords(any(PutRecordsRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("request failed")));

        withRetries(3).notify(List.of(first));

        verify(kinesisAsyncClient, times(1)).putRecords(any(PutRecordsRequest.class));
    }

    @Test
    void should_stop_and_keep_the_interrupt_flag_when_interrupted_while_backing_off() {
        when(kinesisAsyncClient.putRecords(any(PutRecordsRequest.class))).thenReturn(completed(throttled()));
        KinesisAuditReportNotificationService underTest = new KinesisAuditReportNotificationService(
                kinesisAsyncClient, "streamName", recordTransformer, 3, Duration.ofSeconds(30));

        Thread.currentThread().interrupt();
        try {
            underTest.notify(List.of(first));
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
        verify(kinesisAsyncClient, times(1)).putRecords(any(PutRecordsRequest.class));
    }

    private KinesisAuditReportNotificationService withRetries(int maxRetries) {
        return new KinesisAuditReportNotificationService(
                kinesisAsyncClient, "streamName", recordTransformer, maxRetries, Duration.ZERO);
    }

    private static PutRecordsRequestEntry entry(String data) {
        return PutRecordsRequestEntry.builder().partitionKey("key").data(SdkBytes.fromUtf8String(data)).build();
    }

    private static CompletableFuture<PutRecordsResponse> completed(PutRecordsResultEntry... results) {
        int failed = (int) Arrays.stream(results).filter(result -> result.errorCode() != null).count();
        return CompletableFuture.completedFuture(
                PutRecordsResponse.builder().failedRecordCount(failed).records(results).build());
    }

    private static PutRecordsResultEntry accepted() {
        return PutRecordsResultEntry.builder().shardId("shardId-000000000000").sequenceNumber("1").build();
    }

    private static PutRecordsResultEntry throttled() {
        return PutRecordsResultEntry.builder()
                .errorCode("ProvisionedThroughputExceededException")
                .errorMessage("Rate exceeded")
                .build();
    }
}
