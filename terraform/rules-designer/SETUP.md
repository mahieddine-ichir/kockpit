# Complete Setup Guide for Rules Designer Infrastructure

This guide walks you through setting up the complete infrastructure for the Kockpit Rules Designer.

## Quick Start

### 1. Prerequisites

- AWS Account with appropriate permissions
- AWS CLI configured
- Terraform installed (>= 1.0)
- GitHub repository access

### 2. Deploy Infrastructure with Terraform

```bash
cd terraform/rules-designer

# Initialize Terraform
terraform init

# Plan infrastructure for dev environment
terraform plan -var-file=dev.tfvars

# Apply infrastructure
terraform apply -var-file=dev.tfvars

# Save the outputs
terraform output s3_bucket_name
terraform output cloudfront_distribution_id
```

### 3. Configure GitHub Secrets

Add the following secrets to your GitHub repository:

**AWS Credentials:**
- `AWS_ACCESS_KEY_ID` - Your AWS access key
- `AWS_SECRET_ACCESS_KEY` - Your AWS secret key
- `AWS_REGION` - AWS region (e.g., `us-east-1`)

**Terraform Outputs (from step 2):**
- `S3_BUCKET` - The S3 bucket name
- `CLOUDFRONT_DISTRIBUTION_ID` - The CloudFront distribution ID

For multiple environments, use environment-specific secrets:
- `S3_BUCKET_DEV`, `S3_BUCKET_STAGING`, `S3_BUCKET_PRODUCTION`
- `CLOUDFRONT_DISTRIBUTION_ID_DEV`, `CLOUDFRONT_DISTRIBUTION_ID_STAGING`, `CLOUDFRONT_DISTRIBUTION_ID_PRODUCTION`

### 4. Deploy Application

Use the GitHub Actions workflow to deploy:

**Manual Deployment:**
1. Go to GitHub Actions
2. Select "Deploy Rules Designer to S3"
3. Click "Run workflow"
4. Select environment (dev/staging/production)
5. Click "Run workflow"

**Automatic Deployment:**
- Push changes to `dev` branch → deploys to dev environment
- Changes to `kockpit-rules/kockpit-rules-designer/**` trigger automatic deployment

## Using GitHub Workflows

### Terraform Workflow

The `rules-designer-terraform.yml` workflow provisions infrastructure:

**Manual Trigger:**
```
Actions → Terraform - Rules Designer Infrastructure → Run workflow
  - Environment: dev/staging/production
  - Action: plan/apply/destroy
```

**Automatic Trigger:**
- Runs `terraform plan` on pull requests
- Runs `terraform plan` when terraform files are modified

### Deployment Workflow

The `rules-designer-s3.yml` workflow deploys the application:

**Manual Trigger:**
```
Actions → Deploy Rules Designer to S3 → Run workflow
  - Environment: dev/staging/production
```

**Automatic Trigger:**
- Pushes to `dev` branch
- Changes to `kockpit-rules/kockpit-rules-designer/**`

## Infrastructure Components

### S3 Bucket
- **Purpose:** Hosts the static website files
- **Versioning:** Enabled for file history
- **Access:** Private (accessed only through CloudFront)

### CloudFront Distribution
- **Purpose:** CDN for fast content delivery and HTTPS
- **Caching:** Optimized for static assets
- **Error Handling:** Routes 404/403 to index.html for SPA routing
- **Security:** HTTPS required, Origin Access Control for S3

### Origin Access Control (OAC)
- **Purpose:** Secure access from CloudFront to S3
- **Method:** AWS Signature Version 4 (SigV4)

## Environment Strategy

### Development (dev)
- **Purpose:** Testing and development
- **CloudFront:** PriceClass_100 (North America & Europe)
- **Bucket:** `kockpit-rules-designer-dev`
- **Access:** Internal team only

### Staging (staging)
- **Purpose:** Pre-production testing
- **CloudFront:** PriceClass_100
- **Bucket:** `kockpit-rules-designer-staging`
- **Access:** QA team and stakeholders

### Production (production)
- **Purpose:** Live environment
- **CloudFront:** PriceClass_All (Global)
- **Bucket:** `kockpit-rules-designer-production`
- **Access:** Public (if applicable)

## Advanced Configuration

### Custom Domain

To use a custom domain (e.g., `rules-designer.yourdomain.com`):

1. **Create ACM Certificate** (must be in us-east-1):
   ```bash
   aws acm request-certificate \
     --domain-name rules-designer.yourdomain.com \
     --validation-method DNS \
     --region us-east-1
   ```

2. **Update tfvars**:
   ```hcl
   custom_domain = "rules-designer.yourdomain.com"
   acm_certificate_arn = "arn:aws:acm:us-east-1:ACCOUNT_ID:certificate/CERT_ID"
   ```

3. **Apply Terraform**:
   ```bash
   terraform apply -var-file=production.tfvars
   ```

4. **Update DNS**:
   - Create CNAME record pointing to CloudFront domain

### Terraform State Backend

For team collaboration, use S3 backend:

1. **Create state bucket**:
   ```bash
   aws s3 mb s3://your-terraform-state-bucket
   aws s3api put-bucket-versioning \
     --bucket your-terraform-state-bucket \
     --versioning-configuration Status=Enabled
   ```

2. **Create backend config**:
   ```hcl
   # backend-config-dev.hcl
   bucket = "your-terraform-state-bucket"
   key    = "kockpit/rules-designer/dev/terraform.tfstate"
   region = "us-east-1"
   ```

3. **Initialize with backend**:
   ```bash
   terraform init -backend-config=backend-config-dev.hcl
   ```

## Troubleshooting

### Issue: Terraform state locked
```bash
# Force unlock (use with caution)
terraform force-unlock LOCK_ID
```

### Issue: CloudFront distribution takes time to deploy
- CloudFront distributions can take 15-20 minutes to fully deploy
- Check status: `aws cloudfront get-distribution --id DISTRIBUTION_ID`

### Issue: 403 errors after deployment
- Wait for CloudFront cache invalidation to complete
- Check S3 bucket policy allows CloudFront access

### Issue: HTTPS certificate errors
- ACM certificate must be in us-east-1 region
- Certificate must be validated before use
- DNS validation is recommended over email

## Cost Optimization

- **S3:** Minimal cost for storage (usually < $1/month)
- **CloudFront:** Free tier: 1TB data transfer, 10M requests/month
- **Data Transfer:** Main cost driver, optimize by:
  - Using PriceClass_100 for dev/staging
  - Enabling compression
  - Setting appropriate cache headers

## Monitoring

### CloudWatch Metrics
```bash
# CloudFront metrics
aws cloudwatch get-metric-statistics \
  --namespace AWS/CloudFront \
  --metric-name Requests \
  --dimensions Name=DistributionId,Value=DISTRIBUTION_ID \
  --start-time 2024-01-01T00:00:00Z \
  --end-time 2024-01-02T00:00:00Z \
  --period 3600 \
  --statistics Sum
```

### S3 Access Logs (Optional)
Enable S3 access logging to track bucket access patterns.

## Security Best Practices

1. **Use OIDC** instead of access keys for GitHub Actions
2. **Enable S3 versioning** for rollback capability
3. **Use separate AWS accounts** for dev/staging/production
4. **Implement least privilege** IAM policies
5. **Enable CloudTrail** for audit logging
6. **Regular security reviews** of IAM policies and bucket policies