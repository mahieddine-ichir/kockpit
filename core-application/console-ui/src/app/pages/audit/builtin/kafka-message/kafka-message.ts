export interface KafkaMessage {
  source: String;
  key: String;
  topic: String;
  payload: String;
  keyClassname: String;
  payloadClassname: String;
  partition: number;
  timestamp: number;
  serializedKeySize: number;
  serializedValueSize: number;
  offset: number;
  headers: Map<String, String>;
  isJson: boolean;
  isXml: boolean;
  isString: boolean;
}
