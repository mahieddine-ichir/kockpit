export interface SqsMessage {
  queueUrl: string;
  issue: string;
  payload: string;
  messageId: string;
  groupId: string;
  deduplicationId: string;
  attributes: Map<string, string>;
}
