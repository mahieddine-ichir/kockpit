# AWS Configuration
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "eu-west-1"
}

variable "environment" {
  description = "Environment name (dev, staging, production)"
  type        = string
}

# Existing Infrastructure (Data Sources)
variable "vpc_id" {
  description = "VPC ID where ECS service will be deployed"
  type        = string
}

variable "private_subnet_names" {
  description = "Names of private subnets for ECS tasks"
  type        = list(string)
}

variable "ecs_cluster_name" {
  description = "Name of existing ECS cluster"
  type        = string
}

variable "load_balancer_arn" {
  description = "ARN of existing Application Load Balancer"
  type        = string
}

variable "s3_bucket_name" {
  description = "S3 bucket name for IAM permissions (can be same as kockpit_data_s3_bucket)"
  type        = string
}

# Kockpit Configuration
variable "opensearch_endpoints" {
  description = "OpenSearch cluster endpoints"
  type        = string
}

variable "kockpit_data_s3_bucket" {
  description = "S3 bucket for Kockpit data storage"
  type        = string
}

variable "kockpit_manifests_s3_bucket" {
  description = "S3 bucket for Kockpit manifests"
  type        = string
}

variable "kinesis_stream_name" {
  description = "Kinesis stream for audit notifications (optional)"
  type        = string
  default     = ""
}

# Container Configuration
variable "container_image" {
  description = "Docker image for Kockpit backend"
  type        = string
  default     = "ghcr.io/mahieddine-ichir/kockpit/kockpit-backend-application-aws:latest"
}

variable "cpu" {
  description = "CPU units (1024 = 1 vCPU)"
  type        = number
  default     = 512
}

variable "memory" {
  description = "Memory in MB"
  type        = number
  default     = 1024
}

variable "desired_count" {
  description = "Number of ECS service instances"
  type        = number
  default     = 2
}

# Load Balancer Integration (Optional)
variable "create_listener_rule" {
  description = "Create ALB listener rule for routing"
  type        = bool
  default     = false
}

variable "load_balancer_listener_arn" {
  description = "ALB listener ARN (required if create_listener_rule = true)"
  type        = string
  default     = ""
}

variable "listener_rule_priority" {
  description = "Priority for ALB listener rule"
  type        = number
  default     = 100
}

# Additional Configuration
variable "additional_env_vars" {
  description = "Additional environment variables"
  type = list(object({
    name  = string
    value = string
  }))
  default = []
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default = {
    ManagedBy = "terraform"
    Project   = "kockpit"
  }
}