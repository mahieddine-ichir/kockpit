# Optional Cognito User Pool (can be created separately)
resource "aws_cognito_user_pool" "console_pool" {
  count = var.enable_cognito_auth && var.cognito_user_pool_id == null ? 1 : 0
  name  = "${var.service_name}-${var.kockpit_env}-users"

  # Password policy
  password_policy {
    minimum_length    = 8
    require_lowercase = true
    require_uppercase = true
    require_numbers   = true
    require_symbols   = true
  }

  # User attributes
  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]

  # Account recovery
  account_recovery_setting {
    recovery_mechanism {
      name     = "verified_email"
      priority = 1
    }
  }

  tags = var.tags
}

# Cognito User Pool Client
resource "aws_cognito_user_pool_client" "console_client" {
  count                = var.enable_cognito_auth && var.cognito_client_id == null ? 1 : 0
  name                 = "${var.service_name}-${var.kockpit_env}-client"
  user_pool_id         = var.cognito_user_pool_id != null ? var.cognito_user_pool_id : aws_cognito_user_pool.console_pool[0].id
  generate_secret      = false  # Public client for SPA

  # OAuth configuration
  allowed_oauth_flows                  = ["code"]
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_scopes                = ["openid", "profile", "email"]

  # Callback URLs
  callback_urls = var.aliases != null && length(var.aliases) > 0 ? [
    for alias in var.aliases : "https://${alias}/auth/callback"
  ] : ["https://${aws_cloudfront_distribution.console_distribution.domain_name}/auth/callback"]

  logout_urls = var.aliases != null && length(var.aliases) > 0 ? [
    for alias in var.aliases : "https://${alias}/"
  ] : ["https://${aws_cloudfront_distribution.console_distribution.domain_name}/"]

  # Token validity
  access_token_validity  = 1  # 1 hour
  id_token_validity     = 1  # 1 hour
  refresh_token_validity = 30 # 30 days

  token_validity_units {
    access_token  = "hours"
    id_token      = "hours"
    refresh_token = "days"
  }
}

# Cognito User Pool Domain
resource "aws_cognito_user_pool_domain" "console_domain" {
  count       = var.enable_cognito_auth && var.cognito_user_pool_domain == null ? 1 : 0
  domain      = "${var.service_name}-${var.kockpit_env}-auth"
  user_pool_id = var.cognito_user_pool_id != null ? var.cognito_user_pool_id : aws_cognito_user_pool.console_pool[0].id
}

# Locals for Cognito configuration
locals {
  cognito_user_pool_id     = var.cognito_user_pool_id != null ? var.cognito_user_pool_id : (var.enable_cognito_auth ? aws_cognito_user_pool.console_pool[0].id : null)
  cognito_client_id        = var.cognito_client_id != null ? var.cognito_client_id : (var.enable_cognito_auth ? aws_cognito_user_pool_client.console_client[0].id : null)
  cognito_domain          = var.cognito_user_pool_domain != null ? var.cognito_user_pool_domain : (var.enable_cognito_auth ? aws_cognito_user_pool_domain.console_domain[0].domain : null)
}