# General Configuration
variable "aws_region" {
  description = "AWS region where resources will be created"
  type        = string
  default     = "us-west-2"
}

variable "service_name" {
  description = "Name of the ECS service"
  type        = string
  default     = "kockpit-console-backend"
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default = {
    Environment = "production"
    Project     = "kockpit"
    ManagedBy   = "terraform"
  }
}

# Network Configuration (Data Sources)
variable "vpc_id" {
  description = "ID of the existing VPC"
  type        = string
}

variable "private_subnet_ids" {
  description = "IDs of the private subnets where ECS tasks will run"
  type        = list(string)
}

variable "ecs_cluster_name" {
  description = "Name of the existing ECS cluster"
  type        = string
}

variable "load_balancer_listener_arn" {
  description = "ARN of the existing load balancer listener"
  type        = string
}

variable "listener_rule_priority" {
  description = "Priority for the load balancer listener rule"
  type        = number
  default     = 100
}

variable "path_pattern" {
  description = "Path pattern for the listener rule (e.g., '/api/*')"
  type        = string
  default     = "/*"
}

# Container Configuration
variable "container_image" {
  description = "Docker image for the ECS service"
  type        = string
  default     = "nginx:latest"  # Replace with actual kockpit-console-backend image
}

variable "container_port" {
  description = "Port the container listens on"
  type        = number
  default     = 8080
}

variable "cpu" {
  description = "CPU units for the ECS task (1024 = 1 vCPU)"
  type        = number
  default     = 512
}

variable "memory" {
  description = "Memory for the ECS task in MB"
  type        = number
  default     = 1024
}

variable "desired_count" {
  description = "Desired number of ECS service instances"
  type        = number
  default     = 2
}

# Health Check Configuration
variable "health_check_path" {
  description = "Health check path for the target group"
  type        = string
  default     = "/health"
}

variable "health_check_port" {
  description = "Health check port for the target group"
  type        = string
  default     = "traffic-port"
}

# Logging Configuration
variable "log_retention_days" {
  description = "CloudWatch log retention period in days"
  type        = number
  default     = 7
}

# Kockpit-specific Configuration
variable "kockpit_env" {
  description = "Kockpit environment (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "opensearch_endpoints" {
  description = "OpenSearch cluster endpoints (comma-separated if multiple)"
  type        = string
}

variable "kockpit_data_s3_bucket" {
  description = "S3 bucket for Kockpit data storage"
  type        = string
}

variable "kockpit_manifests_s3_bucket" {
  description = "S3 bucket for Kockpit manifests storage"
  type        = string
}

variable "kinesis_stream_name" {
  description = "Kinesis stream name for audit notifications"
  type        = string
  default     = ""
}

# Additional Environment Variables
variable "additional_environment_variables" {
  description = "Additional environment variables to pass to the container"
  type = list(object({
    name  = string
    value = string
  }))
  default = []
}

# Computed Environment Variables (internal)
locals {
  # Base environment variables derived from inputs
  base_environment_variables = [
    {
      name  = "KOCKPIT_AWS_REGION"
      value = var.aws_region
    },
    {
      name  = "KOCKPIT_SDK_AWS_REGION"
      value = var.aws_region
    },
    {
      name  = "aws.region"
      value = var.aws_region
    },
    {
      name  = "KOCKPIT_ENV"
      value = var.kockpit_env
    },
    {
      name  = "SPRING_PROFILES_ACTIVE"
      value = "aws"
    },
    {
      name  = "OPENSEARCH_ENDPOINTS"
      value = var.opensearch_endpoints
    },
    {
      name  = "kockpit.audit.stream.opensearch.endpoints"
      value = var.opensearch_endpoints
    },
    {
      name  = "kockpit.aws.s3.bucket"
      value = var.kockpit_data_s3_bucket
    },
    {
      name  = "kockpit.manifests.aws.s3.bucket"
      value = var.kockpit_manifests_s3_bucket
    },
    {
      name  = "kockpit.manifests.aws.region"
      value = var.aws_region
    }
  ]

  # Conditionally add Kinesis stream name if provided
  kinesis_env_vars = var.kinesis_stream_name != "" ? [
    {
      name  = "kockpit.audit.notification.kinesis.stream_name"
      value = var.kinesis_stream_name
    }
  ] : []

  # Combine all environment variables
  environment_variables = concat(
    local.base_environment_variables,
    local.kinesis_env_vars,
    var.additional_environment_variables
  )
}
