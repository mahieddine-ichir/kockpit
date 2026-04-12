terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Backend configuration for state storage
  backend "s3" {
    # Configure these via backend config file or environment variables
    # bucket = "your-terraform-state-bucket"
    # key    = "kockpit/rules-designer/terraform.tfstate"
    # region = "us-east-1"
  }
}

provider "aws" {
  region = var.aws_region
}

# S3 bucket for hosting the Rules Designer
resource "aws_s3_bucket" "rules_designer" {
  bucket = "${var.project_name}-rules-designer-${var.environment}"

  tags = {
    Name        = "Kockpit Rules Designer - ${var.environment}"
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "Terraform"
  }
}

# S3 bucket versioning
resource "aws_s3_bucket_versioning" "rules_designer" {
  bucket = aws_s3_bucket.rules_designer.id

  versioning_configuration {
    status = "Enabled"
  }
}

# S3 bucket public access block
resource "aws_s3_bucket_public_access_block" "rules_designer" {
  bucket = aws_s3_bucket.rules_designer.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# S3 bucket policy for CloudFront access
resource "aws_s3_bucket_policy" "rules_designer" {
  bucket = aws_s3_bucket.rules_designer.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowCloudFrontServicePrincipal"
        Effect    = "Allow"
        Principal = {
          Service = "cloudfront.amazonaws.com"
        }
        Action   = "s3:GetObject"
        Resource = "${aws_s3_bucket.rules_designer.arn}/*"
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = aws_cloudfront_distribution.rules_designer.arn
          }
        }
      }
    ]
  })
}

# CloudFront Origin Access Control
resource "aws_cloudfront_origin_access_control" "rules_designer" {
  name                              = "${var.project_name}-rules-designer-${var.environment}-oac"
  description                       = "OAC for Kockpit Rules Designer ${var.environment}"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# CloudFront distribution
resource "aws_cloudfront_distribution" "rules_designer" {
  enabled             = true
  is_ipv6_enabled     = true
  comment             = "Kockpit Rules Designer - ${var.environment}"
  default_root_object = "index.html"
  price_class         = var.cloudfront_price_class

  origin {
    domain_name              = aws_s3_bucket.rules_designer.bucket_regional_domain_name
    origin_id                = "S3-${aws_s3_bucket.rules_designer.id}"
    origin_access_control_id = aws_cloudfront_origin_access_control.rules_designer.id
  }

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "S3-${aws_s3_bucket.rules_designer.id}"

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400
    compress               = true
  }

  # Custom error response for SPA routing
  custom_error_response {
    error_code         = 404
    response_code      = 200
    response_page_path = "/index.html"
  }

  custom_error_response {
    error_code         = 403
    response_code      = 200
    response_page_path = "/index.html"
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = var.custom_domain == "" ? true : false

    dynamic "viewer_certificate" {
      for_each = var.custom_domain != "" ? [1] : []
      content {
        acm_certificate_arn      = var.acm_certificate_arn
        ssl_support_method       = "sni-only"
        minimum_protocol_version = "TLSv1.2_2021"
      }
    }
  }

  dynamic "aliases" {
    for_each = var.custom_domain != "" ? [var.custom_domain] : []
    content {
      aliases = [aliases.value]
    }
  }

  tags = {
    Name        = "Kockpit Rules Designer - ${var.environment}"
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "Terraform"
  }
}