# Kockpit
Kockpit, an Engineering Platform.

> **Breaking change — kockpit 2.x** : à partir de la version 2.0.0, kockpit cible **Spring Boot 4 uniquement**
> (Spring Framework 7, Jakarta EE 11, Spring AI 2.x) et requiert **Java 21 minimum** (build et runtime,
> y compris pour les projets consommant `kockpit-rules-maven-plugin`).
> Les consommateurs Spring Boot 3.x doivent rester sur la lignée kockpit 1.x.

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

## Deployment Workflows

### Rules Designer to S3

The Kockpit Rules Designer (React/Vite frontend) can be automatically deployed to AWS S3.

**Workflow File:** `.github/workflows/rules-designer-s3.yml`

#### Automatic Deployment

The workflow triggers automatically on:
- Push to `deploy/rules-designer` branch when files in `kockpit-rules/kockpit-rules-designer/` change

#### Manual Deployment

1. Go to GitHub → Actions → "Deploy Rules Designer to S3"
2. Click "Run workflow"
3. Select environment: `dev`, `staging`, or `production`
4. Optionally specify a custom S3 bucket name

#### Required GitHub Secrets

Set these in GitHub → Settings → Secrets and variables → Actions:

| Secret Name | Description | Example |
|-------------|-------------|---------|
| `AWS_ROLE_ARN` | AWS IAM role ARN for OIDC (recommended) | `arn:aws:iam::123456789:role/GitHubActionsRole` |
| `AWS_REGION` | AWS region for S3 bucket | `eu-west-1` |
| `S3_BUCKET_DEV` | S3 bucket for dev environment | `kockpit-rules-designer-dev` |
| `S3_BUCKET_STAGING` | S3 bucket for staging environment | `kockpit-rules-designer-staging` |
| `S3_BUCKET_PRODUCTION` | S3 bucket for production environment | `kockpit-rules-designer-prod` |
| `CLOUDFRONT_DISTRIBUTION_ID` | (Optional) CloudFront distribution ID | `E1234567890ABC` |

**Alternative:** Instead of OIDC, you can use AWS access keys:
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_REGION`

#### AWS Setup for OIDC (Recommended)

1. **Create IAM Role for GitHub Actions:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::YOUR_ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:YOUR_ORG/kockpit:*"
        }
      }
    }
  ]
}
```

2. **Attach Policy to Role:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::kockpit-rules-designer-*",
        "arn:aws:s3:::kockpit-rules-designer-*/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "cloudfront:CreateInvalidation"
      ],
      "Resource": "arn:aws:cloudfront::YOUR_ACCOUNT_ID:distribution/*"
    }
  ]
}
```

3. **Create OIDC Provider in AWS:**
   - Provider URL: `https://token.actions.githubusercontent.com`
   - Audience: `sts.amazonaws.com`

#### S3 Bucket Configuration

For hosting static websites:

```bash
# Enable static website hosting
aws s3 website s3://kockpit-rules-designer-dev/ \
  --index-document index.html \
  --error-document index.html

# Set bucket policy for public read
aws s3api put-bucket-policy --bucket kockpit-rules-designer-dev --policy '{
  "Version": "2012-10-17",
  "Statement": [{
    "Sid": "PublicReadGetObject",
    "Effect": "Allow",
    "Principal": "*",
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::kockpit-rules-designer-dev/*"
  }]
}'
```

#### CloudFront Setup (Optional)

For HTTPS and CDN:

1. Create CloudFront distribution pointing to S3 bucket
2. Configure custom domain (optional)
3. Add distribution ID to `CLOUDFRONT_DISTRIBUTION_ID` secret

#### What Gets Deployed

- Built React application from `kockpit-rules/kockpit-rules-designer/dist/`
- Static assets with 1-year cache
- HTML files with no-cache headers
- Automatic CloudFront cache invalidation (if configured)

#### Local Build and Deploy

```bash
# Navigate to designer
cd kockpit-rules/kockpit-rules-designer

# Install dependencies
npm install

# Build
npm run build

# Deploy manually (requires AWS CLI configured)
aws s3 sync dist/ s3://your-bucket-name/ --delete
```

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

