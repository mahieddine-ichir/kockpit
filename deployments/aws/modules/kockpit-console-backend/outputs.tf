# ECS Service Outputs
output "ecs_service_name" {
  description = "Name of the ECS service"
  value       = module.ecs_app.ecs_service_name
}

output "ecs_service_arn" {
  description = "ARN of the ECS service"
  value       = module.ecs_app.ecs_service_arn
}

output "task_definition_arn" {
  description = "ARN of the ECS task definition"
  value       = module.ecs_app.ecs_task_definition_arn
}

# Target Group Outputs
output "target_group_arn" {
  description = "ARN of the target group"
  value       = module.ecs_app.aws_lb_target_group.arn
}

output "target_group_name" {
  description = "Name of the target group"
  value       = module.ecs_app.aws_lb_target_group.name
}

# Security Group Outputs
output "security_group_id" {
  description = "ID of the ECS service security group"
  value       = module.ecs_app.ecs_security_group
}

# IAM Role Outputs
output "task_execution_role_arn" {
  description = "ARN of the ECS task execution role"
  value       = module.ecs_app.aws_iam_role_execution_arn
}

output "task_role_arn" {
  description = "ARN of the ECS task role"
  value       = module.ecs_app.ecs_task_role_arn
}

# CloudWatch Log Group Output
output "log_group_name" {
  description = "Name of the CloudWatch log group"
  value       = module.ecs_app.log_group_name
}
