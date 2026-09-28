package org.kockpit.audit.stream.kinesis.efo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.mockito.InOrder;
import software.amazon.kinesis.lifecycle.events.ShutdownRequestedInput;
import software.amazon.kinesis.processor.RecordProcessorCheckpointer;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Consumers buffer accepted records and persist them asynchronously, so on shutdown the shard
 * must only be checkpointed once they've been drained - otherwise the next lease owner resumes
 * past records this worker never wrote.
 */
class AuditRecordProcessorTest {

    private final AuditConsumer first = mock(AuditConsumer.class);
    private final AuditConsumer second = mock(AuditConsumer.class);
    private final RecordProcessorCheckpointer checkpointer = mock(RecordProcessorCheckpointer.class);
    private final AuditRecordProcessor processor = new AuditRecordProcessor(List.of(first, second), 1);

    @Test
    @DisplayName("A l'arret, les consumers sont vides avant le checkpoint du shard")
    void drains_consumers_before_checkpointing_on_shutdown() throws Exception {
        processor.shutdownRequested(ShutdownRequestedInput.builder().checkpointer(checkpointer).build());

        InOrder order = inOrder(first, second, checkpointer);
        order.verify(first).drain();
        order.verify(second).drain();
        order.verify(checkpointer).checkpoint();
    }

    @Test
    @DisplayName("Un consumer en echec n'empeche ni le drain des autres ni le checkpoint")
    void a_failing_drain_does_not_block_the_others_or_the_checkpoint() throws Exception {
        doThrow(new IllegalStateException("boom")).when(first).drain();

        processor.shutdownRequested(ShutdownRequestedInput.builder().checkpointer(checkpointer).build());

        verify(second).drain();
        verify(checkpointer).checkpoint();
    }
}
