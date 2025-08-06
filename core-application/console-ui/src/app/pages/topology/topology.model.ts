export class TopologyModel {
  applicationId: string;
  applicationName: string;
  instances: ApplicationInstance[];
}

export class ApplicationInstance {
  instanceId: string;
  lastUpdatedTimestamp: Date;
}
