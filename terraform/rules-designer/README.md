# Kockpit Rules Designer Infrastructure

This Terraform configuration provisions AWS infrastructure for the Kockpit Rules Designer:
- S3 bucket for static website hosting
- CloudFront distribution for CDN and HTTPS
- Origin Access Control for secure S3 access

## Prerequisites

1. AWS CLI configured with appropriate credentials
2. Terraform installed (>= 1.0)
3. S3 bucket for Terraform state (recommended)

## Setup

### 1. Configure Terraform Backend (Optional but Recommended)

Create a backend configuration file:

```bash
# backend-config-dev.hcl
bucket = "your-terraform-state-bucket"
key    = "kockpit/rules-designer/dev/terraform.tfstate"
region = "us-east-1"
```

### 2. Initialize Terraform

```bash
# Without backend
terraform init

# With backend
terraform init -backend-config=backend-config-dev.hcl
```

### 3. Plan and Apply

```bash
# Dev environment
terraform plan -var-file=dev.tfvars
terraform apply -var-file=dev.tfvars

# Staging environment
terraform plan -var-file=staging.tfvars
terraform apply -var-file=staging.tfvars

# Production environment
terraform plan -var-file=production.tfvars
terraform apply -var-file=production.tfvars
```

## Outputs

After applying, Terraform will output:

- `s3_bucket_name` - S3 bucket name for deployment
- `cloudfront_distribution_id` - CloudFront distribution ID
- `cloudfront_url` - Full URL to access the application

Save these outputs to GitHub Secrets for the deployment workflow:

```bash
# Get outputs
terraform output s3_bucket_name
terraform output cloudfront_distribution_id

# Add to GitHub Secrets:
# - S3_BUCKET (or S3_BUCKET_DEV/STAGING/PRODUCTION)
# - CLOUDFRONT_DISTRIBUTION_ID (or CLOUDFRONT_DISTRIBUTION_ID_DEV/STAGING/PRODUCTION)
```

## Custom Domain (Optional)

To use a custom domain:

1. Create an ACM certificate in `us-east-1` region
2. Update the tfvars file:
   ```hcl
   custom_domain = "rules-designer.yourdomain.com"
   acm_certificate_arn = "arn:aws:acm:us-east-1:ACCOUNT_ID:certificate/CERT_ID"
   ```
3. Apply the changes
4. Update your DNS to point to the CloudFront distribution

## Destroy Infrastructure

```bash
terraform destroy -var-file=dev.tfvars
```

## Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `aws_region` | AWS region | `us-east-1` |
| `environment` | Environment (dev/staging/production) | - |
| `project_name` | Project name prefix | `kockpit` |
| `cloudfront_price_class` | CloudFront price class | `PriceClass_100` |
| `custom_domain` | Custom domain (optional) | `""` |
| `acm_certificate_arn` | ACM certificate ARN (optional) | `""` |