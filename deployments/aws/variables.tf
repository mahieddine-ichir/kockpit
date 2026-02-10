# AWS Configuration
variable "aws_region" {
  description = "AWS region where resources will be created"
  type        = string
  default     = "us-west-2"
}

# Required infrastructure inputs (data sources)
variable "vpc_id" {
  description = "ID of the existing VPC"
  type        = string
}

variable "private_subnet_names" {
  description = "Names of the private subnets where ECS tasks will run"
  type        = list(string)
}

variable "ecs_cluster_name" {
  description = "Name of the existing ECS cluster"
  type        = string
}

variable "load_balancer_arn" {
  description = "ARN of the existing load balancer"
  type        = string
}

variable "s3_bucket_name" {
  description = "Name of the S3 bucket the ECS service needs access to"
  type        = string
}

# Container configuration
variable "container_image" {
  description = "Docker image for the kockpit-console-backend service"
  type        = string
  default     = "ghcr.io/mahieddine-ichir/kockpit/kockpit-backend-application-aws:latest"
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

# Kockpit-specific configuration
variable "kockpit_env" {
  description = "Kockpit environment (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "opensearch_endpoints" {
  description = "OpenSearch cluster endpoints"
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

variable "additional_environment_variables" {
  description = "Additional environment variables to pass to the container"
  type = list(object({
    name  = string
    value = string
  }))
  default = []
}

# Load balancer listener rule (optional)
variable "create_listener_rule" {
  description = "Whether to create a load balancer listener rule"
  type        = bool
  default     = false
}

variable "load_balancer_listener_arn" {
  description = "ARN of the load balancer listener (required if create_listener_rule is true)"
  type        = string
  default     = ""
}

variable "listener_rule_priority" {
  description = "Priority for the load balancer listener rule"
  type        = number
  default     = 100
}

# Tags
variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default = {
    Environment = "production"
    Project     = "kockpit"
    ManagedBy   = "terraform"
    Service     = "console-backend"
  }
}