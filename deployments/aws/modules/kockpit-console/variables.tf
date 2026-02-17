# General Configuration
variable "aws_region" {
  description = "AWS region where resources will be created"
  type        = string
  default     = "eu-west-1"
}

variable "service_name" {
  description = "Name of the service (used for resource naming)"
  type        = string
  default     = "kockpit-console"
}

variable "tags" {
  description = "Tags to apply to all resources"
  type        = map(string)
  default = {
    Environment = "production"
    Project     = "kockpit"
    ManagedBy   = "terraform"
  }
}

# Environment Configuration
variable "kockpit_env" {
  description = "Kockpit environment (dev, staging, prod)"
  type        = string
  default     = "dev"
}

# S3 Configuration
variable "bucket_name_prefix" {
  description = "Prefix for the S3 bucket name (environment will be appended automatically)"
  type        = string
  default     = "kockpit-console"
}

# CloudFront Configuration
variable "default_root_object" {
  description = "Default root object for CloudFront distribution"
  type        = string
  default     = "index.html"
}

variable "price_class" {
  description = "CloudFront price class"
  type        = string
  default     = "PriceClass_100"
  validation {
    condition     = contains(["PriceClass_All", "PriceClass_200", "PriceClass_100"], var.price_class)
    error_message = "Price class must be one of: PriceClass_All, PriceClass_200, PriceClass_100."
  }
}

variable "aliases" {
  description = "List of CNAMEs (alternate domain names) for the distribution"
  type        = list(string)
  default     = null
}

variable "acm_certificate_arn" {
  description = "ARN of the ACM certificate for custom domains (optional if create_certificate is true)"
  type        = string
  default     = null

  validation {
    condition = var.acm_certificate_arn == null || can(regex("^arn:aws:acm:us-east-1:", var.acm_certificate_arn))
    error_message = "ACM certificate must be in us-east-1 region for CloudFront. ARN should start with 'arn:aws:acm:us-east-1:'."
  }
}

variable "create_certificate" {
  description = "Whether to create an ACM certificate for custom domains"
  type        = bool
  default     = true
}

# Backend Integration
variable "backend_alb_domain" {
  description = "Domain name of the backend ALB for API proxy (optional)"
  type        = string
  default     = null

  validation {
    condition = var.backend_alb_domain == null || can(regex("^[a-zA-Z0-9][a-zA-Z0-9-._]*[a-zA-Z0-9]$", var.backend_alb_domain))
    error_message = "backend_alb_domain must be a valid domain name or null."
  }
}

# Console UI Configuration
variable "console_ui_version" {
  description = "Version tag for cache busting when updating the UI"
  type        = string
  default     = "latest"
}

variable "console_ui_download_url" {
  description = "URL to download the console UI distribution zip"
  type        = string
  default     = "https://github.com/mahieddine-ichir/kockpit/releases/download/console-ui-dev-latest/console-ui-aws-dist.zip"
}

variable "auto_deploy" {
  description = "Whether to automatically download and deploy the console UI"
  type        = bool
  default     = true
}

# Route 53 Configuration
variable "route53_zone_id" {
  description = "Route 53 hosted zone ID for creating DNS records (optional)"
  type        = string
  default     = null
}

variable "create_route53_records" {
  description = "Whether to create Route 53 DNS records for custom domains"
  type        = bool
  default     = false
}

# Cognito Authentication Configuration
variable "enable_cognito_auth" {
  description = "Enable AWS Cognito authentication for CloudFront"
  type        = bool
  default     = false
}

variable "cognito_user_pool_id" {
  description = "AWS Cognito User Pool ID"
  type        = string
  default     = null
}

variable "cognito_user_pool_domain" {
  description = "AWS Cognito User Pool domain (e.g., myapp.auth.us-east-1.amazoncognito.com)"
  type        = string
  default     = null
}

variable "cognito_client_id" {
  description = "AWS Cognito App Client ID"
  type        = string
  default     = null
}

// check if client_secret is necessary
variable "cognito_client_secret" {
  description = "AWS Cognito App Client Secret"
  type        = string
  default     = null
}

variable "cognito_auto_callback_update" {
  description = "Automatically update Cognito callback URLs with CloudFront domain"
  type        = bool
  default     = true
}
