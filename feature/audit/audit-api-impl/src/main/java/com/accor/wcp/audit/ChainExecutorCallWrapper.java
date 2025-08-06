package com.accor.wcp.audit;

import java.util.Map;

/**
 * Chain interface to wrap call, run of async task of an ExecutorService.
 */
public interface ChainExecutorCallWrapper {
    /**
     * Initialize context with current (=parent) thread
     * @param context wrap context, shared between 2 threads
     */
    void initContext(Map<Object, Object> context);

    /**
     * Initialize new thread data from context
     * @param context wrap context, shared between 2 threads
     */
    void beforeExecution(Map<Object, Object> context);

    /**
     * Release execution thread data
     * @param context wrap context, shared between 2 threads
     */
    void releaseAfterExecution(Map<Object, Object> context);

}
