# Example usage of the ECS service module

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

# Example: Deploy kockpit-console-backend using the module
module "kockpit_console_backend" {
  source = "./modules/ecs-service"

  # Required infrastructure inputs
  vpc_id                = var.vpc_id
  private_subnet_names  = var.private_subnet_names
  ecs_cluster_name      = var.ecs_cluster_name
  load_balancer_arn     = var.load_balancer_arn
  s3_bucket_name        = var.s3_bucket_name

  # Service configuration
  service_name      = "kockpit-console-backend"
  container_image   = var.container_image
  container_port    = 8080
  cpu              = var.cpu
  memory           = var.memory
  desired_count    = var.desired_count
  health_check_path = "/actuator/health"

  # Kockpit-specific configuration
  kockpit_env                    = var.kockpit_env
  opensearch_endpoints           = var.opensearch_endpoints
  kockpit_data_s3_bucket        = var.kockpit_data_s3_bucket
  kockpit_manifests_s3_bucket   = var.kockpit_manifests_s3_bucket
  kinesis_stream_name           = var.kinesis_stream_name

  # Additional environment variables if needed
  additional_environment_variables = var.additional_environment_variables

  tags = var.tags
}

# Optional: Create load balancer listener rule to route traffic to the service
resource "aws_lb_listener_rule" "kockpit_backend" {
  count        = var.create_listener_rule ? 1 : 0
  listener_arn = var.load_balancer_listener_arn
  priority     = var.listener_rule_priority

  action {
    type             = "forward"
    target_group_arn = module.kockpit_console_backend.target_group_arn
  }

  condition {
    path_pattern {
      values = ["/api/*", "/actuator/*"]
    }
  }

  tags = var.tags
}