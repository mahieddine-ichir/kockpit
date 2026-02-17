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

# Build Lambda function with dependencies
resource "null_resource" "lambda_build" {
  count = var.enable_cognito_auth ? 1 : 0

  triggers = {
    lambda_code = filemd5("${path.module}/lambda-auth.js")
    package_json = filemd5("${path.module}/lambda-package.json")
  }

  provisioner "local-exec" {
    command = <<-EOT
      mkdir -p ${path.module}/lambda-build
      cp ${path.module}/lambda-package.json ${path.module}/lambda-build/package.json
      cd ${path.module}/lambda-build
      npm install --production

      # Create the main Lambda function
      cat > index.js << 'EOF'
${templatefile("${path.module}/lambda-auth.js", {
  cognito_user_pool_id   = local.cognito_user_pool_id
  cognito_client_id      = local.cognito_client_id
  cognito_client_secret  = "Todo"
  cognito_domain         = local.cognito_domain
  cognito_region         = var.aws_region
})}
EOF
    EOT
  }

  provisioner "local-exec" {
    when = destroy
    command = "rm -rf ${path.module}/lambda-build"
  }
}

# Create the Lambda function zip
data "archive_file" "lambda_auth_zip" {
  count       = var.enable_cognito_auth ? 1 : 0
  type        = "zip"
  output_path = "${path.module}/lambda-auth.zip"
  source_dir  = "${path.module}/lambda-build"

  depends_on = [null_resource.lambda_build]
}
