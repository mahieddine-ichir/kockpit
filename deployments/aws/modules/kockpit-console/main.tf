terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 4.59.0"
    }
    null = {
      source  = "hashicorp/null"
      version = ">= 3.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# Provider for ACM certificates (must be us-east-1 for CloudFront)
provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"
}

# ACM Certificate for custom domains (must be in us-east-1 for CloudFront)
resource "aws_acm_certificate" "console_cert" {
  count             = var.aliases != null && length(var.aliases) > 0 && var.create_certificate ? 1 : 0
  provider          = aws.us_east_1
  domain_name       = var.aliases[0]
  subject_alternative_names = length(var.aliases) > 1 ? slice(var.aliases, 1, length(var.aliases)) : []
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }

  tags = merge(var.tags, {
    Name = "kockpit-console-${var.kockpit_env}-cert"
  })
}

# Certificate validation (waits for DNS validation to complete)
resource "aws_acm_certificate_validation" "console_cert_validation" {
  count           = var.aliases != null && length(var.aliases) > 0 && var.create_certificate ? 1 : 0
  provider        = aws.us_east_1
  certificate_arn = aws_acm_certificate.console_cert[0].arn

  timeouts {
    create = "10m"
  }
}

# S3 bucket for hosting the web application
resource "aws_s3_bucket" "console_bucket" {
  bucket = "${var.bucket_name_prefix}-${var.kockpit_env}"

  tags = merge(var.tags, {
    Name        = "${var.bucket_name_prefix}-${var.kockpit_env}"
    Purpose     = "Kockpit Console UI"
    Environment = var.kockpit_env
  })
}

# S3 bucket versioning
resource "aws_s3_bucket_versioning" "console_bucket_versioning" {
  bucket = aws_s3_bucket.console_bucket.id
  versioning_configuration {
    status = "Enabled"
  }
}

# S3 bucket public access block
resource "aws_s3_bucket_public_access_block" "console_bucket_pab" {
  bucket = aws_s3_bucket.console_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# S3 bucket policy for CloudFront access
data "aws_iam_policy_document" "console_bucket_policy" {
  statement {
    sid    = "AllowCloudFrontServicePrincipal"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["cloudfront.amazonaws.com"]
    }
    actions = [
      "s3:GetObject"
    ]
    resources = [
      "${aws_s3_bucket.console_bucket.arn}/*"
    ]
    condition {
      test     = "StringEquals"
      variable = "AWS:SourceArn"
      values   = [aws_cloudfront_distribution.console_distribution.arn]
    }
  }
}

resource "aws_s3_bucket_policy" "console_bucket_policy" {
  bucket = aws_s3_bucket.console_bucket.id
  policy = data.aws_iam_policy_document.console_bucket_policy.json
}

