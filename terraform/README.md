# Terraform Infrastructure Setup

This directory contains Terraform configuration for provisioning GCP infrastructure for the Expense Tracker application.

## Architecture Overview

- **Cloud Provider**: Google Cloud Platform (GCP)
- **Kubernetes**: Google Kubernetes Engine (GKE)
- **Database**: Cloud SQL (PostgreSQL 15)
- **Secrets**: Google Secret Manager
- **State Management**: Google Cloud Storage (GCS) with Terragrunt
- **Environment Strategy**: Single GCP project with namespace isolation

## Infrastructure Components

### Networking
- VPC with custom subnets
- Private IP ranges for pods and services
- Cloud NAT for egress traffic

### GKE Cluster
- Regional cluster for high availability
- Workload Identity enabled
- Auto-scaling node pools
- Network policies enabled
- Monitoring and logging with Cloud Operations

### Database
- Cloud SQL PostgreSQL 15
- Private IP only (no public access)
- Automated backups
- Point-in-time recovery (production only)
- Different tiers for staging/production

### Security
- Workload Identity for pod authentication
- Secret Manager for sensitive data
- IAM roles with least privilege
- Private GKE cluster option

## Directory Structure

```
terraform/
├── terragrunt.hcl              # Root Terragrunt config (DRY)
├── backend.tf                  # Terraform backend config
├── main.tf                     # Main infrastructure resources
├── variables.tf                # Input variables
├── outputs.tf                  # Output values
└── environments/
    ├── staging/
    │   ├── terragrunt.hcl      # Staging-specific config
    │   └── terraform.tfvars    # Staging variables (gitignored)
    └── production/
        ├── terragrunt.hcl      # Production-specific config
        └── terraform.tfvars    # Production variables (gitignored)
```

## Prerequisites

### 1. Install Tools

```bash
# Install Terraform
brew install terraform

# Install Terragrunt
brew install terragrunt

# Install gcloud CLI
brew install --cask google-cloud-sdk
```

### 2. GCP Setup

```bash
# Authenticate with GCP
gcloud auth login
gcloud auth application-default login

# Set your project
export GCP_PROJECT_ID="your-project-id"
gcloud config set project $GCP_PROJECT_ID

# Enable required APIs
gcloud services enable \
  container.googleapis.com \
  compute.googleapis.com \
  sqladmin.googleapis.com \
  secretmanager.googleapis.com \
  cloudresourcemanager.googleapis.com
```

### 3. Create GCS Bucket for Terraform State

```bash
# Create bucket for Terraform state
gsutil mb -p $GCP_PROJECT_ID -l us-central1 gs://expense-tracker-terraform-state-${GCP_PROJECT_ID}

# Enable versioning
gsutil versioning set on gs://expense-tracker-terraform-state-${GCP_PROJECT_ID}
```

### 4. Create Environment Variable Files

```bash
# Staging
cd terraform/environments/staging
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your project ID

# Production
cd ../production
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your project ID
```

## Usage

### Using Terragrunt (Recommended)

Terragrunt manages backend configuration and keeps code DRY across environments.

```bash
# Staging
cd terraform/environments/staging

# Initialize
terragrunt init

# Plan changes
terragrunt plan

# Apply changes
terragrunt apply

# Destroy (careful!)
terragrunt destroy
```

```bash
# Production
cd terraform/environments/production

# Initialize
terragrunt init

# Plan changes
terragrunt plan

# Apply changes (requires approval)
terragrunt apply

# Destroy (careful!)
terragrunt destroy
```

### Using Terraform Directly (Alternative)

```bash
cd terraform

# Initialize
terraform init

# Plan with environment-specific variables
terraform plan -var-file=environments/staging/terraform.tfvars

# Apply
terraform apply -var-file=environments/staging/terraform.tfvars
```

## Environment Configuration

### Staging
- **Cluster**: 2 nodes (e2-medium), auto-scale 1-5
- **Database**: db-f1-micro (smallest tier)
- **Nodes**: Preemptible (cost savings)
- **Backups**: Daily, no point-in-time recovery
- **Deletion Protection**: Disabled

### Production
- **Cluster**: 3 nodes (e2-standard-2), auto-scale 3-10
- **Database**: db-custom-2-4096 (2 vCPU, 4GB RAM)
- **Nodes**: Standard (not preemptible)
- **Backups**: Daily with point-in-time recovery
- **Deletion Protection**: Enabled

## CI/CD Integration

### Terraform Plan (Pull Requests)
- Triggered on PRs that modify `terraform/**`
- Runs `terragrunt plan` for both staging and production
- Posts plan output as PR comment
- No changes applied

### Terraform Apply (Main Branch)
- Triggered on push to `main` branch
- Applies changes to staging first
- Requires manual approval for production
- Uses GitHub Environments for protection

