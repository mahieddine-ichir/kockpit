# Kockpit Console Backend Module

This Terraform module creates an ECS Fargate service for the Kockpit Console Backend.

It is a thin wrapper around [`ecs-app-module`](../ecs-app-module), which provides the ECS
task/service, ALB target group, security group, IAM roles (task + task execution), and CloudWatch log
group. This module adds on top: the S3 buckets and their IAM policy, the dedicated HTTP listener, the
CloudFront-to-ALB security group rule, and the Kockpit/OpenSearch-flavored environment variables.

**Auth**: this module does not authenticate traffic at the ALB — the listener it creates is a plain
HTTP forward, unauthenticated. Access control is enforced upstream, at the CloudFront distribution +
Lambda@Edge in the `kockpit-console` module that fronts this backend. Don't add ALB-level auth here
without also reconciling it with that layer.

## Features

- **Kockpit Spring Boot Application**: Pre-configured for Kockpit console backend with OpenSearch integration
- **Multi-Architecture Support**: Supports both ARM64 and X86_64 CPU architectures
- **S3 Integration**: Configures IAM permissions for Kockpit data and manifests buckets
- **Load Balancer Integration**: Creates a dedicated ALB HTTP listener forwarding all traffic to this
  service's target group (not a path-based rule on a shared listener)
- **Health Check Configuration**: Supports separate health check ports (e.g., Spring Boot management port)
- **Environment-Aware**: Configurable environments (dev, staging, production)
- **Security**: Security group ingress from the ALB, scoped to the container/health-check ports
- **Logging**: CloudWatch log group with configurable retention
- **OpenSearch Integration**: Built-in configuration for search endpoints
- **Kinesis Integration**: Optional audit streaming to Kinesis

## Usage

```hcl
module "kockpit_backend" {
  source = "git::https://github.com/mahieddine-ichir/kockpit.git//deployments/aws/modules/kockpit-console-backend?ref=main"

  # Required infrastructure
  vpc_id                = "vpc-xxxxxxxxx"
  private_subnet_ids    = ["subnet-xxxxxxxxx", "subnet-yyyyyyyyy"]
  ecs_cluster_name      = "my-ecs-cluster"
  load_balancer_arn     = "arn:aws:elasticloadbalancing:region:account:loadbalancer/app/my-alb/xxxxxxxxx"
  lb_security_group_id  = "sg-xxxxxxxxx"

  # Kockpit configuration
  kockpit_env                   = "production"
  opensearch_endpoints          = "https://search-domain.region.es.amazonaws.com:443"
  kockpit_data_s3_bucket       = "kockpit-data-production"
  kockpit_manifests_s3_bucket  = "kockpit-manifests-production"

  # Optional customization
  service_name         = "kockpit-console-backend"
  image_tag           = "v1.0.0"
  cpu_architecture    = "ARM64"
  container_port      = 8080
  cpu                 = 1024
  memory              = 2048
  desired_count       = 2
  health_check_path   = "/actuator/health"
  health_check_port   = "8090"

  tags = {
    Environment = "production"
    Project     = "kockpit"
  }
}
```

## Requirements

| Name | Version |
|------|---------|
| terraform | >= 1.4 |
| aws | ~> 5.0 |

## Providers

| Name | Version |
|------|---------|
| aws | ~> 5.0 |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| vpc_id | ID of the existing VPC | `string` | n/a | yes |
| private_subnet_ids | IDs of the private subnets where ECS tasks will run | `list(string)` | n/a | yes |
| ecs_cluster_name | Name of the existing ECS cluster | `string` | n/a | yes |
| load_balancer_arn | ARN of the load balancer this module creates its HTTP listener on | `string` | n/a | yes |
| lb_security_group_id | Security group ID of the load balancer | `string` | n/a | yes |
| opensearch_endpoints | OpenSearch cluster endpoints | `string` | n/a | yes |
| kockpit_data_s3_bucket | S3 bucket for Kockpit data storage | `string` | n/a | yes |
| kockpit_manifests_s3_bucket | S3 bucket for Kockpit manifests storage | `string` | n/a | yes |
| aws_region | AWS region where resources will be created | `string` | `"eu-west-1"` | no |
| service_name | Name of the ECS service | `string` | `"kockpit-console-backend"` | no |
| kockpit_env | Kockpit environment (dev, staging, prod) | `string` | `"dev"` | no |
| image_tag | Docker image tag for the kockpit backend application | `string` | `"latest"` | no |
| cpu_architecture | CPU architecture for the ECS task (ARM64 or X86_64) | `string` | `"ARM64"` | no |
| container_port | Port the container listens on | `number` | `8080` | no |
| cpu | CPU units for the ECS task (1024 = 1 vCPU) | `number` | `512` | no |
| memory | Memory for the ECS task in MB | `number` | `1024` | no |
| desired_count | Desired number of ECS service instances | `number` | `2` | no |
| health_check_path | Health check path for the target group | `string` | `"/actuator/health"` | no |
| health_check_port | Health check port for the target group | `string` | `"traffic-port"` | no |
| log_retention_days | CloudWatch log retention period in days | `number` | `7` | no |
| kinesis_stream_name | Kinesis stream name for audit notifications | `string` | `""` | no |
| additional_environment_variables | Additional environment variables to pass to the container | `list(object({name=string,value=string}))` | `[]` | no |
| tags | Tags to apply to all resources | `map(string)` | See variables.tf | no |

