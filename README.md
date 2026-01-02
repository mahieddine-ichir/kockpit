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
