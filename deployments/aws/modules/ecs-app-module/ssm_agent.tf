#Amazon SSM agent config
resource "aws_iam_role" "ssm_server_role" {
  count = var.sidecar_amazon_ssm_agent_enable ? 1 : 0
  name  = "ssm-agent-${var.application}-${var.env}"
  assume_role_policy = jsonencode({
    "Statement" : [
      {
        "Effect" : "Allow",
        "Principal" : {
          "Service" : "ssm.amazonaws.com"
        },
        "Action" : "sts:AssumeRole"
      }
    ],
    "Version" : "2012-10-17"
  })
}

data "aws_iam_policy" "ssm_server_policy" {
  count = var.sidecar_amazon_ssm_agent_enable ? 1 : 0
  name  = "AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "ssm_server_policy_attachment" {
  count      = var.sidecar_amazon_ssm_agent_enable ? 1 : 0
  role       = aws_iam_role.ssm_server_role[0].name
  policy_arn = data.aws_iam_policy.ssm_server_policy[0].arn
}

resource "aws_iam_role_policy" "ssm_clean_up_policy" {
  count = var.sidecar_amazon_ssm_agent_enable ? 1 : 0
  name  = "ssm-agent-clean-up-${var.application}-${var.env}"
  role  = aws_iam_role.ssm_server_role[0].name
  policy = jsonencode({
    "Statement" : [
      {
        "Effect" : "Allow",
        "Action" : [
          "ssm:DeleteActivation",
          "ssm:DeregisterManagedInstance"
        ],
        "Resource" : "*"
      }
    ],
    "Version" : "2012-10-17"
  })
}
