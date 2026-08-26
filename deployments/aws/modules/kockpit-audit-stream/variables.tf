# General Configuration
variable "aws_region" {
  description = "AWS region where resources will be created"
  type        = string
  default     = "eu-west-1"
}

variable "service_name" {
  description = "Name of the ECS service"
  type        = string
  default     = "kockpit-auditstream"
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

variable "lb_listener_arn" {
  description = "ARN of the existing ALB listener to attach the routing rule to"
  type        = string
}

variable "lb_security_group_id" {
  description = "Security group ID of the load balancer"
  type        = string
}

variable "listener_rule_priority" {
  description = "Priority for the ALB listener rule"
  type        = number
  default     = 200
}

variable "path_patterns" {
  description = "Path patterns for the ALB listener rule (e.g., ['/auditservice/v2/*'])"
  type        = list(string)
  default     = ["/auditservice/v2/*"]
}

variable "http_methods" {
  description = "HTTP methods for the ALB listener rule condition (empty list = no method condition)"
  type        = list(string)
  default     = ["POST"]
}

# Container Configuration
variable "image_tag" {
  description = "Docker image tag for the audit stream application"
  type        = string
  default     = "latest"
}

locals {
  container_image = "ghcr.io/mahieddine-ichir/kockpit/kockpit-audit-stream-application-aws:${var.image_tag}"
}

variable "container_port" {
  description = "Port the container listens on"
  type        = number
  default     = 8080
}

variable "health_check_port" {
  description = "Health check port for the target group ('traffic-port' or a port number)"
  type        = string
  default     = "8090"
}

locals {
  health_check_port_number = var.health_check_port == "traffic-port" ? var.container_port : tonumber(var.health_check_port)
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

# Health Check Configuration
variable "health_check_path" {
  description = "Health check path for the target group"
  type        = string
  default     = "/actuator/health"
}

variable "health_check_timeout" {
  description = "Health check timeout in seconds"
  type        = number
  default     = 20
}

variable "health_check_interval" {
  description = "Health check interval in seconds"
  type        = number
  default     = 25
}

variable "health_check_unhealthy_threshold" {
  description = "Number of consecutive health check failures before marking unhealthy"
  type        = number
  default     = 10
}

# Logging Configuration
variable "log_retention_days" {
  description = "CloudWatch log retention period in days"
  type        = number
  default     = 30
}

# Kockpit-specific Configuration
variable "kockpit_env" {
  description = "Kockpit environment (dev, oat, pro)"
  type        = string
}

variable "opensearch_endpoints" {
  description = "OpenSearch cluster endpoint"
  type        = string
}

# Kinesis Consumer Configuration
variable "kinesis_stream_name" {
  description = "Name of the Kinesis stream to consume from"
  type        = string
}

variable "kinesis_app_name" {
  description = "Application name for KCL (used as the DynamoDB checkpoint table name)"
  type        = string
}

variable "consumer_poll_max_records" {
  description = "Maximum number of records per Kinesis consumer poll"
  type        = number
  default     = 500
}

variable "consumer_initial_position_in_stream" {
  description = "Initial position in the Kinesis stream (TRIM_HORIZON or LATEST)"
  type        = string
  default     = "TRIM_HORIZON"
  validation {
    condition     = contains(["TRIM_HORIZON", "LATEST"], var.consumer_initial_position_in_stream)
    error_message = "Must be TRIM_HORIZON or LATEST."
  }
}

variable "elasticsearch_indexation_partition_size" {
  description = "Batch size for OpenSearch indexation"
  type        = number
  default     = 50
}

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
  environment_variables = merge(
    {
      AWS_REGION                              = var.aws_region
      KOCKPIT_AUDIT_STREAM_AWS_REGION         = var.aws_region
      SERVER_PORT                             = tostring(var.container_port)
      MANAGEMENT_SERVER_PORT                  = tostring(local.health_check_port_number)
      SPRING_PROFILES_ACTIVE                  = "aws"
      KOCKPIT_ENV                             = var.kockpit_env
      ENV                                     = var.kockpit_env
      OPENSEARCH_ENDPOINTS                    = var.opensearch_endpoints
      KINESIS_STREAM_NAME                     = var.kinesis_stream_name
      KINESIS_APP_NAME                        = var.kinesis_app_name
      "CONSUMER_POLL_MAX-RECORDS"             = tostring(var.consumer_poll_max_records)
      "CONSUMER_INITIAL-POSITION-IN-STREAM"   = var.consumer_initial_position_in_stream
      ELASTICSEARCH_INDEXATION_PARTITION_SIZE = tostring(var.elasticsearch_indexation_partition_size)
      KOCKPIT_AUDIT_TRACE_ENABLED             = tostring(var.audit_trace_enabled)
    },
    { for e in var.additional_environment_variables : e.name => e.value }
  )
}
