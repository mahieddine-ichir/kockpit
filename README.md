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
- kockpit-backend-application-azure (docker image): uses a storage account for file-based communication and Azure event hub (on Kafka protocol) for audits notifications.
- kockpit-backend-application-aws (docker image): uses a s3 for file-based communication and Kinesis for audits notifications.

### Kockpit Stream application
- kockpit-audit-stream-application-kafka (docker image): The Kockpit stream application that reads audits from Kafka broker and
indexes them into an Opensearch cluster
```shell
docker run -p 8080:8080 -e OPENSEARCH_ENDPOINTS=http://opensearch-1:9200,http://opensearch-2:9200 \
  -e KAFKA_
  ghcr.io/mahieddine-ichir/kockpit/kockpit-backend-application-kafka:latest
```
- kockpit-audit-stream-application-kinesis (docker image):
