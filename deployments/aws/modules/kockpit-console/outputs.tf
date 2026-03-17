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
  value = {
    s3_sync_deployed       = var.auto_deploy ? "completed" : "skipped"
    cloudfront_invalidated = var.auto_deploy ? "completed" : "skipped"
    console_version        = var.console_ui_version
  }
}

# Certificate Information
output "certificate_arn" {
  description = "ARN of the SSL certificate (created or provided)"
  value = var.aliases != null && length(var.aliases) > 0 ? (
    var.acm_certificate_arn != null ? var.acm_certificate_arn : (
      var.create_certificate ? aws_acm_certificate_validation.console_cert_validation[0].certificate_arn : null
    )
  ) : null
}

output "certificate_validation_options" {
  description = "Certificate validation options for DNS validation"
  value = var.create_certificate && var.aliases != null && length(var.aliases) > 0 ? aws_acm_certificate.console_cert[0].domain_validation_options : null
}

# Route 53 Information
output "route53_records" {
  description = "Route 53 DNS records created"
  value = var.create_route53_records && var.route53_zone_id != null ? [
    for record in aws_route53_record.console_domain : {
      name = record.name
      type = record.type
      fqdn = record.fqdn
    }
  ] : []
}

# Cognito Information
output "cognito_user_pool_id" {
  description = "Cognito User Pool ID"
  value       = var.enable_cognito_auth ? local.cognito_user_pool_id : null
}

output "cognito_client_id" {
  description = "Cognito App Client ID"
  value       = var.enable_cognito_auth ? local.cognito_client_id : null
}

output "cognito_domain" {
  description = "Cognito authentication domain"
  value       = var.enable_cognito_auth ? local.cognito_domain : null
}

output "cognito_login_url" {
  description = "Cognito login URL"
  value = var.enable_cognito_auth ? "https://${local.cognito_domain}.auth.${var.aws_region}.amazoncognito.com/login" : null
}

# Debug Information
output "debug_certificate_config" {
  description = "Debug information for certificate configuration"
  value = {
    aliases                        = var.aliases
    aliases_length                = var.aliases != null ? length(var.aliases) : 0
    acm_certificate_arn           = var.acm_certificate_arn
    create_certificate            = var.create_certificate
    using_default_certificate     = var.aliases == null || length(var.aliases) == 0
    certificate_arn_will_be_used  = var.aliases != null && length(var.aliases) > 0 ? (
      var.acm_certificate_arn != null ? var.acm_certificate_arn : (
        var.create_certificate ? "will_be_created_and_validated" : null
      )
    ) : null
  }
}