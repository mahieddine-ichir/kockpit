resource "aws_security_group" "ecs" {
  description = "ingress to the app task"

  vpc_id = var.vpc.id
  name   = "ecs-${var.application}-${var.env}"

  // HTTP
  ingress {
    protocol        = "tcp"
    from_port       = var.service_port
    to_port         = var.service_port
    security_groups = concat(var.in_security_groups, var.enable_load_balancer ? [var.lb_security_group_id] : [])
  }

  // Separate health check port (e.g. a management/actuator port distinct from service_port)
  dynamic "ingress" {
    for_each = local.healthcheck_port_number != var.service_port ? [1] : []
    content {
      protocol        = "tcp"
      from_port       = local.healthcheck_port_number
      to_port         = local.healthcheck_port_number
      security_groups = concat(var.in_security_groups, var.enable_load_balancer ? [var.lb_security_group_id] : [])
    }
  }

  // Output
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = var.tags
}
