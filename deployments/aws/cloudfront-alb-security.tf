# CloudFront to ALB Security Restriction
# This file adds security group rules to restrict ALB access to CloudFront only

# Data source to get CloudFront managed prefix list
data "aws_ec2_managed_prefix_list" "cloudfront" {
  name = "com.amazonaws.global.cloudfront.origin-facing"
}

# Data source for existing ALB security group (you'll need to provide the ID)
variable "alb_security_group_id" {
  description = "Security group ID of the existing ALB that needs CloudFront restriction"
  type        = string
  # Example: sg-1234567890abcdef0
}

# Security group rule to allow HTTP from CloudFront only
resource "aws_security_group_rule" "alb_cloudfront_http" {
  type              = "ingress"
  from_port         = 80
  to_port           = 80
  protocol          = "tcp"
  prefix_list_ids   = [data.aws_ec2_managed_prefix_list.cloudfront.id]
  security_group_id = var.alb_security_group_id
  description       = "Allow HTTP from CloudFront origin-facing IPs only"
}

# Optional: Remove existing broad HTTP rule (uncomment if needed)
# You may need to identify and remove existing rules that allow 0.0.0.0/0:80

# resource "aws_security_group_rule" "remove_broad_http" {
#   type              = "ingress"
#   from_port         = 80
#   to_port           = 80
#   protocol          = "tcp"
#   cidr_blocks       = ["0.0.0.0/0"]
#   security_group_id = var.alb_security_group_id
#
#   # This will remove the rule - set to absent
#   lifecycle {
#     create_before_destroy = false
#   }
# }

output "cloudfront_prefix_list_id" {
  description = "CloudFront managed prefix list ID used for ALB restriction"
  value       = data.aws_ec2_managed_prefix_list.cloudfront.id
}

output "cloudfront_ip_ranges_count" {
  description = "Number of IP ranges in CloudFront prefix list"
  value       = length(data.aws_ec2_managed_prefix_list.cloudfront.entries)
}