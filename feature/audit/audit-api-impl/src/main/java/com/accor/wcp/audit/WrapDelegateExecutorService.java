package com.accor.wcp.audit;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Delegate {@link ExecutorService } to propagate data from current
 * thread to child thread (or initialize data to child thread).
 */
public class WrapDelegateExecutorService implements ExecutorService {
    private final ExecutorService executorService;
    private final List<ChainExecutorCallWrapper> chainExecutorCallWrappers;

    public WrapDelegateExecutorService(ExecutorService executorService, ChainExecutorCallWrapper chainExecutorCallWrapper) {
        this.executorService = executorService;
        this.chainExecutorCallWrappers = List.of(chainExecutorCallWrapper);
    }

    public WrapDelegateExecutorService(ExecutorService executorService, List<ChainExecutorCallWrapper> chainExecutorCallWrappers) {
        this.executorService = executorService;
        this.chainExecutorCallWrappers = chainExecutorCallWrappers;
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(this.wrap(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executorService.submit(this.wrap(task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executorService.submit(this.wrap(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        return executorService.invokeAll(tasks.stream().map(this::wrap).toList());
    }

    @Override
    public <T> List<Future<T>> invokeAll(
            Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return executorService.invokeAll(tasks.stream().map(this::wrap).toList(), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        return executorService.invokeAny(tasks.stream().map(this::wrap).toList());
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return executorService.invokeAny(tasks.stream().map(this::wrap).toList(), timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        executorService.execute(this.wrap(command));
    }

    protected <T> Callable<T> wrap(Callable<T> task) {
        final Map<Object, Object> context = new HashMap<>();
        chainExecutorCallWrappers.forEach(w -> w.initContext(context));
        return () -> {
            chainExecutorCallWrappers.forEach(w -> w.beforeExecution(context));
            T call;
            try {
                call = task.call();
                return call;
            } finally {
                chainExecutorCallWrappers.forEach(w -> w.releaseAfterExecution(context));
            }
        };
    }

    protected Runnable wrap(Runnable task) {
        // Wrap runnable in a Callable (not to duplicate code)
        Callable<Object> wrap = wrap(() -> {
            task.run();
            return null;
        });
        return () -> {
            try {
                // No need to get a result (always = null)
                wrap.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
