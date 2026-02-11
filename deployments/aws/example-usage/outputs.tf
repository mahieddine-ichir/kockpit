# Outputs from the Kockpit ECS service module

output "service_name" {
  description = "Name of the ECS service"
  value       = module.kockpit_console_backend.ecs_service_name
}

output "service_arn" {
  description = "ARN of the ECS service"
  value       = module.kockpit_console_backend.ecs_service_arn
}

output "target_group_arn" {
  description = "ARN of the target group (for ALB integration)"
  value       = module.kockpit_console_backend.target_group_arn
}

output "service_url" {
  description = "Service URL through load balancer"
  value       = module.kockpit_console_backend.service_url
}

output "security_group_id" {
  description = "Security group ID for the ECS service"
  value       = module.kockpit_console_backend.security_group_id
}

output "log_group_name" {
  description = "CloudWatch log group name"
  value       = module.kockpit_console_backend.log_group_name
}

# Useful for integration with other resources
output "task_role_arn" {
  description = "Task role ARN (has S3 permissions)"
  value       = module.kockpit_console_backend.task_role_arn
}

# Console Frontend Outputs
output "console_url" {
  description = "URL to access the Kockpit Console"
  value       = module.kockpit_console.console_url
}

output "console_api_url" {
  description = "API URL through CloudFront (same domain as console)"
  value       = "${module.kockpit_console.console_url}/backend"
}

output "cloudfront_distribution_id" {
  description = "CloudFront distribution ID"
  value       = module.kockpit_console.cloudfront_distribution_id
}

output "s3_bucket_name" {
  description = "S3 bucket name for console UI"
  value       = module.kockpit_console.s3_bucket_id
}

# Integration Summary
output "integration_summary" {
  description = "Summary of the complete integration"
  value = {
    console_frontend_url = module.kockpit_console.console_url
    api_through_cloudfront = "${module.kockpit_console.console_url}/backend"
    direct_api_url = module.kockpit_console_backend.service_url
    architecture = "Frontend (CloudFront + S3) → API Proxy → Backend (ALB + ECS)"
  }
}