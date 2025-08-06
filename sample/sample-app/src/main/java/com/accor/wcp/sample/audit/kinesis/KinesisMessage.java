package com.accor.wcp.sample.audit.kinesis;

public class KinesisMessage {

    private String partitionkey;
    private String payload;

    public KinesisMessage(String partitionkey, String payload) {
        this.partitionkey = partitionkey;
        this.payload = payload;
    }

    public String getPartitionkey() {
        return partitionkey;
    }

    public String getPayload() {
        return payload;
    }
}
