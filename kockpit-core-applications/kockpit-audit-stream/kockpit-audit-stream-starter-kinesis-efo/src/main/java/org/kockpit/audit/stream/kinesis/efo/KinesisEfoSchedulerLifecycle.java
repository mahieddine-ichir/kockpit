package org.kockpit.audit.stream.kinesis.efo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import software.amazon.kinesis.coordinator.Scheduler;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Runs the KCL {@link Scheduler} on its own daemon thread for the lifetime of the application
 * context, and drives its graceful shutdown (finish in-flight record processing, checkpoint,
 * release leases) when the context closes.
 */
@Slf4j
@RequiredArgsConstructor
class KinesisEfoSchedulerLifecycle implements SmartLifecycle {

    private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(20);

    private final Scheduler scheduler;

    private volatile Thread schedulerThread;

    @Override
    public void start() {
        log.info("✅ Starting Kinesis EFO scheduler");
        schedulerThread = new Thread(scheduler, "kinesis-efo-scheduler");
        schedulerThread.setDaemon(true);
        schedulerThread.start();
    }

    @Override
    public void stop() {
        if (schedulerThread == null) {
            return;
        }
        log.info("🛑 Stopping Kinesis EFO scheduler, waiting up to {}s for graceful shutdown", SHUTDOWN_TIMEOUT.getSeconds());
        Future<Boolean> gracefulShutdownFuture = scheduler.startGracefulShutdown();
        try {
            gracefulShutdownFuture.get(SHUTDOWN_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            log.info("✅ Kinesis EFO scheduler shut down cleanly");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("⚠️ Interrupted while waiting for Kinesis EFO scheduler shutdown");
        } catch (ExecutionException e) {
            log.error("❌ Exception during Kinesis EFO scheduler shutdown: {}", e.getMessage(), e);
        } catch (TimeoutException e) {
            log.error("❌ Timed out waiting for Kinesis EFO scheduler to shut down");
        } finally {
            schedulerThread = null;
        }
    }

    @Override
    public boolean isRunning() {
        return schedulerThread != null && schedulerThread.isAlive();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }
}
