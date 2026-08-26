resource "aws_ecs_task_definition" "taskdef_ec2" {
  //EC2ONLY
  count  = var.launch_type == "FARGATE" ? 0 : 1
  family = "${var.application}-${var.env}"
  container_definitions = templatefile("${path.module}/task_def_stream.json", {
    name             = "${var.application}-${var.env}"
    application      = var.application
    image_url        = var.task_image_url
    log_group_region = var.region
    log_group_name   = aws_cloudwatch_log_group.api.name
    service_port     = var.service_port
    service_dns      = "${var.application}-${var.env}.${var.aws_service_discovery_private_dns_namespace.name}"
    ulimit_soft      = var.ulimit_soft
    ulimit_hard      = var.ulimit_hard
    account_id       = var.account_id

    secrets               = jsonencode(local.secrets)
    environment_variables = jsonencode(local.environment_variables)
  })
  requires_compatibilities = var.task_requires_compatibilities
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  task_role_arn            = aws_iam_role.ecs_task.arn
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  network_mode             = "awsvpc"

  volume {
    name = "scratch"
    docker_volume_configuration {
      driver      = "local"
      scope       = "task"
      driver_opts = {}
      labels      = {}
    }
  }

  tags = var.tags
}

resource "aws_ecs_task_definition" "taskdef_fargate" {
  //FARGATEONLY
  count  = var.launch_type == "FARGATE" ? 1 : 0
  family = "${var.application}-${var.env}"
  container_definitions = templatefile("${path.module}/task_def.json", {
    name                  = "${var.application}-${var.env}"
    application           = var.application
    image_url             = var.task_image_url
    log_group_region      = var.region
    log_group_name        = aws_cloudwatch_log_group.api.name
    service_port          = var.service_port
    service_dns           = "${var.application}-${var.env}.${var.aws_service_discovery_private_dns_namespace.name}"
    secrets               = jsonencode(local.secrets)
    environment_variables = jsonencode(local.environment_variables)
    log_mode              = var.log_mode
    log_buffer_size       = var.log_buffer_size

    #Amazon SSM agent conf
    sidecar_amazon_ssm_agent_enable = var.sidecar_amazon_ssm_agent_enable
    ssm_log_group_name              = try(aws_cloudwatch_log_group.ssm_agent_cloudwatch[0].name, "")
    ssm_role_name                   = try(aws_iam_role.ssm_server_role[0].name, "")
    ssm_log_stream_prefix           = "ssm-${var.application}-${var.env}"
  })
  requires_compatibilities = var.task_requires_compatibilities
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  task_role_arn            = aws_iam_role.ecs_task.arn
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  network_mode             = "awsvpc"
  runtime_platform {
    operating_system_family = var.operating_system_family
    cpu_architecture        = var.cpu_architecture
  }

  tags = var.tags
}
