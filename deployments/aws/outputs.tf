# Module outputs
output "ecs_service_name" {
  description = "Name of the ECS service"
  value       = module.kockpit_console_backend.ecs_service_name
}

output "ecs_service_arn" {
  description = "ARN of the ECS service"
  value       = module.kockpit_console_backend.ecs_service_arn
}

output "target_group_arn" {
  description = "ARN of the target group"
  value       = module.kockpit_console_backend.target_group_arn
}

output "security_group_id" {
  description = "ID of the ECS service security group"
  value       = module.kockpit_console_backend.security_group_id
}

output "service_url" {
  description = "URL where the service will be accessible"
  value       = module.kockpit_console_backend.service_url
}

output "log_group_name" {
  description = "Name of the CloudWatch log group"
  value       = module.kockpit_console_backend.log_group_name
}