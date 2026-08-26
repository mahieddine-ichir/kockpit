#Amazon SSM agent
resource "aws_cloudwatch_log_group" "ssm_agent_cloudwatch" {
  count             = var.sidecar_amazon_ssm_agent_enable ? 1 : 0
  name              = "ssm-agent-${var.application}-${var.env}"
  retention_in_days = var.log_retention_in_days
  tags              = var.tags
}

# API
resource "aws_cloudwatch_log_group" "api" {
  name              = "service-${var.application}-${var.env}"
  retention_in_days = var.log_retention_in_days
  tags              = var.tags
}

resource "aws_appautoscaling_target" "service_scale_target" {
  service_namespace  = "ecs"
  resource_id        = "service/${var.aws_ecs_cluster_name}/${aws_ecs_service.ecs_service.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  min_capacity       = var.asg_min_capacity
  max_capacity       = var.asg_max_capacity

  depends_on = [aws_ecs_service.ecs_service]
}

resource "aws_appautoscaling_policy" "cpu_scaling_policy" {
  name               = "${var.application}-${var.env}-cpu-scaling"
  service_namespace  = aws_appautoscaling_target.service_scale_target.service_namespace
  scalable_dimension = aws_appautoscaling_target.service_scale_target.scalable_dimension
  resource_id        = aws_appautoscaling_target.service_scale_target.resource_id
  policy_type        = "TargetTrackingScaling"

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }

    target_value       = var.threshold_high_cpu
    scale_in_cooldown  = 300
    scale_out_cooldown = 300
  }

  depends_on = [aws_appautoscaling_target.service_scale_target]
}

resource "aws_appautoscaling_policy" "memory_scaling_policy" {
  name               = "${var.application}-${var.env}-memory-scaling"
  service_namespace  = aws_appautoscaling_target.service_scale_target.service_namespace
  scalable_dimension = aws_appautoscaling_target.service_scale_target.scalable_dimension
  resource_id        = aws_appautoscaling_target.service_scale_target.resource_id
  policy_type        = "TargetTrackingScaling"

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageMemoryUtilization"
    }

    target_value       = var.threshold_high_ram
    scale_in_cooldown  = 300
    scale_out_cooldown = 300
  }

  depends_on = [aws_appautoscaling_target.service_scale_target]
}