variable "region" {
  default     = "eu-west-1"
  description = "Ireland AWS region"
}
variable "env" {
  default = ""
}
variable "tags" {
  default = {}
  type    = map(string)
}
variable "stack" {
}
variable "account_id" {}

# -----ECS and APP ----
variable "application" {
  description = "Application name"
}
variable "threshold_high_cpu" {
  default     = "60"
  description = "Threshold for high cpu default 60"
}
variable "threshold_low_cpu" {
  default     = "30"
  description = "Threshold for low cpu default 30"
}
variable "threshold_high_ram" {
  default     = "70"
  description = "Threshold for high ram default 70"
}
variable "launch_type" {
  default     = "FARGATE"
  description = "A set of launch types, valid value EC2 or FARGATE"
}
variable "asg_min_capacity" {
  default     = 1
  description = "AutoScalling min Capacity default 1"
}
variable "asg_max_capacity" {
  default     = 4
  description = "AutoScalling max Capacity default 4"
}
variable "desired_count" {
  default     = 1
  description = "AutoScalling desired count Capacity default 1"
}

variable "log_retention_in_days" {
  default = 7
}

# ECS
variable "aws_ecs_cluster_id" {}
variable "aws_ecs_cluster_name" {}
variable "aws_ecs_platform_version" {
  default = "1.4.0"
}

#------- VPC ------
variable "vpc" {}
variable "subnets" {
  description = "the subnets used for the application task"
}

# LB
variable "aws_lb_listener_arn" {}
variable "path_routing_patterns" {}
variable "http_method_conditions" {
  default = []
}
variable "lb_security_group_id" {
}

# Security
variable "in_security_groups" {
  default = []
}
variable "service_security_groups" {
  default = []
}

# TASK
variable "task_requires_compatibilities" {
  default = ["FARGATE"]
}
variable "task_image_url" {}
variable "task_cpu" {
  default = 1024
}
variable "task_memory" {
  default = 2048
}
variable "operating_system_family" {
  default = "LINUX"
}
variable "cpu_architecture" {
  default = "X86_64"
}

//EC2ONLY
variable "ulimit_soft" {
  default = 65535
}
//EC2ONLY
variable "ulimit_hard" {
  default = 65535
}

variable "secrets" {
  default = {}
  type    = map(any)
}

variable "environment_variables" {
  default = {}
  type    = map(any)
}

# SERVICE
variable "aws_service_discovery_private_dns_namespace" {}

variable "service_dns_config_ttl" {
  default = 10
}

variable "service_dns_config_routing_policy" {
  default = "MULTIVALUE"
}

variable "service_failure_threshold" {
  default = 1
}

variable "service_port" {
  default = 5000
}
variable "service_protocol" {
  default = "HTTP"
}
variable "service_deregistration_delay" {
  default = 30
}
variable "service_healthcheck_path" {
  default = "/actuator/health"
}
variable "service_healthcheck_matcher" {
  default = "200"
}
variable "service_healthcheck_interval" {
  default = 10
}
variable "service_healthcheck_healthy_threshold" {
  default = 2
}
variable "service_healthcheck_unhealthy_threshold" {
  default = 3
}
variable "service_healthcheck_timeout" {
  default = 8
}
variable "service_launch_type" {
  default = "FARGATE"
}

variable "deployment_minimum_healthy_percent" {
  default = 0
}
variable "deployment_maximum_healthy_percent" {
  default = 100
}

variable "kms_secret_arn" {
  type = string
}
#S3
variable "s3_buckets_arns" {
  default = []
}

#LOG
variable "log_mode" {
  default = "non-blocking"
}
variable "log_buffer_size" {
  default = "25m"
}

#Amazon SSM agent
variable "sidecar_amazon_ssm_agent_enable" {
  type    = bool
  default = false
}

variable "enable_execute_command" {
  type    = bool
  default = false
}

variable "enable_code_deploy" {
  type    = bool
  default = false
}

variable "create_listener_rule" {
  description = "Whether the module creates its own (unauthenticated, forward-only) ALB listener rule for path_routing_patterns. Set to false when the caller adds its own listener rule instead (e.g. to gate the app behind authenticate-cognito before forwarding to this module's target group)."
  type        = bool
  default     = true
}

variable "create_cognito_auth_listener_rule" {
  description = "Whether the module creates an ALB listener rule that gates path_routing_patterns behind Cognito authentication before forwarding to this module's target group. Use instead of create_listener_rule when the app must be behind SSO. Kept as an in-module resource (rather than the caller creating its own rule) so the ECS service can safely depend_on it without crossing the module boundary, which is required for AWS to accept the ECS service (target group must already be attached to a load balancer) and which a caller-side listener rule cannot do without creating a dependency cycle back through this module's target group output."
  type        = bool
  default     = false
}

variable "cognito_user_pool_arn" {
  type    = string
  default = ""
}

variable "cognito_user_pool_client_id" {
  type    = string
  default = ""
}

variable "cognito_user_pool_domain" {
  type    = string
  default = ""
}

variable "cognito_session_cookie_name" {
  type    = string
  default = "AWSELBAuthSessionCookie"
}

variable "enable_periodic_scaling" {
  type    = bool
  default = false
}

variable "periodic_upscale_cron" {
  type    = string
  default = "cron(20 18 ? * MON-FRI *)"
}

variable "periodic_downscale_cron" {
  type    = string
  default = "cron(55 19 ? * MON-FRI *)"
}

variable "periodic_upscale_min_capacity" {
  type    = number
  default = 3
}

variable "periodic_upscale_max_capacity" {
  type    = number
  default = 6
}

variable "periodic_downscale_min_capacity" {
  type    = number
  default = 1
}

variable "periodic_downscale_max_capacity" {
  type    = number
  default = 1
}

variable "deployment_bucket_name" {
  type    = string
  default = ""
}

variable "aws_lb_test_listener_arn" {
  description = "ALB test listener arn"
  type        = string
  default     = ""
}

variable "deployment_config_name" {
  default     = "CodeDeployDefault.ECSCanary10Percent15Minutes"
  description = "the deployment configuration specifies how traffic is shifted to the updated Amazon ECS task set. You can shift traffic using a canary, linear, or all-at-once deployment configuration. CodeDeployDefault.ECSLinear10PercentEvery1Minutes/CodeDeployDefault.ECSCanary10Percent5Minutes/CodeDeployDefault.ECSLinear10PercentEvery3Minutes..."
  type        = string
}

variable "wait_time_in_minutes" {
  default     = 60
  description = "The number of minutes to wait before the status of a blue/green deployment changed to Stopped if rerouting is not started manually"
  type        = number
}

variable "termination_wait_time_in_minutes" {
  default     = 30
  description = "The number of minutes to wait after a successful blue/green deployment before terminating instances from the original environment."
  type        = number
}
