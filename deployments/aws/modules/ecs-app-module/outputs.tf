output "aws_lb_target_group" {
  value = aws_lb_target_group.app
}

output "aws_lb_target_group_2" {
  value = var.enable_code_deploy ? aws_lb_target_group.lb_tg_2[0] : null
}

output "ecs_task_role" {
  value = aws_iam_role.ecs_task.name
}

#Deprecated
output "aws_iam_role_execution_name" {
  value = aws_iam_role.ecs_task_execution.name
}
output "aws_iam_role_execution" {
  value = aws_iam_role.ecs_task_execution
}
output "ecs_security_group" {
  value = aws_security_group.ecs.id
}

output "ecs_kms_key_id" {
  value = aws_kms_key.app_kms_key.id
}

output "ecs_service_name" {
  value = var.enable_code_deploy ? aws_ecs_service.ecs_service_with_codedeploy[0].name : aws_ecs_service.ecs_service[0].name
}

output "log_group_name" {
  value = aws_cloudwatch_log_group.api.name
}

output "ecs_service_arn" {
  value = var.enable_code_deploy ? aws_ecs_service.ecs_service_with_codedeploy[0].id : aws_ecs_service.ecs_service[0].id
}
