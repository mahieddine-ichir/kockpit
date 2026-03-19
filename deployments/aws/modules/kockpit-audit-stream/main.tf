terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 4.59.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# Data sources for existing infrastructure
data "aws_vpc" "main" {
  id = var.vpc_id
}

data "aws_ecs_cluster" "main" {
  cluster_name = var.ecs_cluster_name
}

data "aws_caller_identity" "current" {}

locals {
  private_subnet_ids       = var.private_subnet_ids
  health_check_port_number = var.health_check_port == "traffic-port" ? var.container_port : tonumber(var.health_check_port)
}

# IAM role for ECS task execution
resource "aws_iam_role" "ecs_task_execution_role" {
  name = "${var.service_name}-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# IAM role for ECS task (application runtime)
resource "aws_iam_role" "ecs_task_role" {
  name = "${var.service_name}-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  tags = var.tags
}

# IAM policy for Kinesis consumer access
resource "aws_iam_role_policy" "kinesis_consumer_policy" {
  name = "${var.service_name}-kinesis-consumer"
  role = aws_iam_role.ecs_task_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "kinesis:GetRecords",
          "kinesis:GetShardIterator",
          "kinesis:DescribeStream",
          "kinesis:DescribeStreamSummary",
          "kinesis:ListShards",
          "kinesis:ListStreams",
          "kinesis:SubscribeToShard"
        ]
        Resource = "arn:aws:kinesis:${var.aws_region}:${data.aws_caller_identity.current.account_id}:stream/${var.kinesis_stream_name}"
      }
    ]
  })
}

# IAM policy for DynamoDB access (KCL lease/checkpoint table)
resource "aws_iam_role_policy" "dynamodb_kcl_policy" {
  name = "${var.service_name}-dynamodb-kcl"
  role = aws_iam_role.ecs_task_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "dynamodb:CreateTable",
          "dynamodb:PutItem",
          "dynamodb:GetItem",
          "dynamodb:UpdateItem",
          "dynamodb:DeleteItem",
          "dynamodb:Scan",
          "dynamodb:Query",
          "dynamodb:DescribeTable",
          "dynamodb:DescribeTimeToLive",
          "dynamodb:UpdateTimeToLive",
          "dynamodb:TagResource",
          "dynamodb:DescribeContributorInsights",
          "dynamodb:UpdateContributorInsights",
          "dynamodb:DescribeContinuousBackups"
        ]
        Resource = "arn:aws:dynamodb:${var.aws_region}:${data.aws_caller_identity.current.account_id}:table/${var.kinesis_app_name}"
      }
    ]
  })
}

# IAM policy for OpenSearch access
resource "aws_iam_role_policy" "opensearch_policy" {
  name = "${var.service_name}-opensearch"
  role = aws_iam_role.ecs_task_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "es:ESHttpGet",
          "es:ESHttpPost",
          "es:ESHttpPut",
          "es:ESHttpDelete",
          "es:ESHttpHead",
          "es:DescribeElasticsearchDomains",
          "es:ListDomainNames"
        ]
        Resource = "*"
      }
    ]
  })
}

# Security group for ECS service
resource "aws_security_group" "ecs_service" {
  name        = "${var.service_name}-sg"
  description = "Security group for ${var.service_name} ECS service"
  vpc_id      = data.aws_vpc.main.id

  ingress {
    from_port   = var.container_port
    to_port     = var.container_port
    protocol    = "tcp"
    cidr_blocks = [data.aws_vpc.main.cidr_block]
  }

  # Additional ingress rule for health check port if different from container port
  dynamic "ingress" {
    for_each = local.health_check_port_number != var.container_port ? [1] : []
    content {
      from_port   = local.health_check_port_number
      to_port     = local.health_check_port_number
      protocol    = "tcp"
      cidr_blocks = [data.aws_vpc.main.cidr_block]
    }
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, {
    Name = "${var.service_name}-sg"
  })
}

