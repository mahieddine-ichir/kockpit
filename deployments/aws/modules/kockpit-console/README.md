# Kockpit Console Module

This Terraform module deploys the Kockpit Console web application using Amazon S3 for static hosting and CloudFront for global content delivery.

## Features

- **Static Web Hosting**: S3 bucket with proper configuration for single-page applications
- **Global CDN**: CloudFront distribution for fast content delivery worldwide
- **Custom Domains**: Support for custom domain names with SSL certificates
- **API Proxy**: Optional integration with backend ALB for seamless API communication
- **Auto Deployment**: Automatic download and deployment of console UI from GitHub releases
- **SPA Support**: Proper error handling for client-side routing
- **Security**: Origin Access Control (OAC) for secure S3 access

## Usage

### Basic Usage

```hcl
module "kockpit_console" {
  source = "git::https://github.com/mahieddine-ichir/kockpit.git//deployments/aws/modules/kockpit-console?ref=main"

  # Basic configuration
  service_name = "kockpit-console"
  aws_region   = "eu-west-1"

  # Tags
  tags = {
    Environment = "production"
    Project     = "kockpit"
    Team        = "platform"
  }
}
```

### With Custom Domain

```hcl
module "kockpit_console" {
  source = "git::https://github.com/mahieddine-ichir/kockpit.git//deployments/aws/modules/kockpit-console?ref=main"

  service_name        = "kockpit-console"
  bucket_name_prefix  = "my-kockpit-console"

  # Custom domain configuration
  aliases             = ["console.mydomain.com"]
  acm_certificate_arn = "arn:aws:acm:us-east-1:123456789012:certificate/12345678-1234-1234-1234-123456789012"

  tags = {
    Environment = "production"
    Project     = "kockpit"
  }
}
```

### With Backend Integration

```hcl
module "kockpit_console" {
  source = "git::https://github.com/mahieddine-ichir/kockpit.git//deployments/aws/modules/kockpit-console?ref=main"

  service_name = "kockpit-console"

  # Backend ALB integration for API proxy
  backend_alb_domain = "api.mydomain.com"

  # Custom domain for frontend
  aliases             = ["console.mydomain.com"]
  acm_certificate_arn = "arn:aws:acm:us-east-1:123456789012:certificate/12345678-1234-1234-1234-123456789012"

  tags = {
    Environment = "production"
    Project     = "kockpit"
  }
}
```

### Manual Deployment Mode

```hcl
module "kockpit_console" {
  source = "git::https://github.com/mahieddine-ichir/kockpit.git//deployments/aws/modules/kockpit-console?ref=main"

  service_name = "kockpit-console"
  auto_deploy  = false  # Skip automatic download and deployment

  tags = {
    Environment = "production"
    Project     = "kockpit"
  }
}
```

## Requirements

| Name | Version |
|------|---------|
| terraform | >= 1.0 |
| aws | ~> 5.0 |
| archive | ~> 2.0 |
| null | ~> 3.0 |

## Providers

| Name | Version |
|------|---------|
| aws | ~> 5.0 |
| archive | ~> 2.0 |
| null | ~> 3.0 |
| random | n/a |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| aws_region | AWS region where resources will be created | `string` | `"eu-west-1"` | no |
| service_name | Name of the service (used for resource naming) | `string` | `"kockpit-console"` | no |
| bucket_name_prefix | Prefix for the S3 bucket name (will be suffixed with random string) | `string` | `"kockpit-console"` | no |
| default_root_object | Default root object for CloudFront distribution | `string` | `"index.html"` | no |
| price_class | CloudFront price class | `string` | `"PriceClass_100"` | no |
| aliases | List of CNAMEs (alternate domain names) for the distribution | `list(string)` | `null` | no |
| acm_certificate_arn | ARN of the ACM certificate for custom domains | `string` | `null` | no |
| backend_alb_domain | Domain name of the backend ALB for API proxy | `string` | `null` | no |
| console_ui_version | Version tag for cache busting when updating the UI | `string` | `"latest"` | no |
| console_ui_download_url | URL to download the console UI distribution zip | `string` | `"https://github.com/mahieddine-ichir/kockpit/releases/download/console-ui-dev-latest/console-ui-dist.zip"` | no |
| auto_deploy | Whether to automatically download and deploy the console UI | `bool` | `true` | no |
| tags | Tags to apply to all resources | `map(string)` | `{"Environment": "production", "Project": "kockpit", "ManagedBy": "terraform"}` | no |

## Outputs

| Name | Description |
|------|-------------|
| s3_bucket_id | ID of the S3 bucket hosting the console UI |
| s3_bucket_arn | ARN of the S3 bucket hosting the console UI |
| cloudfront_distribution_id | ID of the CloudFront distribution |
| cloudfront_domain_name | Domain name of the CloudFront distribution |
| console_url | URL to access the Kockpit console |
| deployment_status | Status of the console deployment |

## Architecture

```
Internet → CloudFront → S3 Bucket (Static Files)
              ↓
         ALB Backend (API Proxy)
```

### Key Features:

1. **S3 Static Hosting**:
   - Versioned bucket for rollback capability
   - Public access blocked (access only via CloudFront)
   - Proper MIME type detection for web assets

2. **CloudFront Distribution**:
   - Global edge locations for fast delivery
   - Custom SSL certificates support
   - SPA-friendly error handling (404 → index.html)
   - Separate cache behaviors for static assets vs API calls

3. **API Integration**:
   - Optional backend ALB integration
   - Proxies `/backend/*` requests to your API
   - Proper header forwarding for authentication

4. **Security**:
   - Origin Access Control (OAC) for S3 access
   - HTTPS-only access
   - Geo-restriction support (disabled by default)

## Manual Deployment

If you set `auto_deploy = false`, you can manually upload files to the S3 bucket:

```bash
# Download and extract the console UI
curl -L -o console-ui-dist.zip https://github.com/mahieddine-ichir/kockpit/releases/download/console-ui-dev-latest/console-ui-dist.zip
unzip console-ui-dist.zip

# Upload to S3
aws s3 sync dist/ s3://your-bucket-name/ --delete

# Invalidate CloudFront cache
aws cloudfront create-invalidation --distribution-id YOUR_DISTRIBUTION_ID --paths "/*"
```

## Notes

- **Certificate Requirements**: For custom domains, ACM certificates must be in the `us-east-1` region for CloudFront
- **DNS Configuration**: You'll need to create CNAME/ALIAS records pointing your custom domain to the CloudFront distribution
- **Backend Integration**: When using `backend_alb_domain`, ensure your ALB accepts traffic from CloudFront edge locations
- **Cost Optimization**: Use `PriceClass_100` for cost-effective delivery to US and Europe only