### Required GitHub Secrets

Set these in your GitHub repository settings:

```
GCP_PROJECT_ID                  # Your GCP project ID
GCP_WORKLOAD_IDENTITY_PROVIDER  # Workload Identity Provider
GCP_SERVICE_ACCOUNT             # Service account email for GitHub Actions
```

### Setting up Workload Identity Federation

```bash
# Create service account for GitHub Actions
gcloud iam service-accounts create github-actions \
  --display-name="GitHub Actions Service Account"

# Grant necessary roles
gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member="serviceAccount:github-actions@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/editor"

# Create Workload Identity Pool
gcloud iam workload-identity-pools create "github-actions-pool" \
  --location="global" \
  --display-name="GitHub Actions Pool"

# Create Workload Identity Provider
gcloud iam workload-identity-pools providers create-oidc "github-provider" \
  --location="global" \
  --workload-identity-pool="github-actions-pool" \
  --display-name="GitHub Provider" \
  --attribute-mapping="google.subject=assertion.sub,attribute.actor=assertion.actor,attribute.repository=assertion.repository" \
  --issuer-uri="https://token.actions.githubusercontent.com"

# Bind service account to Workload Identity
gcloud iam service-accounts add-iam-policy-binding \
  "github-actions@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/projects/PROJECT_NUMBER/locations/global/workloadIdentityPools/github-actions-pool/attribute.repository/YOUR_GITHUB_ORG/YOUR_REPO"
```

## Outputs

After applying Terraform, you'll get:

- **GKE Cluster Name**: Used for kubectl configuration
- **GKE Cluster Endpoint**: Kubernetes API endpoint
- **Database Connection Name**: Cloud SQL connection string
- **Database Password Secret**: Secret Manager secret name
- **Static IP**: For ingress/load balancer

## Connecting to GKE

```bash
# Get cluster credentials
gcloud container clusters get-credentials expense-tracker-staging \
  --region us-central1 \
  --project $GCP_PROJECT_ID

# Verify connection
kubectl get nodes
```

## Connecting to Cloud SQL

```bash
# Using Cloud SQL Proxy
cloud_sql_proxy -instances=CONNECTION_NAME=tcp:5432

# Or use private IP from within GKE (recommended)
# Connection string available in Secret Manager
```

## Cost Optimization

### Staging
- Uses preemptible nodes (up to 80% cost savings)
- Smallest database tier (db-f1-micro)
- Minimal node count (1-5)
- No point-in-time recovery

### Production
- Standard nodes for reliability
- Appropriately sized database
- Higher node count for availability (3-10)
- Point-in-time recovery enabled
- Deletion protection enabled

## Troubleshooting

### State Lock Issues
```bash
# If state is locked, force unlock (use with caution)
terragrunt force-unlock LOCK_ID
```

### Permission Errors
```bash
# Verify your authentication
gcloud auth list
gcloud config get-value project

# Check service account permissions
gcloud projects get-iam-policy $GCP_PROJECT_ID
```

### API Not Enabled
```bash
# Enable all required APIs
gcloud services enable container.googleapis.com compute.googleapis.com sqladmin.googleapis.com
```

## Security Best Practices

1. **Never commit `terraform.tfvars`** - Contains sensitive data (gitignored)
2. **Use Workload Identity** - No service account keys needed
3. **Enable deletion protection** - Prevents accidental resource deletion in production
4. **Use Secret Manager** - Store database passwords and API keys
5. **Private GKE cluster** - No public endpoint (optional, can be enabled)
6. **Least privilege IAM** - Grant minimum necessary permissions

## Next Steps

After infrastructure is provisioned:

1. **Configure kubectl** to connect to GKE
2. **Deploy Kubernetes manifests** from `k8s/` directory
3. **Set up ingress** with the static IP created by Terraform
4. **Configure DNS** to point to the static IP
5. **Deploy application** using CD pipelines

## Maintenance

### Updating Infrastructure
1. Make changes to Terraform files
2. Create PR - triggers `terraform-plan.yml`
3. Review plan output in PR comments
4. Merge PR - triggers `terraform-apply.yml`
5. Changes applied automatically (staging → production)

### Destroying Infrastructure
```bash
# Staging
cd terraform/environments/staging
terragrunt destroy

# Production (requires disabling deletion protection first)
cd terraform/environments/production
terragrunt destroy
```

## References

- [Terraform GCP Provider](https://registry.terraform.io/providers/hashicorp/google/latest/docs)
- [Terragrunt Documentation](https://terragrunt.gruntwork.io/docs/)
- [GKE Best Practices](https://cloud.google.com/kubernetes-engine/docs/best-practices)
- [Cloud SQL Best Practices](https://cloud.google.com/sql/docs/postgres/best-practices)
