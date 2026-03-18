# Lambda@Edge function for Cognito authentication
resource "aws_iam_role" "lambda_edge_role" {
  count = var.enable_cognito_auth ? 1 : 0
  name  = "${var.service_name}-${var.kockpit_env}-lambda-edge-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = {
        Service = ["lambda.amazonaws.com", "edgelambda.amazonaws.com"]
      }
    }]
  })

  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "lambda_edge_basic" {
  count      = var.enable_cognito_auth ? 1 : 0
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  role       = aws_iam_role.lambda_edge_role[0].name
}

resource "null_resource" "deploy_lambda_auth" {
  count = var.enable_cognito_auth ? 1 : 0

  triggers = {
    always = timestamp()
  }

  provisioner "local-exec" {
    command = <<-EOT
        set -e
        BUILD_DIR=$(mktemp -d)
        # Download prebuilt zip (node_modules + lambda-auth.js)
        curl -L -o $BUILD_DIR/lambda-auth-prebuilt.zip ${var.lambda_auth_zip_url}
        cd $BUILD_DIR && unzip -o lambda-auth-prebuilt.zip

        # Render template → index.js
        sed \
          -e "s/\$${cognito_user_pool_id}/${local.cognito_user_pool_id}/g" \
          -e "s/\$${cognito_region}/${local.cognito_region}/g" \
          -e "s/\$${cognito_client_id}/${local.cognito_client_id}/g" \
          -e "s/\$${cognito_client_secret}/${local.cognito_client_secret}/g" \
          -e "s/\$${cognito_domain}/${local.cognito_domain}/g" \
          $BUILD_DIR/lambda-auth.js > $BUILD_DIR/index.js

        # Create final zip with index.js + node_modules
        cd $BUILD_DIR && zip -r /tmp/lambda-auth-final.zip index.js node_modules/

        # Deploy to Lambda (must be us-east-1)
        aws lambda update-function-code \
          --function-name ${var.service_name}-${var.kockpit_env}-cognito-auth \
          --zip-file fileb:///tmp/lambda-auth-final.zip \
          --region us-east-1

        aws lambda wait function-updated \
          --function-name ${var.service_name}-${var.kockpit_env}-cognito-auth \
          --region us-east-1

        # Publish new version and capture ARN
        NEW_ARN=$(aws lambda publish-version \
          --function-name ${var.service_name}-${var.kockpit_env}-cognito-auth \
          --region us-east-1 \
          --query 'FunctionArn' --output text)

        echo "Deployed Lambda@Edge: $NEW_ARN"
        rm -rf $BUILD_DIR /tmp/lambda-auth-final.zip
      EOT
    }

  depends_on = [aws_iam_role_policy_attachment.lambda_edge_basic]
}

data "aws_lambda_function" "cognito_auth" {
  count         = var.enable_cognito_auth ? 1 : 0
  provider      = aws.us_east_1
  function_name = "${var.service_name}-${var.kockpit_env}-cognito-auth"
  depends_on    = [null_resource.deploy_lambda_auth]
}
