resource "aws_service_discovery_service" "discovery" {
  count = var.enable_service_discovery ? 1 : 0
  name  = "${var.application}-${var.env}"

  dns_config {
    namespace_id = var.aws_service_discovery_private_dns_namespace.id

    dns_records {
      ttl  = var.service_dns_config_ttl
      type = "A"
    }

    routing_policy = var.service_dns_config_routing_policy
  }

  health_check_custom_config {
    failure_threshold = var.service_failure_threshold
  }

  tags = var.tags
}

resource "aws_ecs_service" "ecs_service" {
  platform_version                   = var.aws_ecs_platform_version
  name                               = "${var.application}-${var.env}"
  cluster                            = var.aws_ecs_cluster_id
  task_definition                    = var.launch_type == "FARGATE" ? "${aws_ecs_task_definition.taskdef_fargate[0].id}:${aws_ecs_task_definition.taskdef_fargate[0].revision}" : "${aws_ecs_task_definition.taskdef_ec2[0].id}:${aws_ecs_task_definition.taskdef_ec2[0].revision}"
  desired_count                      = var.desired_count
  launch_type                        = var.service_launch_type
  deployment_minimum_healthy_percent = var.deployment_minimum_healthy_percent
  deployment_maximum_percent         = var.deployment_maximum_healthy_percent
  enable_execute_command             = var.enable_execute_command

  network_configuration {
    security_groups  = concat([aws_security_group.ecs.id], var.service_security_groups)
    subnets          = var.subnets
    assign_public_ip = false
  }

  dynamic "load_balancer" {
    for_each = var.enable_load_balancer ? [1] : []
    content {
      target_group_arn = aws_lb_target_group.app[0].arn
      container_name   = "${var.application}-${var.env}"
      container_port   = var.service_port
    }
  }

  propagate_tags = "SERVICE"

  dynamic "service_registries" {
    for_each = var.enable_service_discovery ? [1] : []
    content {
      registry_arn = aws_service_discovery_service.discovery[0].arn
    }
  }

  tags = var.tags

  depends_on = [
    aws_lb_listener_rule.app_path_based_routing,
    aws_lb_listener_rule.app_path_based_routing_cognito_auth,
  ]
}

resource "aws_appautoscaling_scheduled_action" "periodic_upscale" {
  count              = var.enable_periodic_scaling ? 1 : 0
  name               = "${var.application}-${var.env}-periodic-upscale"
  service_namespace  = "ecs"
  resource_id        = aws_appautoscaling_target.service_scale_target.resource_id
  scalable_dimension = aws_appautoscaling_target.service_scale_target.scalable_dimension
  schedule           = var.periodic_upscale_cron
  timezone           = "Europe/Paris"

  scalable_target_action {
    min_capacity = var.periodic_upscale_min_capacity
    max_capacity = var.periodic_upscale_max_capacity
  }
}

resource "aws_appautoscaling_scheduled_action" "periodic_downscale" {
  count              = var.enable_periodic_scaling ? 1 : 0
  name               = "${var.application}-${var.env}-periodic-downscale"
  service_namespace  = "ecs"
  resource_id        = aws_appautoscaling_target.service_scale_target.resource_id
  scalable_dimension = aws_appautoscaling_target.service_scale_target.scalable_dimension
  schedule           = var.periodic_downscale_cron
  timezone           = "Europe/Paris"

  scalable_target_action {
    min_capacity = var.periodic_downscale_min_capacity
    max_capacity = var.periodic_downscale_max_capacity
  }
}
