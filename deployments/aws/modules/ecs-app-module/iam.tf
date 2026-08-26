// task execution role policy document
data "aws_iam_policy_document" "ecs_task_execution_role" {
  statement {
    sid    = "AllowECSToAuthenticateToECRInCentralAccount"
    effect = "Allow"
    # ecr:GetAuthorizationToken does not support resource-level permissions; AWS requires "*" here.
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    sid    = "AllowECSToPullAppImage"
    effect = "Allow"

    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage"
    ]

    resources = var.ecr_repository_arns
  }

  statement {
    sid    = "AllowECSToWriteLogsToCloudWatchLogs"
    effect = "Allow"

    actions = [
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]

    resources = compact([
      "${aws_cloudwatch_log_group.api.arn}:*",
      var.sidecar_amazon_ssm_agent_enable ? "${aws_cloudwatch_log_group.ssm_agent_cloudwatch[0].arn}:*" : "",
    ])
  }
}


data "aws_iam_policy_document" "app_ecs_tasks_assume_role" {
  statement {
    sid    = "AllowECSTasksToAssumeRole"
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

// task execution role
resource "aws_iam_role" "ecs_task_execution" {
  name               = "ecs_task_execution-${var.application}-${var.env}"
  assume_role_policy = data.aws_iam_policy_document.app_ecs_tasks_assume_role.json
  tags               = var.tags
}

// task execution role policy
resource "aws_iam_role_policy" "ecs_task_execution" {
  name   = "ecs_task_execution-${var.application}-${var.env}"
  role   = aws_iam_role.ecs_task_execution.name
  policy = data.aws_iam_policy_document.ecs_task_execution_role.json
}

// ssm task execution policy
resource "aws_iam_role_policy" "ssm_ecs_task_exec_cpu_stress" {
  count = var.sidecar_amazon_ssm_agent_enable ? 1 : 0
  name  = "ssm-task-exec-cpu-stress-${var.application}-${var.env}"
  role  = aws_iam_role.ecs_task_execution.name
  policy = jsonencode({
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "ssm:CreateActivation"
        ],
        # ssm:CreateActivation creates a not-yet-existing resource, so it can't be ARN-scoped; AWS requires "*".
        "Resource" : "*"
      }
    ],
    "Version" : "2012-10-17"
  })
}

// task role policy document
data "aws_iam_policy_document" "ecs_task_role_fargate" {
  //FARGATEONLY
  count = var.launch_type == "FARGATE" ? 1 : 0
  statement {
    sid     = "AllowServiceToAccessSecretsFromSSM"
    effect  = "Allow"
    actions = ["ssm:GetParametersByPath"]

    resources = [
      "arn:aws:ssm:${var.region}:${var.account_id}:parameter/${var.application}/*",
    ]
  }

  statement {
    sid       = "AllowAccessToKMSForDecryptingSSMParameters"
    effect    = "Allow"
    actions   = ["kms:Decrypt"]
    resources = ["arn:aws:kms:${var.region}:${var.account_id}:alias/aws/ssm"]
  }

  statement {
    sid    = "AllowAccessToServiceDiscovery"
    effect = "Allow"
    # ListServices/ListInstances don't support resource-level permissions; AWS requires "*" here.
    actions   = ["servicediscovery:ListServices", "servicediscovery:ListInstances"]
    resources = ["*"]
  }

  dynamic "statement" {
    for_each = length(var.s3_buckets_arns) >= 1 ? [1] : []
    content {
      sid       = "AllowAccessToS3ReadAndWrite"
      effect    = "Allow"
      actions   = ["s3:GetObject", "s3:PutObject", "s3:DeleteObject", "s3:ListBucket"]
      resources = var.s3_buckets_arns
    }
  }

  statement {
    sid    = "AllowWriteToCloudWatch"
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents",
      "logs:DescribeLogStreams"
    ]
    resources = [
      "arn:aws:logs:${var.region}:${var.account_id}:log-group:${var.application}*:*"
    ]
  }

}

data "aws_iam_policy_document" "ecs_task_role_ec2" {
  //EC2ONLY
  count = var.launch_type == "FARGATE" ? 0 : 1
  statement {
    sid     = "AllowServiceToAccessSecretsFromSSM"
    effect  = "Allow"
    actions = ["ssm:GetParametersByPath"]

    resources = [
      "arn:aws:ssm:${var.region}:${var.account_id}:parameter/${var.application}/*",
    ]
  }

  statement {
    sid       = "AllowAccessToKMSForDecryptingSSMParameters"
    effect    = "Allow"
    actions   = ["kms:Decrypt"]
    resources = ["arn:aws:kms:${var.region}:${var.account_id}:alias/aws/ssm"]
  }

  statement {
    sid    = "AllowAccessToServiceDiscovery"
    effect = "Allow"
    # ListServices/ListInstances don't support resource-level permissions; AWS requires "*" here.
    actions   = ["servicediscovery:ListServices", "servicediscovery:ListInstances"]
    resources = ["*"]
  }
}

// task role
resource "aws_iam_role" "ecs_task" {
  name               = "ecs_task-${var.application}-${var.env}"
  assume_role_policy = data.aws_iam_policy_document.app_ecs_tasks_assume_role.json
  tags               = var.tags
}

// task role policy
resource "aws_iam_role_policy" "ecs_task" {
  name   = "ecs_task-${var.application}-${var.env}"
  role   = aws_iam_role.ecs_task.name
  policy = var.launch_type == "FARGATE" ? data.aws_iam_policy_document.ecs_task_role_fargate[0].json : data.aws_iam_policy_document.ecs_task_role_ec2[0].json
}

// ssm task policy
resource "aws_iam_role_policy" "ssm_ecs_task_cpu_stress" {
  count = var.sidecar_amazon_ssm_agent_enable ? 1 : 0
  name  = "ssm-task-cpu-stress-${var.application}-${var.env}"
  role  = aws_iam_role.ecs_task.name
  policy = jsonencode({
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "ssm:AddTagsToResource",
          "ssm:CreateActivation",
          "ssm:DescribeActivations"
        ],
        # These act on not-yet-existing or non-ARN activation resources; AWS requires "*" here.
        "Resource" : "*"
      },
      {
        "Effect" : "Allow",
        "Action" : "iam:PassRole",
        "Resource" : "arn:aws:iam::${var.account_id}:role/${aws_iam_role.ssm_server_role[0].name}"
      }
    ],
    "Version" : "2012-10-17"
  })
}
