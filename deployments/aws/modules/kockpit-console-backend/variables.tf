# General Configuration
variable "aws_region" {
  description = "AWS region where resources will be created"
  type        = string
  default     = "eu-west-1"
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

variable "aws_lb_listener_arn" {
  description = "ARN of the existing ALB listener the path-based routing rule attaches to"
  type        = string
}

variable "path_routing_patterns" {
  description = "ALB listener rule path patterns routed to this app"
  type        = list(string)
  default     = []
}

variable "listener_rule_priority" {
  description = "Priority for the ALB listener rule this module creates. Leave unset (null) to let AWS auto-assign the lowest available priority."
  type        = number
  default     = null
}

variable "lb_security_group_id" {
  description = "Security group ID of the load balancer"
  type        = string
}

# Container Configuration
variable "image_tag" {
  description = "Docker image tag for the kockpit backend application"
  type        = string
  default     = "latest"
}

locals {
  container_image = "ghcr.io/mahieddine-ichir/kockpit/kockpit-backend-application-aws:${var.image_tag}"
}

variable "container_port" {
  description = "Port the container listens on"
  type        = number
  default     = 8080
}

variable "cpu_architecture" {
  description = "CPU architecture for the ECS task (ARM64 or X86_64)"
  type        = string
  default     = "X86_64"
  validation {
    condition     = contains(["ARM64", "X86_64"], var.cpu_architecture)
    error_message = "CPU architecture must be either ARM64 or X86_64."
  }
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
  default     = "/actuator/health"
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

variable "kms_key_arn" {
  description = "ARN of the KMS key to grant GenerateDataKey and Decrypt permissions (optional)"
  type        = string
  default     = null
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
  environment_variables = merge(
    {
      KOCKPIT_AWS_REGION                          = var.aws_region
      KOCKPIT_SDK_AWS_REGION                      = var.aws_region
      "aws.region"                                = var.aws_region
      KOCKPIT_ENV                                 = var.kockpit_env
      SPRING_PROFILES_ACTIVE                      = "aws"
      OPENSEARCH_ENDPOINTS                        = var.opensearch_endpoints
      "kockpit.audit.stream.opensearch.endpoints" = var.opensearch_endpoints
      "kockpit.aws.s3.bucket"                     = var.kockpit_data_s3_bucket
      "kockpit.manifests.aws.s3.bucket"           = var.kockpit_manifests_s3_bucket
      "kockpit.manifests.aws.region"              = var.aws_region
    },
    var.kinesis_stream_name != "" ? {
      "kockpit.audit.notification.kinesis.stream_name" = var.kinesis_stream_name
    } : {},
    { for e in var.additional_environment_variables : e.name => e.value }
  )
}
