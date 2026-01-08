# Kockpit
Kockpit, an Engineering Platform.

# Kockpit modules / composition

## Core

## Features

## Kockpit Rules
* Rules definition:
  * the rule engine POJO's needed for a rule description
  * rules execution interfaces
* Rules executor
* Rules registry data: rules registry definition (interfaces)
* Rules registry: rules registry default implementation
* Rules starter

* Rules maven plugin: A maven plugin to generate the Rules DSL from an input JSON file
* Rules app Sample: a sample application on how to use the Rules engine, along with the maven plugin

## Core Applications

## Samples

## Packages

- kockpit-console (docker image): The Kockpit Web Console
```shell
  docker run ghcr.io/mahieddine-ichir/kockpit/kockpit-console:latest
```
todo bind to the backend-api

- kockpit-console.zip (ZIP package).
todo how to deploy on azure
todo how to deploy on AWS

### Kockpit Backend application
The Kockpit backend API (for web console). Audits search engine is backed by an Opensearch cluster.

- kockpit-backend-application-filesystem (docker image): uses a local drive / filesystem for file-based communication.
```shell
  docker run -p 8080:8080 \
    -e OPENSEARCH_ENDPOINTS=http://opensearch:9200 \
    -e kockpit.sdk.manifest.filesystem.path=/data/manifests \
    -e kockpit.sdk.filesystem.local_directory=/data \
    -v ~/IdeaProjects/kockpit/data:/data \
    ghcr.io/mahieddine-ichir/kockpit/kockpit-backend-application-filesystem
```
- kockpit-backend-application-azure (docker image): uses a storage account for file-based communication and Azure event hub (on Kafka protocol) for audits notifications.
```shell
  docker run -p 8080:8080 \
    -e spring.profiles.include=azure \
    -e OPENSEARCH_ENDPOINTS=http://opensearch:9200 \
    -e kockpit.sdk.service.audit.notification.topic=audits \
    -e STORAGE_ENDPOINT=<azure storage account endpointm> \
    -e STORAGE_ACCOUNT=<azure storage account name> \
    -e STORAGE_KEY=<azure storage account key> \
    -e STORAGE_CONTAINER=<azure storage account container> \
    ghcr.io/mahieddine-ichir/kockpit/kockpit-backend-application-azure
```
or using a local .env file
```shell
docker run -p 8080:8080 --env-file .env.azure \
    -e spring.profiles.include=azure \
    -e OPENSEARCH_ENDPOINTS=http://opensearch:9200 \
    -e kockpit.sdk.service.audit.notification.topic=audits \
    ghcr.io/mahieddine-ichir/kockpit/kockpit-backend-application-azure
```

- kockpit-backend-application-aws (docker image): uses a s3 for file-based communication and Kinesis for audits notifications.
```shell
  docker run -p 8080:8080 \
    -e kockpit.sdk.aws.region=eu-west-1 \
    -e kockpit.service.aws.region=eu-west-1 \
    -e OPENSEARCH_ENDPOINTS=http://opensearch:9200 \
    ghcr.io/mahieddine-ichir/kockpit/kockpit-backend-application-aws
```

or using a local .env file
### Kockpit Stream application
- kockpit-audit-stream-application-kafka (docker image): The Kockpit stream application that reads audits from Kafka broker and
indexes them into an Opensearch cluster
```shell
docker run -p 8080:8080 -e OPENSEARCH_ENDPOINTS=http://opensearch-1:9200,http://opensearch-2:9200 \
  -e KAFKA_
  ghcr.io/mahieddine-ichir/kockpit/kockpit-backend-application-kafka:latest
```
- kockpit-audit-stream-application-kinesis (docker image):

---

## Configuration Properties

### Kockpit AI MCP Server

#### Core Properties (Default Profile)

| Property | Default Value | Description |
|----------|---------------|-------------|
| `server.port` | `8080` | Server port for the MCP server application |
| `spring.application.name` | `mcp-server` | Spring Boot application name |
| `spring.main.web-application-type` | `servlet` | Web application type (servlet mode) |
| `spring.main.banner-mode` | `off` | Disables Spring Boot banner on startup |
| `spring.ai.mcp.server.name` | `mcp-server` | MCP server name identifier |
| `spring.ai.mcp.server.version` | `1.0.0` | MCP server version |
| `spring.ai.mcp.server.type` | `sync` | MCP server type (sync vs async) |
| `spring.ai.mcp.server.stdio` | `false` | Disables stdio transport for MCP server |
| `opensearch.endpoints` | `localhost` | OpenSearch endpoints (comma-separated URLs) |
| `opensearch.env` | `dev` | OpenSearch environment for audit searches |
| `opensearch.search.index_version` | `wcp` | OpenSearch index version (affects index key paths) |

