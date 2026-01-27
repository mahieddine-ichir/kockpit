package org.kockpit.audit.stream.kinesis.coordination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShardLease {

    private String shardId;
    private String applicationName;
    private String workerId;
    private Instant leaseExpiry;
    private Instant lastHeartbeat;
    private String sequenceNumber;
    private Long leaseCounter;

    public boolean isExpired() {
        return leaseExpiry != null && Instant.now().isAfter(leaseExpiry);
    }

    public boolean isOwnedBy(String applicationName, String workerId) {
        return this.applicationName.equals(applicationName) && this.workerId.equals(workerId);
    }
}