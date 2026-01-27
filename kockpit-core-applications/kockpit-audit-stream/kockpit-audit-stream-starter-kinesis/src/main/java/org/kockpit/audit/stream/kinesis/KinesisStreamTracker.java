package org.kockpit.audit.stream.kinesis;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.arns.Arn;
import software.amazon.kinesis.common.InitialPositionInStream;
import software.amazon.kinesis.common.InitialPositionInStreamExtended;
import software.amazon.kinesis.common.StreamConfig;
import software.amazon.kinesis.common.StreamIdentifier;
import software.amazon.kinesis.processor.FormerStreamsLeasesDeletionStrategy;
import software.amazon.kinesis.processor.MultiStreamTracker;

import java.util.List;

@RequiredArgsConstructor
public class KinesisStreamTracker implements MultiStreamTracker {

    private final String streamName;

    @Override
    public List<StreamConfig> streamConfigList() {
        StreamIdentifier stream1 = StreamIdentifier.singleStreamInstance(streamName);

        InitialPositionInStreamExtended position1 =
                InitialPositionInStreamExtended.newInitialPosition(
                        InitialPositionInStream.TRIM_HORIZON
                );

        return List.of(new StreamConfig(stream1, position1));
    }

    @Override
    public FormerStreamsLeasesDeletionStrategy formerStreamsLeasesDeletionStrategy() {
        return new FormerStreamsLeasesDeletionStrategy.NoLeaseDeletionStrategy();
    }

    @Override
    public InitialPositionInStreamExtended orphanedStreamInitialPositionInStream() {
        // Position for streams no longer in streamConfigList
        return InitialPositionInStreamExtended.newInitialPosition(InitialPositionInStream.LATEST);
    }
}
