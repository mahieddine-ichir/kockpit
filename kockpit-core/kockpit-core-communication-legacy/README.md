# kockpit-core-communication-legacy — DynaConfig Message Topology

This module defines the legacy message DTOs and publisher used by the DynaConfig feature to communicate between a monitored **Application** and the **Kockpit Console**.

---

## Message Types

| DTO | Direction | Purpose |
|-----|-----------|---------|
| `InstanceInitPropertiesMessageDto` | App → Console | Startup: sends all current property values |
| `PropertiesRefreshRequestMessageDto` | Console → App | Request a re-send of current values |
| `PropertyUpdateMessageRequestDto` | Console → App | Update a single property |
| `InstanceInitPropertiesUpdateRequestDto` | Console → App | Batch update multiple properties |
| `PropertyUpdateMessageResponseDto` | App → Console | Result of a single update (`SENT`/`ACKED`/`DONE`/`ERROR`) |
| `InstanceInitPropertiesUpdateResponseDto` | App → Console | Batch result wrapping multiple responses |

---

## Startup Flow

```
APPLICATION boots
  │
  ├─ @PostConstruct DynaConfigApplicationServiceIntegration
  │     registers handlers for incoming messages
  │
  └─ sendInitInstancePropertiesMessage()
        scans @DynaConfigEnabler beans + @ConfigurationProperties
        collects Map<propertyName, Set<values>>
        ──► App2WCPConsole.notify("dynaconfig", InstanceInitPropertiesMessageDto)
                                                        │
CONSOLE receives InstanceInitPropertiesMessageDto       │
        └─ InstanceResponseHandler.handleInstanceInitProperties()
              DynaConfigInstanceManager stores values
              SynchronizeInstanceService.syncInstance()

CONSOLE boots (@PostConstruct DynaConfigServiceActivator)
  └─ for each manifest with dynaconfig:
        SynchronizeInstanceService.refreshInstances()
        ──► WCPConsole2App.broadcast("dynaconfig", PropertiesRefreshRequestMessageDto)
                                                        │
APPLICATION receives PropertiesRefreshRequestMessageDto │
        └─ (if not INITIALIZING) re-runs sendInitInstancePropertiesMessage()
```

---

## Property Update Flow

```
Console REST PUT /services/dynaconfig/{domain}/{env}/{appId}
  └─ InstanceRequestHandler.broadcastMultiUpdatesRequest()
        builds InstanceInitPropertiesUpdateRequestDto
        stores DynaConfigRequest{requestId, messages, responses=[]}
        ──► WCPConsole2App.broadcast(...)       [or .send() for unicast]
                                                        │
APPLICATION receives InstanceInitPropertiesUpdateRequestDto
  └─ updates each property via DynamicPropertyUpdateHandler
        ──► App2WCPConsole.notifyResponse(requestId, InstanceInitPropertiesUpdateResponseDto)
                                                        │
CONSOLE receives InstanceInitPropertiesUpdateResponseDto
  └─ InstanceResponseHandler.handleInstanceMultiUpdatesResponse()
        links responses to original request
        DynaConfigInstanceManager.updateInstanceStatus()
```

---

## Application State Machine

```
INITIALIZING
    │  on receiving InstanceInitPropertiesUpdateRequestDto
    ├─ all OK   → RUNNING
    └─ any fail → RUNNING_WITH_ERRORS

RUNNING / RUNNING_WITH_ERRORS
    └─ on PropertiesRefreshRequestMessageDto → re-sends InstanceInitPropertiesMessageDto
```

---

## Transport Layer

The two directions use **different transports**, creating an intentional asymmetry.

| Direction | Transport | Mechanism |
|-----------|-----------|-----------|
| Console → App | S3 | Console writes objects; app polls the prefix |
| App → Console | Kinesis | App puts records on a stream; console consumes it |

### Console → App: S3 polling

```
CONSOLE (sends)
  InstanceRequestHandler.broadcastMultiUpdatesRequest()
    └─ FrontWCPConsole2AppCommunicationService.broadcast()
         └─ ServiceMessageS3Producer.internalSend()
              └─ S3.putObject(bucket, "{domain}/{env}/{appId}/{messageId}.json", json)

APP (receives)
  WCP2AppNotificationS3Consumer  [dedicated thread: "wcpsdk-comm-wcp2app"]
    └─ polling loop (every ~Nms, starts at 500ms until app ready)
         └─ S3.listObjects(prefix="{domain}/{env}/{appId}/")
              └─ filter: lastModified > lastConsumption
                   └─ S3.getObject(key) → WCP2AppNotificationProcessor.process()
                        └─ dispatches to registered command handlers
                             └─ DynaConfigApplicationServiceIntegration handlers
```

**Key design points:**

- **S3 as a message bus** — Console writes one JSON file per message under `{domain}/{env}/{appId}/{messageId}.json`; no queue, no push — pure S3 object listing + timestamp comparison.
- **Broadcast vs unicast**
  - `broadcast` → writes to `{domain}/{env}/{appId}/` → all instances of that app pick it up (they all poll the same prefix).
  - `send` (unicast) → `ServiceInstanceMessageDto` carries an `instanceId`; the processor filters by instance.
- **Cleanup** — Console runs a `@Scheduled` task every **60 seconds** to delete S3 objects older than 60 seconds. The bucket is ephemeral by design, purely for real-time signalling.

### App → Console: Kinesis

App responses travel the other way via `App2WCPConsoleCommunicationServiceImpl`, which calls `PutRecord` on a Kinesis stream. The Console consumes this stream to receive `InstanceInitPropertiesMessageDto` and `InstanceInitPropertiesUpdateResponseDto`.

---

## Periodic Refresh

The Console schedules a broadcast every **5 minutes**: it sends `PropertiesRefreshRequestMessageDto` to all registered instances of all apps, keeping the Console in sync with current runtime values.

---

## Key Classes

| Class | Role |
|-------|------|
| `InstanceRequestHandler` | Sends requests; tracks in-flight requests in `ConcurrentHashMap<requestId, DynaConfigRequest>` |
| `InstanceResponseHandler` | Receives responses; correlates via `requestId` |
| `DynaConfigApplicationServiceIntegration` | App-side message dispatcher |
| `DynaConfigSpringBeanProcessor` | Scans beans for `@DynaConfigEnabler` / `@DynaConfigAttribute` |
| `DynaConfigLegacyPublisher` | Legacy publisher that wraps outgoing messages in the legacy envelope format |
| `DynaConfigLegacyMapper` | Maps between modern DTOs and legacy wire format |