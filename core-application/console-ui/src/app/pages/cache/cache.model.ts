
export interface CacheMetric {
  name : string;
  value : object;
}

export interface InstanceCacheState {
  instanceId : string;
  cacheMetrics : CacheMetric[];
}

export interface CacheState {
  instanceCacheStates : InstanceCacheState[];
  columns: string[];
}

export interface CacheCommands {
  cacheCommands : CacheCommand[];
}
export interface CacheCommand {
  id : string;
  dateTime : Date;
  command : string;
  cacheInstanceOperationResults : CacheInstanceOperationResult[]
}
export interface CacheInstanceOperationResult{
  instanceId: string;
  dateTime : Date;
  result : string;
}
