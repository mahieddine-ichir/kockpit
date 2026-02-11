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

variable "private_subnet_ids" {
  description = "IDs of private subnets for ECS tasks"
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

variable "lb_security_group_id" {
  description = "Security group ID of the load balancer"
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

# Console Frontend Configuration
variable "console_domain_aliases" {
  description = "Custom domain names for the console (optional)"
  type        = list(string)
  default     = null
}

variable "create_ssl_certificate" {
  description = "Create SSL certificate for custom domains"
  type        = bool
  default     = true
}

variable "existing_certificate_arn" {
  description = "Existing ACM certificate ARN (optional)"
  type        = string
  default     = null
}

variable "auto_deploy_ui" {
  description = "Automatically deploy console UI files"
  type        = bool
  default     = true
}

variable "console_ui_version" {
  description = "Console UI version for cache busting"
  type        = string
  default     = "latest"
}

variable "console_ui_download_url" {
  description = "URL to download console UI distribution (AWS version with /backend API prefix)"
  type        = string
  default     = "https://github.com/mahieddine-ichir/kockpit/releases/download/console-ui-dev-latest/console-ui-aws-dist.zip"
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default = {
    ManagedBy = "terraform"
    Project   = "kockpit"
  }
}