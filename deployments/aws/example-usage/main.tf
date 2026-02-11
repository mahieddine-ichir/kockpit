terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# Deploy Kockpit Console Backend using the module
module "kockpit_console_backend" {
  # Use Git source - replace with your actual repository
  source = "git::https://github.com/your-org/kockpit.git//deployments/aws/modules/kockpit-console-backend?ref=main"

  # AWS Configuration
  aws_region = var.aws_region

  # Required infrastructure inputs (existing resources)
  vpc_id                = var.vpc_id
  private_subnet_ids    = var.private_subnet_ids
  ecs_cluster_name      = var.ecs_cluster_name
  load_balancer_arn     = var.load_balancer_arn
  lb_security_group_id  = var.lb_security_group_id
  s3_bucket_name        = var.s3_bucket_name  # For IAM permissions

  # Kockpit-specific configuration
  kockpit_env                   = var.environment
  opensearch_endpoints          = var.opensearch_endpoints
  kockpit_data_s3_bucket       = var.kockpit_data_s3_bucket
  kockpit_manifests_s3_bucket  = var.kockpit_manifests_s3_bucket
  kinesis_stream_name          = var.kinesis_stream_name

  # Container configuration
  container_image = var.container_image
  cpu            = var.cpu
  memory         = var.memory
  desired_count  = var.desired_count

  # Optional additional environment variables
  additional_environment_variables = var.additional_env_vars

  tags = var.tags
}

# Optional: Create ALB listener rule to route traffic
resource "aws_lb_listener_rule" "kockpit_api" {
  count        = var.create_listener_rule ? 1 : 0
  listener_arn = var.load_balancer_listener_arn
  priority     = var.listener_rule_priority

  action {
    type             = "forward"
    target_group_arn = module.kockpit_console_backend.target_group_arn
  }

  condition {
    path_pattern {
      values = ["/backend/*"]  # Changed to /backend/* for CloudFront integration
    }
  }

  tags = var.tags
}

# Deploy Kockpit Console Frontend with CloudFront API integration
module "kockpit_console" {
  # Use Git source - replace with your actual repository
  source = "git::https://github.com/your-org/kockpit.git//deployments/aws/modules/kockpit-console?ref=main"

  # Basic configuration
  service_name = "kockpit-console"
  kockpit_env  = var.environment
  aws_region   = var.aws_region

  # CloudFront + API Integration: Connect frontend to backend ALB
  backend_alb_domain = replace(module.kockpit_console_backend.service_url, "https://", "")

  # Custom domain configuration (optional)
  aliases             = var.console_domain_aliases
  create_certificate  = var.create_ssl_certificate
  acm_certificate_arn = var.existing_certificate_arn

  # Route 53 DNS automation (optional)
  route53_zone_id        = var.route53_zone_id
  create_route53_records = var.create_route53_records

  # UI Configuration
  auto_deploy             = var.auto_deploy_ui
  console_ui_version     = var.console_ui_version
  console_ui_download_url = var.console_ui_download_url

  tags = var.tags

  depends_on = [module.kockpit_console_backend]
}