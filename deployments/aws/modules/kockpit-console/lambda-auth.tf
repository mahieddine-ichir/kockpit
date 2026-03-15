# Lambda@Edge function for Cognito authentication
resource "aws_iam_role" "lambda_edge_role" {
  count = var.enable_cognito_auth ? 1 : 0
  name  = "${var.service_name}-${var.kockpit_env}-lambda-edge-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = [
            "lambda.amazonaws.com",
            "edgelambda.amazonaws.com"
          ]
        }
      }
    ]
  })

  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "lambda_edge_basic" {
  count      = var.enable_cognito_auth ? 1 : 0
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  role       = aws_iam_role.lambda_edge_role[0].name
}

# Lambda function for JWT validation
resource "aws_lambda_function" "cognito_auth" {
  count         = var.enable_cognito_auth ? 1 : 0
  provider      = aws.us_east_1  # Lambda@Edge must be in us-east-1
  filename      = "${path.module}/lambda-auth.zip"
  function_name = "${var.service_name}-${var.kockpit_env}-cognito-auth"
  role          = aws_iam_role.lambda_edge_role[0].arn
  handler       = "index.handler"
  runtime       = "nodejs18.x"
  publish       = true  # Required for Lambda@Edge
  timeout       = 5

  depends_on = [data.archive_file.lambda_auth_zip]

  tags = var.tags
}

# Download pre-built lambda zip (node_modules + lambda-auth.js) from CI release
resource "null_resource" "lambda_download" {
  count = var.enable_cognito_auth ? 1 : 0

  triggers = {
    zip_url = var.lambda_auth_zip_url
  }

  provisioner "local-exec" {
    command = "mkdir -p ${path.module}/lambda-build && curl -L -o ${path.module}/lambda-build/lambda-auth-prebuilt.zip ${var.lambda_auth_zip_url} && cd ${path.module}/lambda-build && unzip -o lambda-auth-prebuilt.zip && rm lambda-auth-prebuilt.zip"
  }

  provisioner "local-exec" {
    when    = destroy
    command = "rm -rf ${path.module}/lambda-build"
  }
}

# Render lambda-auth.js template into index.js with environment-specific values
resource "local_file" "lambda_index" {
  count    = var.enable_cognito_auth ? 1 : 0
  content  = templatefile("${path.module}/lambda-build/lambda-auth.js", {
    cognito_user_pool_id  = local.cognito_user_pool_id
    cognito_region        = local.cognito_region
    cognito_client_id     = local.cognito_client_id
    cognito_client_secret = local.cognito_client_secret
    cognito_domain        = local.cognito_domain
  })
  filename = "${path.module}/lambda-build/index.js"

  depends_on = [null_resource.lambda_download]
}

# Create the Lambda function zip
data "archive_file" "lambda_auth_zip" {
  count       = var.enable_cognito_auth ? 1 : 0
  type        = "zip"
  output_path = "${path.module}/lambda-auth.zip"
  source_dir  = "${path.module}/lambda-build"

  excludes = ["lambda-auth.js"]

  depends_on = [local_file.lambda_index]
}