#### AWS Profile Properties (`-Paws`)

Activate AWS support using Maven profile: `mvn spring-boot:run -Paws`

| Property | Default Value | Description |
|----------|---------------|-------------|
| `aws.service.name` | `es` | AWS service name for Signature V4 signing (Elasticsearch/OpenSearch) |
| `aws.region` | `eu-west-1` | AWS region for credentials and request signing |

**Example AWS Configuration:**
```yaml
# application-aws.yaml
aws:
  service:
    name: es
  region: eu-west-1

opensearch:
  endpoints: https://vpc-your-domain.region.es.amazonaws.com
```

**AWS Credentials:**
The AWS module uses the default AWS credentials chain:
- Environment variables (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`)
- AWS credential files (`~/.aws/credentials`)
- IAM instance profiles (EC2, ECS, Lambda)
- IAM role assumed credentials

### Kockpit Backend Application

#### Filesystem Profile

| Property | Default Value | Description |
|----------|---------------|-------------|
| `OPENSEARCH_ENDPOINTS` | - | OpenSearch cluster endpoints |
| `kockpit.sdk.manifest.filesystem.path` | - | Path for manifest files storage |
| `kockpit.sdk.filesystem.local_directory` | - | Local directory for file-based communication |

#### Azure Profile

| Property | Default Value | Description |
|----------|---------------|-------------|
| `spring.profiles.include` | - | Activate Azure profile |
| `OPENSEARCH_ENDPOINTS` | - | OpenSearch cluster endpoints |
| `kockpit.sdk.service.audit.notification.topic` | `audits` | Azure Event Hub topic for audit notifications |
| `STORAGE_ENDPOINT` | - | Azure storage account endpoint |
| `STORAGE_ACCOUNT` | - | Azure storage account name |
| `STORAGE_KEY` | - | Azure storage account access key |
| `STORAGE_CONTAINER` | - | Azure storage container name |

#### AWS Profile

| Property | Default Value | Description |
|----------|---------------|-------------|
| `kockpit.sdk.aws.region` | - | AWS region for SDK services (S3, Kinesis) |
| `kockpit.service.aws.region` | - | AWS region for service configuration |
| `OPENSEARCH_ENDPOINTS` | - | OpenSearch cluster endpoints (can be AWS managed) |

### Kockpit Audit Stream Application

#### Kafka Profile

| Property | Default Value | Description |
|----------|---------------|-------------|
| `OPENSEARCH_ENDPOINTS` | - | Comma-separated list of OpenSearch nodes |
| `KAFKA_` | - | Kafka broker configuration (incomplete in docs) |

#### Kinesis Profile

(To be documented)

---

### Environment Variable Examples

#### Running MCP Server with AWS OpenSearch

```bash
mvn spring-boot:run -Paws \
  -Daws.region=eu-west-1 \
  -Daws.service.name=es \
  -Dopensearch.endpoints=https://vpc-domain.eu-west-1.es.amazonaws.com
```

#### Running Backend with Filesystem

```bash
docker run -p 8080:8080 \
  -e OPENSEARCH_ENDPOINTS=http://opensearch:9200 \
  -e kockpit.sdk.manifest.filesystem.path=/data/manifests \
  -e kockpit.sdk.filesystem.local_directory=/data \
  -v ~/data:/data \
  ghcr.io/mahieddine-ichir/kockpit/kockpit-backend-application-filesystem
```

#### Running Backend with Azure

```bash
docker run -p 8080:8080 --env-file .env.azure \
  -e spring.profiles.include=azure \
  -e OPENSEARCH_ENDPOINTS=http://opensearch:9200 \
  -e kockpit.sdk.service.audit.notification.topic=audits \
  ghcr.io/mahieddine-ichir/kockpit/kockpit-backend-application-azure
```

---
