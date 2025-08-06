export interface KinesisMessage {
  partitionKey: string;
  streamName: string;
  payload: string;
}
