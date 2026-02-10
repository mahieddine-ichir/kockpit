# Kockpit ECS Service Deployment Example

This directory shows how to use the Kockpit ECS service module in another project.

## Quick Start

1. **Copy this example** to your project directory
2. **Update the source** in `main.tf` to point to your Kockpit repository
3. **Configure variables** by copying `terraform.tfvars.example` to `terraform.tfvars`
4. **Deploy**:
   ```bash
   terraform init
   terraform plan
   terraform apply
   ```

## Usage Examples

### Basic Usage
```hcl
module "kockpit_backend" {
  source = "git::https://github.com/your-org/kockpit.git//deployments/aws/modules/ecs-service?ref=v1.0.0"

  # Minimal required configuration
  vpc_id                = "vpc-12345"
  private_subnet_names  = ["subnet-1", "subnet-2"]
  ecs_cluster_name      = "my-cluster"
  load_balancer_arn     = "arn:aws:elasticloadbalancing:..."
  s3_bucket_name        = "my-bucket"

  opensearch_endpoints        = "search-cluster.region.es.amazonaws.com"
  kockpit_data_s3_bucket     = "my-bucket"
  kockpit_manifests_s3_bucket = "my-manifests-bucket"
}
```

### Production Usage
```hcl
module "kockpit_prod" {
  source = "git::https://github.com/your-org/kockpit.git//deployments/aws/modules/ecs-service?ref=v1.2.0"

  # Production configuration
  kockpit_env    = "production"
  cpu           = 2048
  memory        = 4096
  desired_count = 5

  # All other required variables...

  tags = {
    Environment = "production"
    Team        = "platform"
    CostCenter  = "engineering"
  }
}
```

### Multiple Environments
```hcl
# Development environment
module "kockpit_dev" {
  source = "git::https://github.com/your-org/kockpit.git//deployments/aws/modules/ecs-service"

  kockpit_env = "dev"
  cpu        = 512
  memory     = 1024
  # ... dev-specific config
}

# Production environment
module "kockpit_prod" {
  source = "git::https://github.com/your-org/kockpit.git//deployments/aws/modules/ecs-service"

  kockpit_env = "production"
  cpu        = 2048
  memory     = 4096
  # ... prod-specific config
}
```

## Required Infrastructure

Before using this module, ensure you have:

- ✅ **VPC** with private subnets
- ✅ **ECS Cluster** (Fargate)
- ✅ **Application Load Balancer**
- ✅ **S3 Buckets** for data and manifests
- ✅ **OpenSearch** cluster
- ⚠️  **Kinesis Stream** (optional)

## Module Source Options

### 1. Git Repository (Recommended for teams)
```hcl
source = "git::https://github.com/your-org/kockpit.git//deployments/aws/modules/ecs-service?ref=v1.0.0"
```

### 2. Git with SSH (for private repos)
```hcl
source = "git::ssh://git@github.com/your-org/kockpit.git//deployments/aws/modules/ecs-service?ref=v1.0.0"
```

### 3. Local path (for development)
```hcl
source = "./modules/kockpit-ecs-service"
```

### 4. Terraform Registry (if published)
```hcl
source  = "your-org/kockpit-ecs/aws"
version = "~> 1.0"
```

## Best Practices

1. **Pin versions** using Git tags (`?ref=v1.0.0`)
2. **Use separate state files** for different environments
3. **Store sensitive values** in AWS SSM/Secrets Manager
4. **Review security groups** and networking configuration
5. **Monitor costs** with appropriate tags

## Outputs

After deployment, the module provides these outputs:
- `service_name` - ECS service name
- `target_group_arn` - For ALB listener rules
- `service_url` - Access URL
- `security_group_id` - For additional security rules
- `log_group_name` - CloudWatch logs location

## Troubleshooting

### Common Issues

1. **Service won't start**: Check CloudWatch logs at `/ecs/kockpit-console-backend`
2. **Health checks fail**: Verify `/actuator/health` endpoint works
3. **Can't access S3**: Check IAM permissions and bucket policies
4. **OpenSearch connection**: Verify security groups and VPC connectivity

### Useful Commands

```bash
# Check service status
aws ecs describe-services --cluster YOUR_CLUSTER --services kockpit-console-backend

# View logs
aws logs tail /ecs/kockpit-console-backend --follow

# Check target health
aws elbv2 describe-target-health --target-group-arn TARGET_GROUP_ARN
```