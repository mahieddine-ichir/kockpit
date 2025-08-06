package com.accor.wcp.audit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class AuditedDelegateExecutorTest {

  private AuditedDelegateExecutor underTest;

  ThreadPoolExecutor executor;

  @BeforeEach
  void init() {
    executor = new ThreadPoolExecutor(1, 3, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    underTest = new AuditedDelegateExecutor(executor);
  }

  @AfterEach
  void clear() {
    executor.shutdownNow();
    executor = null;
  }

  @Test
  void should_execute_normally_with_audit_propagation() throws InterruptedException {
    // Given
    final AuditReport mainThreadAuditReport = AuditReport.builder().build();
    AuditReportContainer.setAuditReport(mainThreadAuditReport);
    final IndexedKeyValue indexedKeyValue =
        IndexedKeyValue.builder().key("fakeKey").value("fakeVal").build();
    assertThat(mainThreadAuditReport.getIndexedKeyValues()).isEmpty();

    // When
    underTest.execute(
        () -> {
          // Execution in a child thread
          log.info("Execution in child thread : {}", Thread.currentThread().getName());
          assertEquals(mainThreadAuditReport, AuditReportContainer.getAuditReport());
          AuditReportContainer.getAuditReport().getIndexedKeyValues().add(indexedKeyValue);
        });

    // Then
    executor.awaitTermination(1, TimeUnit.SECONDS);
    assertThat(mainThreadAuditReport.getIndexedKeyValues()).hasSize(1);
  }

  @Test
  void should_not_propagate_audit_without_delegate() throws InterruptedException {
    // Given
    final AuditReport mainThreadAuditReport = AuditReport.builder().build();
    AuditReportContainer.setAuditReport(mainThreadAuditReport);

    // When
    executor.execute(
        () -> {
          assertDoesNotThrow(AuditReportContainer::getAuditReport);
        });

    // Then
    executor.awaitTermination(1, TimeUnit.SECONDS);
    assertThat(mainThreadAuditReport.getIndexedKeyValues()).isEmpty();
  }
}
