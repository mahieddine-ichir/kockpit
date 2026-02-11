# S3 Bucket Information
output "s3_bucket_id" {
  description = "ID of the S3 bucket hosting the console UI"
  value       = aws_s3_bucket.console_bucket.id
}

output "s3_bucket_arn" {
  description = "ARN of the S3 bucket hosting the console UI"
  value       = aws_s3_bucket.console_bucket.arn
}

output "s3_bucket_domain_name" {
  description = "Domain name of the S3 bucket"
  value       = aws_s3_bucket.console_bucket.bucket_domain_name
}

output "s3_bucket_regional_domain_name" {
  description = "Regional domain name of the S3 bucket"
  value       = aws_s3_bucket.console_bucket.bucket_regional_domain_name
}

# CloudFront Information
output "cloudfront_distribution_id" {
  description = "ID of the CloudFront distribution"
  value       = aws_cloudfront_distribution.console_distribution.id
}

output "cloudfront_distribution_arn" {
  description = "ARN of the CloudFront distribution"
  value       = aws_cloudfront_distribution.console_distribution.arn
}

output "cloudfront_domain_name" {
  description = "Domain name of the CloudFront distribution"
  value       = aws_cloudfront_distribution.console_distribution.domain_name
}

output "cloudfront_hosted_zone_id" {
  description = "CloudFront Route 53 zone ID"
  value       = aws_cloudfront_distribution.console_distribution.hosted_zone_id
}

# Application URLs
output "console_url" {
  description = "URL to access the Kockpit console"
  value       = var.aliases != null ? "https://${var.aliases[0]}" : "https://${aws_cloudfront_distribution.console_distribution.domain_name}"
}

# Deployment Information
output "deployment_status" {
  description = "Status of the console deployment"
  value = var.auto_deploy ? {
    s3_files_deployed      = length(aws_s3_object.console_files)
    cloudfront_invalidated = var.auto_deploy ? "completed" : "skipped"
    console_version       = var.console_ui_version
  } : {
    s3_files_deployed      = 0
    cloudfront_invalidated = "skipped"
    console_version       = var.console_ui_version
  }
}

# Debug Information
output "debug_certificate_config" {
  description = "Debug information for certificate configuration"
  value = {
    aliases                        = var.aliases
    aliases_length                = var.aliases != null ? length(var.aliases) : 0
    acm_certificate_arn           = var.acm_certificate_arn
    using_default_certificate     = var.aliases == null || length(var.aliases) == 0
    certificate_arn_will_be_used  = var.aliases != null && length(var.aliases) > 0 ? var.acm_certificate_arn : null
  }
}