## Outputs

| Name | Description |
|------|-------------|
| ecs_service_name | Name of the ECS service |
| ecs_service_arn | ARN of the ECS service |
| task_definition_arn | ARN of the ECS task definition |
| target_group_arn | ARN of the target group |
| target_group_name | Name of the target group |
| security_group_id | ID of the ECS service security group |
| task_execution_role_arn | ARN of the ECS task execution role |
| task_role_arn | ARN of the ECS task role |
| log_group_name | Name of the CloudWatch log group |

## S3 Permissions

The module creates an IAM role with the following S3 permissions for both data and manifests buckets:
- `s3:ListBucket` - List objects in the buckets
- `s3:GetObject` - Read objects from the buckets
- `s3:PutObject` - Write objects to the buckets

## Container Image

The module uses a fixed container image: `ghcr.io/mahieddine-ichir/kockpit/kockpit-backend-application-aws` with configurable tags via the `image_tag` variable.

## Security Groups

The ECS task's security group (created by `ecs-app-module`) allows inbound traffic from
`lb_security_group_id` on `container_port`, plus `health_check_port` when it differs from
`container_port`. This module additionally opens `lb_security_group_id` to CloudFront's managed
prefix list on port 80, so the ALB can be reached from the CloudFront distribution in front of it.

Note: unlike the bidirectional rules this module used to create directly, `ecs-app-module` does not
add egress rules onto the load balancer's own security group — it relies on that security group
already permitting outbound traffic (the common default for an ALB security group). If
`lb_security_group_id` has restrictive egress, add an explicit egress rule to it separately.

## Load Balancer Integration

The module creates its own dedicated HTTP listener (port 80) on `load_balancer_arn`, whose default
action forwards all traffic to this service's target group — it is not a path-based rule on a shared
listener. If you need path-based routing to multiple backends on the same ALB, put this module behind
its own listener (as it does today) or behind a separate, purpose-built listener rule outside this
module.

## Architecture

```
Internet → ALB → ECS Tasks (Fargate)
            ↓
       [Security Groups]
            ↓
    S3 (Data & Manifests) + OpenSearch + Kinesis (optional)
```

## Environment Variables

The module automatically configures these environment variables for the Kockpit application:
- `KOCKPIT_AWS_REGION` / `KOCKPIT_SDK_AWS_REGION` / `aws.region`
- `KOCKPIT_ENV`
- `SPRING_PROFILES_ACTIVE=aws`
- `OPENSEARCH_ENDPOINTS` / `kockpit.audit.stream.opensearch.endpoints`
- `kockpit.aws.s3.bucket` / `kockpit.manifests.aws.s3.bucket`
- `kockpit.audit.notification.kinesis.stream_name` (if provided)

## OpenSearch Configuration

⚠️ **Important**: OpenSearch connectivity requires proper HTTPS configuration:

### HTTPS Connection Required
AWS OpenSearch Service domains use **HTTPS by default**. The backend service must connect using the correct protocol and port:

```hcl
# ✅ Correct - Use HTTPS URL with port 443
opensearch_endpoints = "https://search-your-domain.region.es.amazonaws.com:443"

# ❌ Incorrect - HTTP will cause connection timeouts
opensearch_endpoints = "http://search-your-domain.region.es.amazonaws.com:9200"
```

### Environment Variable Configuration
The module sets these OpenSearch-related environment variables:
```bash
OPENSEARCH_ENDPOINTS=https://search-domain.region.es.amazonaws.com:443
kockpit.audit.stream.opensearch.endpoints=https://search-domain.region.es.amazonaws.com:443
```

### Common Issues and Solutions
| Issue | Cause | Solution |
|-------|-------|----------|
| Connection timeouts | Using HTTP instead of HTTPS | Update `opensearch_endpoints` to use `https://` |
| SSL handshake failures | Port mismatch (9200 vs 443) | Use port 443 for HTTPS connections |
| Access denied | Missing IAM permissions | Ensure ECS task role has OpenSearch permissions |

### IAM Permissions for OpenSearch
If your OpenSearch domain uses IAM-based access control, ensure the ECS task role includes:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "es:ESHttpGet",
        "es:ESHttpPost",
        "es:ESHttpPut",
        "es:ESHttpDelete"
      ],
      "Resource": "arn:aws:es:region:account:domain/your-domain/*"
    }
  ]
}
```