export interface Queue {
    name: string;
    dlq: string;
    url: string;
    label: string;
    route: string;
    deleteWhenReplay:boolean;
    resultColumns: QueueColumn[];
}

export interface QueueColumn {
    name: string;
    label: string;
    limitDisplay: number;
}


export interface Messages {
  nbMessagesByStatus: Map<string, number>;
  messages: Message[];
}

export interface Count {
  totalCount: number;
}

export interface Message {
    retries: MessageRetry[];
    id: string;
    groupId: string;
    attributes: Attribute[];
    sentTimestamp: number;
    body: string;
    comment: string;
    status: string;
}

export interface MessageRetry {
  groupId: string;
  attributes: Attribute[];
  sentTimestamp: number;
  receiveTime: number;
  body: string;
  status: string;
}

export interface SendMessageRetry {
  parentId: string;
  retry: MessageRetry;
}

export interface Attribute {
  name: string;
  value: string;
  type: string;
}



export interface SendMessagesRetry{
  retries : SendMessageRetry[]
}

