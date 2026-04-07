# General Configuration
variable "aws_region" {
  description = "AWS region where resources will be created"
  type        = string
  default     = "eu-west-1"
}

variable "service_name" {
  description = "Name of the ECS service"
  type        = string
  default     = "kockpit-kinesis-s3"
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

# Network Configuration
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

# Container Configuration
variable "image_tag" {
  description = "Docker image tag for the kinesis-s3 application"
  type        = string
  default     = "latest"
}

locals {
  container_image = "ghcr.io/mahieddine-ichir/kockpit/kockpit-kinesis-s3-application:${var.image_tag}"
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
  default     = 1
}

# Logging Configuration
variable "log_retention_days" {
  description = "CloudWatch log retention period in days"
  type        = number
  default     = 30
}

# Kinesis Consumer Configuration
variable "kinesis_stream_name" {
  description = "Name of the Kinesis stream to consume from"
  type        = string
}

variable "kinesis_app_name" {
  description = "Application name for KCL (used as the DynamoDB checkpoint table name)"
  type        = string
  default     = "kockpit-kinesis-s3"
}

variable "kinesis_read_interval_ms" {
  description = "Polling interval for Kinesis consumer in milliseconds"
  type        = number
  default     = 5000
}

variable "kinesis_shard_acquisition_interval_ms" {
  description = "Interval between shard acquisition attempts in milliseconds"
  type        = number
  default     = 30000
}

variable "kinesis_record_limit" {
  description = "Maximum number of records per Kinesis read"
  type        = number
  default     = 100
}

# S3 Output Configuration
variable "s3_bucket_name" {
  description = "Name of the S3 bucket to write audit records to"
  type        = string
}

variable "s3_key_prefix" {
  description = "Key prefix for objects written to S3"
  type        = string
  default     = "records"
}

# Observability
variable "audit_trace_enabled" {
  description = "Enable audit trace logging"
  type        = bool
  default     = false
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
  environment_variables = concat(
    [
      {
        name  = "AWS_REGION"
        value = var.aws_region
      },
      {
        name  = "KINESIS_STREAM_NAME"
        value = var.kinesis_stream_name
      },
      {
        name  = "KINESIS_APPLICATION_NAME"
        value = var.kinesis_app_name
      },
      {
        name  = "KINESIS_READ_INTERVAL_MS"
        value = tostring(var.kinesis_read_interval_ms)
      },
      {
        name  = "KINESIS_SHARD_ACQUISITION_INTERVAL_MS"
        value = tostring(var.kinesis_shard_acquisition_interval_ms)
      },
      {
        name  = "KINESIS_RECORD_LIMIT"
        value = tostring(var.kinesis_record_limit)
      },
      {
        name  = "S3_BUCKET"
        value = var.s3_bucket_name
      },
      {
        name  = "S3_KEY_PREFIX"
        value = var.s3_key_prefix
      },
      {
        name  = "AUDIT_STREAM_TRACE"
        value = tostring(var.audit_trace_enabled)
      }
    ],
    var.additional_environment_variables
  )
}