# CloudFront Origin Access Control
resource "aws_cloudfront_origin_access_control" "console_oac" {
  name                              = "${var.service_name}-${var.kockpit_env}-oac"
  description                       = "OAC for ${var.service_name} ${var.kockpit_env}"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# Locals for conditional configuration
locals {
  distribution_origins = concat(
    [
      {
        domain_name              = aws_s3_bucket.console_bucket.bucket_regional_domain_name
        origin_access_control_id = aws_cloudfront_origin_access_control.console_oac.id
        origin_id                = "S3-${aws_s3_bucket.console_bucket.bucket}"
        is_s3                    = true
      }
    ],
    var.backend_alb_domain != null ? [
      {
        domain_name = var.backend_alb_domain
        origin_id   = "ALB-Backend"
        is_s3       = false
      }
    ] : []
  )

  cache_behaviors = var.backend_alb_domain != null ? [
    {
      path_pattern     = "/api/*"
      target_origin_id = "ALB-Backend"
    }
  ] : []
}

# CloudFront Origin Request Policy for API proxy
resource "aws_cloudfront_origin_request_policy" "api_proxy_policy" {
  count = var.backend_alb_domain != null && var.backend_alb_domain != "" ? 1 : 0
  name  = "${var.service_name}-${var.kockpit_env}-api-proxy-policy"

  cookies_config {
    cookie_behavior = "none"
  }

  headers_config {
    header_behavior = "whitelist"
    headers {
      items = ["Content-Type", "Accept", "Host"]
    }
  }

  query_strings_config {
    query_string_behavior = "all"
  }
}

# CloudFront distribution
resource "aws_cloudfront_distribution" "console_distribution" {
  # S3 origin (always present)
  origin {
    domain_name              = aws_s3_bucket.console_bucket.bucket_regional_domain_name
    origin_access_control_id = aws_cloudfront_origin_access_control.console_oac.id
    origin_id                = "S3-${aws_s3_bucket.console_bucket.bucket}"
  }

  # ALB origin (conditional)
  dynamic "origin" {
    for_each = var.backend_alb_domain != null && var.backend_alb_domain != "" ? [1] : []
    content {
      domain_name = var.backend_alb_domain
      origin_id   = "ALB-Backend"

      custom_origin_config {
        http_port              = 80
        https_port             = 443
        origin_protocol_policy = "https-only"
        origin_ssl_protocols   = ["TLSv1.2"]
      }
    }
  }

  enabled             = true
  is_ipv6_enabled     = true
  comment             = "CloudFront distribution for ${var.service_name} ${var.kockpit_env}"
  default_root_object = var.default_root_object
  aliases             = var.aliases

  # Default cache behavior (S3 static files)
  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "S3-${aws_s3_bucket.console_bucket.bucket}"

    forwarded_values {
      query_string = false
      cookies {
        forward = var.enable_cognito_auth ? "all" : "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400
    compress               = true

    # Lambda@Edge for Cognito authentication
    dynamic "lambda_function_association" {
      for_each = var.enable_cognito_auth ? [1] : []
      content {
        event_type   = "viewer-request"
        lambda_arn   = aws_lambda_function.cognito_auth[0].qualified_arn
        include_body = false
      }
    }
  }

  # API proxy cache behavior (conditional)
  dynamic "ordered_cache_behavior" {
    for_each = var.backend_alb_domain != null && var.backend_alb_domain != "" ? [1] : []
    content {
      path_pattern     = "/api/*"
      allowed_methods  = ["DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT"]
      cached_methods   = ["GET", "HEAD", "OPTIONS"]
      target_origin_id = "ALB-Backend"
      compress         = true

      origin_request_policy_id = aws_cloudfront_origin_request_policy.api_proxy_policy[0].id
      cache_policy_id          = "4135ea2d-6df8-44a3-9df3-4b5a84be39ad" # CachingDisabled policy

      viewer_protocol_policy = "redirect-to-https"
    }
  }

  # Custom error pages for SPA routing
  custom_error_response {
    error_code            = 403
    response_code         = 200
    response_page_path    = "/index.html"
    error_caching_min_ttl = 0
  }

  custom_error_response {
    error_code            = 404
    response_code         = 200
    response_page_path    = "/index.html"
    error_caching_min_ttl = 0
  }

  price_class = var.price_class

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = var.aliases == null || length(var.aliases) == 0 ? true : false
    acm_certificate_arn            = var.aliases != null && length(var.aliases) > 0 ? (
      var.acm_certificate_arn != null ? var.acm_certificate_arn : aws_acm_certificate_validation.console_cert_validation[0].certificate_arn
    ) : null
    ssl_support_method             = var.aliases != null && length(var.aliases) > 0 ? "sni-only" : null
    minimum_protocol_version       = var.aliases != null && length(var.aliases) > 0 ? "TLSv1.2_2021" : null
  }

  tags = var.tags
}

# Download and extract the console UI
resource "null_resource" "download_and_extract_ui" {
  count = var.auto_deploy ? 1 : 0
  triggers = {
    always = timestamp()
  }

  provisioner "local-exec" {
    command = <<-EOT
        set -e
        mkdir -p ${path.module}/temp
        curl -L -o ${path.module}/temp/console-ui-dist.zip ${var.console_ui_download_url}
        cd ${path.module}/temp && unzip -o console-ui-dist.zip && rm console-ui-dist.zip
        aws s3 sync ${path.module}/temp/ s3://${aws_s3_bucket.console_bucket.id}/ --delete
        aws cloudfront create-invalidation --distribution-id ${aws_cloudfront_distribution.console_distribution.id} --paths "/*"
        rm -rf ${path.module}/temp
      EOT
  }

  depends_on = [aws_cloudfront_distribution.console_distribution]
  /*
  provisioner "local-exec" {
    command = <<-EOT
      mkdir -p ${path.module}/temp
      curl -L -o ${path.module}/temp/console-ui-dist.zip ${var.console_ui_download_url}
      cd ${path.module}/temp
      unzip -o console-ui-dist.zip
    EOT
   */

  /*
  provisioner "local-exec" {
    when    = destroy
    command = "rm -rf ${path.module}/temp"
  }
   */
}

# Route 53 DNS records for custom domains
resource "aws_route53_record" "console_domain" {
  count   = var.create_route53_records && var.route53_zone_id != null && var.aliases != null && length(var.aliases) > 0 ? length(var.aliases) : 0
  zone_id = var.route53_zone_id
  name    = var.aliases[count.index]
  type    = "A"

  alias {
    name                   = aws_cloudfront_distribution.console_distribution.domain_name
    zone_id                = aws_cloudfront_distribution.console_distribution.hosted_zone_id
    evaluate_target_health = false
  }

  depends_on = [aws_cloudfront_distribution.console_distribution]
}
