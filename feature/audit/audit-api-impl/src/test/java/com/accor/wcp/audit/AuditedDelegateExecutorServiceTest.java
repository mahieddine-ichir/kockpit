package com.accor.wcp.audit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class AuditedDelegateExecutorServiceTest {

  private AuditedDelegateExecutorService underTest;

  ThreadPoolExecutor executor;

  @BeforeEach
  void init() {
    executor = new ThreadPoolExecutor(3, 6, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    underTest = new AuditedDelegateExecutorService(executor);
  }

  @AfterEach
  void clear() {
    underTest.shutdownNow();
    underTest.isShutdown();
    underTest.isTerminated();
    executor = null;
  }

  @Test
  void should_execute_normally_with_audit_propagation()
      throws InterruptedException, ExecutionException, TimeoutException {
    // Given
    final AuditReport mainThreadAuditReport = AuditReport.builder().build();
    AuditReportContainer.setAuditReport(mainThreadAuditReport);
    final IndexedKeyValue indexedKeyValue =
        IndexedKeyValue.builder().key("fakeKey").value("fakeVal").build();
    assertThat(mainThreadAuditReport.getIndexedKeyValues()).isEmpty();

    // When
    Runnable runnable =
        () -> {
          // Execution in child thread
          log.info("Execution in child thread : {}", Thread.currentThread().getName());
          assertEquals(mainThreadAuditReport, AuditReportContainer.getAuditReport());
          AuditReportContainer.getAuditReport().getIndexedKeyValues().add(indexedKeyValue);
          log.info("<<Execution in child thread : {}", Thread.currentThread().getName());
        };
    Callable<String> callable =
        () -> {
          // Execution in child thread
          log.info("Execution in child thread : {}", Thread.currentThread().getName());
          assertEquals(mainThreadAuditReport, AuditReportContainer.getAuditReport());
          AuditReportContainer.getAuditReport().getIndexedKeyValues().add(indexedKeyValue);
          log.info("<< Execution in child thread : {}", Thread.currentThread().getName());
          return "OK";
        };
    underTest.execute(runnable);
    underTest.submit(runnable);
    underTest.submit(callable);
    underTest.submit(runnable, "OK");
    underTest.invokeAll(Arrays.asList(callable, callable, callable));
    underTest.invokeAll(Arrays.asList(callable, callable), 1, TimeUnit.SECONDS);
    underTest.invokeAny(Arrays.asList(callable));
    underTest.invokeAny(Arrays.asList(callable), 1, TimeUnit.SECONDS);

    // Then
    underTest.awaitTermination(1, TimeUnit.SECONDS);
    assertThat(mainThreadAuditReport.getIndexedKeyValues()).hasSize(11);
  }
}
