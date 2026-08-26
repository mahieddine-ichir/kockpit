output "aws_lb_target_group" {
  value = var.enable_load_balancer ? aws_lb_target_group.app[0] : null
}

output "ecs_task_role" {
  value = aws_iam_role.ecs_task.name
}

output "aws_iam_role_execution_name" {
  value = aws_iam_role.ecs_task_execution.name
}

output "aws_iam_role_execution_arn" {
  value = aws_iam_role.ecs_task_execution.arn
}
output "ecs_security_group" {
  value = aws_security_group.ecs.id
}

output "ecs_kms_key_id" {
  value = aws_kms_key.app_kms_key.id
}

output "ecs_service_name" {
  value = aws_ecs_service.ecs_service.name
}

output "log_group_name" {
  value = aws_cloudwatch_log_group.api.name
}

output "ecs_service_arn" {
  value = aws_ecs_service.ecs_service.id
}
