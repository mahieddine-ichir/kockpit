# Kockpit Docker Compose Configurations

This directory contains Docker Compose configurations for running Kockpit with different storage backends.

## Azure Configuration

The `docker-compose-azure.yml` file sets up Kockpit with Azure Storage Account integration.

### Services

- **kafka**: Apache Kafka for event streaming (compatible with Azure Event Hub)
- **opensearch**: OpenSearch for search and analytics
- **opensearch-dashboard**: OpenSearch Dashboards for visualization
- **kockpit-audit-stream-kafka**: Kafka audit stream processor
- **kockpit-backend-application**: Main Kockpit backend API
- **kockpit-console**: Web UI for audit visualization
- **kockpit-sample-all**: Sample application demonstrating Kockpit SDK usage

### Setup Instructions

1. **Copy the environment template**:
   ```bash
   cp .env.azure .env
   ```

2. **Configure Azure Storage credentials** in the `.env` file:
   - `STORAGE_ENDPOINT`: Your Azure Storage endpoint URL
   - `STORAGE_ACCOUNT`: Your Azure Storage account name
   - `STORAGE_KEY`: Your Azure Storage account access key
   - `STORAGE_CONTAINER`: Container name (defaults to "kockpit")

3. **Start the services**:
   ```bash
   docker-compose -f docker-compose-azure.yml --env-file .env up -d
   ```

4. **Access the services**:
   - Backend API: http://localhost:8080/backend/api
   - Sample Application: http://localhost:8081/sample-app
   - Audit Console UI: http://localhost:3000
   - OpenSearch: http://localhost:9200
   - OpenSearch Dashboards: http://localhost:5601
   - Kafka: localhost:9092
   - Audit Stream: http://localhost:9080

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
docker-compose -f docker-compose-azure.yml down
```

### Viewing Logs

```bash
# All services
docker-compose -f docker-compose-azure.yml logs -f

# Specific service
docker-compose -f docker-compose-azure.yml logs -f kockpit-backend-application
```

## Other Configurations

- `docker-compose-aws.yml`: AWS configuration with Kinesis
- `docker-compose-filesystem.yml`: Local filesystem storage configuration

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
