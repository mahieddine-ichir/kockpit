# CloudFront ALB Security Configuration
# Replace with your actual ALB security group ID

alb_security_group_id = "sg-xxxxxxxxxxxxxxxxx"

# To find your ALB security group ID:
# 1. AWS Console → EC2 → Load Balancers → Your ALB → Security tab
# 2. OR use CLI: aws elbv2 describe-load-balancers --names your-alb-name