# ECS Service Terraform Module

This Terraform module creates an ECS Fargate service with:
- IAM roles for task execution and S3 access
- Security group for the ECS service
- ECS task definition with CloudWatch logging
- Target group for load balancer integration
- ECS service configuration

## Features

- **S3 Access**: Configures IAM permissions for listing, getting, and putting objects in an S3 bucket
- **Load Balancer Integration**: Creates a target group that can be attached to an existing load balancer
- **Security**: Follows AWS best practices with separate execution and task roles
- **Logging**: CloudWatch log group with configurable retention
- **Health Checks**: Configurable health check endpoint for the target group

## Usage

```hcl
module "kockpit_backend" {
  source = "./modules/ecs-service"

  # Required variables
  vpc_id                = "vpc-xxxxxxxxx"
  private_subnet_names  = ["private-subnet-1", "private-subnet-2"]
  ecs_cluster_name      = "my-ecs-cluster"
  load_balancer_arn     = "arn:aws:elasticloadbalancing:region:account:loadbalancer/app/my-alb/xxxxxxxxx"
  s3_bucket_name        = "my-kockpit-bucket"

  # Optional customization
  service_name      = "my-backend-service"
  container_image   = "my-account.dkr.ecr.region.amazonaws.com/my-app:latest"
  container_port    = 8080
  cpu              = 1024
  memory           = 2048
  desired_count    = 3

  environment_variables = [
    {
      name  = "SPRING_PROFILES_ACTIVE"
      value = "production"
    },
    {
      name  = "DATABASE_URL"
      value = "jdbc:postgresql://db.example.com:5432/mydb"
    }
  ]

  tags = {
    Environment = "production"
    Project     = "my-project"
  }
}
```

## Requirements

| Name | Version |
|------|---------|
| terraform | >= 1.0 |
| aws | ~> 5.0 |

## Providers

| Name | Version |
|------|---------|
| aws | ~> 5.0 |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| vpc_id | ID of the existing VPC | `string` | n/a | yes |
| private_subnet_names | Names of the private subnets where ECS tasks will run | `list(string)` | n/a | yes |
| ecs_cluster_name | Name of the existing ECS cluster | `string` | n/a | yes |
| load_balancer_arn | ARN of the existing load balancer | `string` | n/a | yes |
| s3_bucket_name | Name of the S3 bucket the ECS service needs access to | `string` | n/a | yes |
| service_name | Name of the ECS service | `string` | `"kockpit-console-backend"` | no |
| container_image | Docker image for the ECS service | `string` | `"nginx:latest"` | no |
| container_port | Port the container listens on | `number` | `8080` | no |
| cpu | CPU units for the ECS task (1024 = 1 vCPU) | `number` | `512` | no |
| memory | Memory for the ECS task in MB | `number` | `1024` | no |
| desired_count | Desired number of ECS service instances | `number` | `2` | no |
| health_check_path | Health check path for the target group | `string` | `"/health"` | no |
| log_retention_days | CloudWatch log retention period in days | `number` | `7` | no |
| aws_region | AWS region where resources will be created | `string` | `"us-west-2"` | no |
| environment_variables | Environment variables to pass to the container | `list(object({name=string,value=string}))` | See variables.tf | no |
| tags | Tags to apply to all resources | `map(string)` | `{}` | no |

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
| service_url | URL where the service will be accessible through the load balancer |

## S3 Permissions

The module creates an IAM role with the following S3 permissions for the specified bucket:
- `s3:ListBucket` - List objects in the bucket
- `s3:GetObject` - Read objects from the bucket
- `s3:PutObject` - Write objects to the bucket

## Load Balancer Integration

The module creates a target group that needs to be manually attached to your load balancer listener rules. You can use the `target_group_arn` output to configure your load balancer routing.

Example load balancer listener rule:
```hcl
resource "aws_lb_listener_rule" "backend" {
  listener_arn = var.load_balancer_listener_arn
  priority     = 100

  action {
    type             = "forward"
    target_group_arn = module.kockpit_backend.target_group_arn
  }

  condition {
    path_pattern {
      values = ["/api/*"]
    }
  }
}
```