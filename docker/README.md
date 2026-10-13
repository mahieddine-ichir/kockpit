# Kockpit Docker Compose Configurations

This directory contains Docker Compose configurations for running Kockpit with different storage backends.
Each profile has its own folder with a `docker-compose.yml`, so run `docker compose` from inside that folder:

| Folder        | Contents                                                                    |
|---------------|-----------------------------------------------------------------------------|
| `kafka/`      | Infrastructure only: Kafka, OpenSearch, OpenSearch Dashboards               |
| `azure/`      | Kafka + OpenSearch + Kockpit applications using Azure Storage (Azurite)     |
| `aws/`        | LocalStack (S3, SQS, SNS, Kinesis, DynamoDB) + OpenSearch                   |
| `filesystem/` | Kafka + OpenSearch + Kockpit applications using local filesystem storage    |
| `opensearch/` | OpenSearch + Dashboards only; also holds the OpenSearch data (`data/`, git-ignored) and init data shared by all profiles |

`docker-compose.yml` at the root of this directory includes `kafka/docker-compose.yml`, so `docker compose up -d`
from here starts the Kafka infrastructure.

All profiles use the same container names (`opensearch`, `kafka`, ...), so run one profile at a time.

## Kafka Infrastructure

The `kafka/` folder starts only the infrastructure needed to run Kockpit locally against Kafka
(the Kockpit applications themselves are run from the IDE):

- **kafka**: Apache Kafka (KRaft, single node)
- **opensearch**: OpenSearch, with security disabled
- **dashboards**: OpenSearch Dashboards

```bash
cd kafka
docker compose up -d
```

Endpoints:
- Kafka, from the host: `localhost:29092`
- Kafka, from other containers: `kafka:9092`
- OpenSearch: http://localhost:9200
- OpenSearch Dashboards: http://localhost:5601

OpenSearch data is stored in `opensearch/data` (git-ignored), and `opensearch/init/data` is mounted at
`/init-data`. To create the index and load the sample audits:

```bash
docker exec -w /init-data opensearch bash init-data.sh
```

Because the Compose project is named after the folder (`kafka`), stop it from the same folder:

```bash
docker compose down
```

## Azure Configuration

The `azure/` folder sets up Kockpit with Azure Storage. By default, storage is emulated locally by
[Azurite](https://github.com/Azure/Azurite), so no Azure account is needed.

### Services

- **azurite**: Azure Blob Storage emulator (port 10000)
- **azurite-init**: one-shot container that creates the blob containers in Azurite, then exits
- **kafka**: Apache Kafka for event streaming (compatible with Azure Event Hub)
- **opensearch**: OpenSearch for search and analytics
- **opensearch-dashboard**: OpenSearch Dashboards for visualization
- **kockpit-audit-stream-kafka**: Kafka audit stream processor
- **kockpit-backend-application**: Main Kockpit backend API
- **kockpit-console**: Web UI for audit visualization
- **kockpit-sample-all**: Sample application demonstrating Kockpit SDK usage

### Running with Azurite (default)

```bash
cd azure
docker compose up -d
```

Azurite uses its standard development account, `devstoreaccount1`, with its
[well-known key](https://learn.microsoft.com/azure/storage/common/storage-use-azurite#http-connection-strings).
`azurite-init` creates the `data` and `manifests` containers, and the backend and sample application wait for it
to finish. Blob data is kept in the `azurite-data` volume.

To browse the blobs from the host (e.g. with Azure Storage Explorer), use the connection string:

```
DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://localhost:10000/devstoreaccount1;
```

### Running with a real Storage Account

1. **Copy the environment template** (`.env` is git-ignored and loaded automatically by Compose):
   ```bash
   cd azure
   cp .env.azure .env
   ```

2. **Configure Azure Storage credentials** in the `.env` file:
   - `STORAGE_ENDPOINT`: Your Azure Storage endpoint URL
   - `STORAGE_ACCOUNT`: Your Azure Storage account name
   - `STORAGE_KEY`: Your Azure Storage account access key
   - `STORAGE_CONTAINER`: Container name (defaults to "data")
   - `MANIFESTS_STORAGE_*`: Same settings for the manifests storage (container defaults to "manifests")

   The containers must already exist in the storage account. Azurite still starts, but is not used.

3. **Start the services**:
   ```bash
   docker compose up -d
   ```

### Endpoints

- Backend API: http://localhost:8080/backend/api
- Sample Application: http://localhost:8082/sample-app
- Audit Console UI: http://localhost:3000
- OpenSearch: http://localhost:9200
- OpenSearch Dashboards: http://localhost:5601
- Kafka: localhost:29092 (from the host), kafka:9092 (from other containers)
- Azurite Blob: http://localhost:10000/devstoreaccount1
- Audit Stream: http://localhost:8081

### Getting Azure Storage Credentials

1. Go to [Azure Portal](https://portal.azure.com)
2. Navigate to your Storage Account
3. Under "Security + networking", click "Access keys"
4. Copy the values for:
   - Storage account name
   - Key (key1 or key2)
   - Endpoint URL format: `https://<storage-account-name>.blob.core.windows.net`

### Stopping Services

```bash
docker compose down
```

### Viewing Logs

```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f kockpit-backend-application
```

## AWS Configuration

The `aws/` folder starts LocalStack and OpenSearch. The LocalStack init scripts in `aws/init` create the S3
buckets, SQS queues, SNS topics, Kinesis streams and DynamoDB tables when LocalStack is ready.

```bash
cd aws
docker compose up -d
```

If your LocalStack version requires an auth token, export `LOCALSTACK_AUTH_TOKEN` before starting it.

## Filesystem Configuration

The `filesystem/` folder runs Kafka, OpenSearch and the Kockpit applications, storing audits and manifests in
`~/IdeaProjects/kockpit/data`.

```bash
cd filesystem
docker compose up -d
```

## Miscellaneous

Start the audit-console
```shell
   docker run -p 3000:80 \
    -e KOCKPIT_BACKEND=http://localhost:8080/backend \
    --name kockpit-console \
   ghcr.io/mahieddine-ichir/kockpit/kockpit-console:latest
```

Start the audit-console-backend
```shell
   docker run -p 8080:8080 --rm \
      -v ./data:/data \
      -e OPENSEARCH_ENDPOINTS=http://localhost:9200 \
      -e INDEX_NAME=audit-data \
      -e kockpit.audit.local.storage-path=/data/manifests \
      -e kockpit.sdk.filesystem.local_directory=/data \
      -e kockpit.sdk.manifest.filesystem.path=/data/manifests \
      -e spring.profiles.active=filesystem,opensearch,local \
      --name kockpit-backend \
   ghcr.io/mahieddine-ichir/kockpit/kockpit-backend-application-filesystem:latest
```
