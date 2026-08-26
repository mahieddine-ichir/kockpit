resource "aws_iam_role_policy" "ecs_task_execution_secret_policy" {
  count  = length(var.secrets) > 0 ? 1 : 0
  name   = "ecs_task-${var.application}-secrets"
  role   = aws_iam_role.ecs_task_execution.name
  policy = data.aws_iam_policy_document.ecs_task_execution_access_readsecret[0].json
}

data "aws_iam_policy_document" "ecs_task_execution_access_readsecret" {
  count = length(var.secrets) > 0 ? 1 : 0
  statement {
    sid    = "AllowEcsTaskExecutionToReadSecretValue"
    effect = "Allow"
    actions = [
      "secretsmanager:GetSecretValue"
    ]
    resources = values(var.secrets)
  }
  statement {
    sid    = "AllowEcsTaskExecutionToAccessKMSForSecretAccess"
    effect = "Allow"
    actions = [
      "kms:GenerateDataKey",
      "kms:Decrypt"
    ]
    resources = [var.kms_secret_arn]
  }
}
