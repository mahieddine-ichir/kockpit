resource "aws_lb_listener_rule" "app_path_based_routing" {
  count        = var.create_listener_rule && !var.enable_code_deploy ? 1 : 0
  listener_arn = var.aws_lb_listener_arn

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }

  condition {
    path_pattern {
      values = var.path_routing_patterns
    }
  }
  dynamic "condition" {
    for_each = length(var.http_method_conditions) >= 1 ? [1] : []
    content {
      http_request_method {
        values = var.http_method_conditions
      }
    }
  }

  lifecycle {
    ignore_changes = [action]
  }
}
# in order to add ignore_changes into lifecycle, I need to add another aws_lb_listener_rule for when we deploy using codedeploy
resource "aws_lb_listener_rule" "app_path_based_routing_with_cd" {
  count        = var.create_listener_rule && var.enable_code_deploy ? 1 : 0
  listener_arn = var.aws_lb_listener_arn

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }

  condition {
    path_pattern {
      values = var.path_routing_patterns
    }
  }
  dynamic "condition" {
    for_each = length(var.http_method_conditions) >= 1 ? [1] : []
    content {
      http_request_method {
        values = var.http_method_conditions
      }
    }
  }

  lifecycle {
    ignore_changes = [action]
  }
}

resource "aws_lb_listener_rule" "app_path_based_routing_cognito_auth" {
  count        = var.create_cognito_auth_listener_rule ? 1 : 0
  listener_arn = var.aws_lb_listener_arn

  action {
    type = "authenticate-cognito"
    authenticate_cognito {
      user_pool_arn              = var.cognito_user_pool_arn
      user_pool_client_id        = var.cognito_user_pool_client_id
      user_pool_domain           = var.cognito_user_pool_domain
      scope                      = "openid"
      on_unauthenticated_request = "authenticate"
      session_cookie_name        = var.cognito_session_cookie_name
    }
  }

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }

  condition {
    path_pattern {
      values = var.path_routing_patterns
    }
  }
  dynamic "condition" {
    for_each = length(var.http_method_conditions) >= 1 ? [1] : []
    content {
      http_request_method {
        values = var.http_method_conditions
      }
    }
  }
}

resource "aws_security_group_rule" "health_check_lb_http" {
  type                     = "egress"
  from_port                = var.service_port
  to_port                  = var.service_port
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.ecs.id
  security_group_id        = var.lb_security_group_id
  description              = "Allow access for target group health check ${var.service_port}"
}

resource "aws_lb_target_group" "app" {
  name                 = "tg-${var.application}-${var.env}"
  port                 = var.service_port
  protocol             = var.service_protocol
  vpc_id               = var.vpc.id
  target_type          = "ip"
  deregistration_delay = var.service_deregistration_delay

  health_check {
    interval            = var.service_healthcheck_interval
    path                = var.service_healthcheck_path
    matcher             = var.service_healthcheck_matcher
    healthy_threshold   = var.service_healthcheck_healthy_threshold
    unhealthy_threshold = var.service_healthcheck_unhealthy_threshold
    timeout             = var.service_healthcheck_timeout
    protocol            = var.service_protocol
  }

  tags = var.tags
}

resource "aws_lb_target_group" "lb_tg_2" {
  count                = var.enable_code_deploy ? 1 : 0
  name                 = "tg-2-${var.application}-${var.env}"
  port                 = var.service_port
  protocol             = var.service_protocol
  vpc_id               = var.vpc.id
  target_type          = "ip"
  deregistration_delay = var.service_deregistration_delay

  health_check {
    interval            = var.service_healthcheck_interval
    path                = var.service_healthcheck_path
    matcher             = var.service_healthcheck_matcher
    healthy_threshold   = var.service_healthcheck_healthy_threshold
    unhealthy_threshold = var.service_healthcheck_unhealthy_threshold
    timeout             = var.service_healthcheck_timeout
    protocol            = var.service_protocol
  }

  tags = var.tags
}


resource "aws_lb_listener_rule" "app_path_based_routing_2" {
  count        = var.create_listener_rule && var.enable_code_deploy ? 1 : 0
  listener_arn = var.aws_lb_test_listener_arn

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.lb_tg_2[0].arn
  }

  condition {
    path_pattern {
      values = var.path_routing_patterns
    }
  }
  dynamic "condition" {
    for_each = length(var.http_method_conditions) >= 1 ? [1] : []
    content {
      http_request_method {
        values = var.http_method_conditions
      }
    }
  }

  lifecycle {
    ignore_changes = [action]
  }

}