resource "aws_security_group_rule" "lb_to_ecs_container_port_ingress" {
  type                     = "ingress"
  from_port                = var.container_port
  to_port                  = var.container_port
  protocol                 = "tcp"
  source_security_group_id = var.lb_security_group_id
  security_group_id        = aws_security_group.ecs_service.id
  description              = "Allow LB to reach ECS service on container port ${var.container_port}"

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_security_group_rule" "lb_to_ecs_container_port_egress" {
  type                     = "egress"
  from_port                = var.container_port
  to_port                  = var.container_port
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.ecs_service.id
  security_group_id        = var.lb_security_group_id
  description              = "Allow load balancer to reach ECS service on container port ${var.container_port}"
}

resource "aws_security_group_rule" "lb_to_ecs_health_check_port_ingress" {
  count                    = local.health_check_port_number != var.container_port ? 1 : 0
  type                     = "ingress"
  from_port                = local.health_check_port_number
  to_port                  = local.health_check_port_number
  protocol                 = "tcp"
  source_security_group_id = var.lb_security_group_id
  security_group_id        = aws_security_group.ecs_service.id
  description              = "Allow LB health check on port ${local.health_check_port_number}"
}

resource "aws_security_group_rule" "lb_to_ecs_health_check_port_egress" {
  count                    = local.health_check_port_number != var.container_port ? 1 : 0
  type                     = "egress"
  from_port                = local.health_check_port_number
  to_port                  = local.health_check_port_number
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.ecs_service.id
  security_group_id        = var.lb_security_group_id
  description              = "Allow LB to reach ECS health check port ${local.health_check_port_number}"
}

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "main" {
  name              = "/ecs/${var.service_name}"
  retention_in_days = var.log_retention_days

  tags = var.tags
}

# ECS Task Definition
resource "aws_ecs_task_definition" "main" {
  family                   = var.service_name
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.cpu
  memory                   = var.memory
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  runtime_platform {
    cpu_architecture        = var.cpu_architecture
    operating_system_family = "LINUX"
  }

  container_definitions = jsonencode([
    {
      name  = var.service_name
      image = local.container_image

      portMappings = concat(
        [
          {
            containerPort = var.container_port
            protocol      = "tcp"
          }
        ],
        local.health_check_port_number != var.container_port ? [
          {
            containerPort = local.health_check_port_number
            protocol      = "tcp"
          }
        ] : []
      )

      environment = local.environment_variables

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.main.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
        }
      }

      essential = true
    }
  ])

  tags = var.tags
}

# Target Group
resource "aws_lb_target_group" "main" {
  name        = "${var.service_name}-tg"
  port        = var.container_port
  protocol    = "HTTP"
  vpc_id      = data.aws_vpc.main.id
  target_type = "ip"

  health_check {
    enabled             = true
    healthy_threshold   = 3
    unhealthy_threshold = var.health_check_unhealthy_threshold
    timeout             = var.health_check_timeout
    interval            = var.health_check_interval
    path                = var.health_check_path
    matcher             = "200"
    port                = var.health_check_port
    protocol            = "HTTP"
  }

  tags = merge(var.tags, {
    Name = "${var.service_name}-tg"
  })
}

# ALB Listener Rule (attaches to an existing listener)
resource "aws_lb_listener_rule" "main" {
  listener_arn = var.lb_listener_arn
  priority     = var.listener_rule_priority

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.main.arn
  }

  condition {
    path_pattern {
      values = var.path_patterns
    }
  }

  dynamic "condition" {
    for_each = length(var.http_methods) > 0 ? [1] : []
    content {
      http_request_method {
        values = var.http_methods
      }
    }
  }

  tags = var.tags
}

# ECS Service
resource "aws_ecs_service" "main" {
  name            = var.service_name
  cluster         = data.aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.main.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = local.private_subnet_ids
    security_groups  = [aws_security_group.ecs_service.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.main.arn
    container_name   = var.service_name
    container_port   = var.container_port
  }

  depends_on = [aws_lb_listener_rule.main]

  tags = var.tags
}
