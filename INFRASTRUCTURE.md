# Infrastructure Setup Guide

This guide walks you through setting up the GCP infrastructure for the Expense Tracker application.

## 🏗️ Architecture Overview

- **Cloud Provider**: Google Cloud Platform (GCP)
- **Kubernetes**: GKE (Google Kubernetes Engine)
- **Database**: Cloud SQL PostgreSQL 15
- **Environment Strategy**: Single GCP project with namespace isolation
- **IaC Tool**: Terraform + Terragrunt

## 📋 Prerequisites

### 1. Install Required Tools

```bash
# Terraform
brew install terraform

# Terragrunt (recommended for multi-environment)
brew install terragrunt

# Google Cloud SDK
brew install --cask google-cloud-sdk
```

### 2. Set Up GCP Project

```bash
# Set your project ID
export GCP_PROJECT_ID="your-project-id"

# Authenticate
gcloud auth login
gcloud auth application-default login

# Set project
gcloud config set project $GCP_PROJECT_ID
```

## 🚀 Quick Start

### Option 1: Automated Setup (Recommended)

```bash
cd terraform
export GCP_PROJECT_ID="your-project-id"
./setup.sh
```

This script will:
- ✅ Authenticate with GCP
- ✅ Enable required APIs
- ✅ Create GCS bucket for Terraform state
- ✅ Generate `terraform.tfvars` files for both environments

### Option 2: Manual Setup

See detailed steps in [`terraform/README.md`](terraform/README.md)

## 🏃 Deploy Infrastructure

### Staging Environment

```bash
cd terraform/environments/staging

# Initialize Terragrunt
terragrunt init

# Review planned changes
terragrunt plan

# Apply infrastructure
terragrunt apply
```

### Production Environment

```bash
cd terraform/environments/production

# Initialize Terragrunt
terragrunt init

# Review planned changes
terragrunt plan

# Apply infrastructure (requires approval)
terragrunt apply
```

## 📊 What Gets Created

### Networking
- VPC: `expense-tracker-{env}-vpc`
- Subnet: `expense-tracker-{env}-subnet`
- Secondary IP ranges for pods and services

### GKE Cluster
- **Staging**: 2 nodes (e2-medium), preemptible, auto-scale 1-5
- **Production**: 3 nodes (e2-standard-2), standard, auto-scale 3-10
- Workload Identity enabled
- Network policies enabled
- Monitoring and logging enabled

### Cloud SQL
- **Staging**: db-f1-micro, daily backups
- **Production**: db-custom-2-4096, daily backups + point-in-time recovery
- Private IP only (no public access)
- Database: `expense_tracker`
- User: `expense_tracker_user`

### Security
- Service account for GKE nodes
- Database password stored in Secret Manager
- IAM roles with least privilege

## 🔐 Secrets Management

After infrastructure is created, database credentials are stored in:

```bash
# Get database password
gcloud secrets versions access latest \
  --secret="expense-tracker-{env}-db-password" \
  --project=$GCP_PROJECT_ID
```

## 🔗 Connect to GKE

```bash
# Get cluster credentials
gcloud container clusters get-credentials expense-tracker-staging \
  --region us-central1 \
  --project $GCP_PROJECT_ID

# Verify connection
kubectl get nodes
kubectl get namespaces
```

## 📝 Environment Variables for Kubernetes

After infrastructure is provisioned, you'll need these values for your K8s deployments:

```bash
# Get database connection details
cd terraform/environments/staging
terragrunt output

# Outputs:
# - cluster_name
# - cluster_endpoint
# - database_connection_name
# - database_password_secret_name
# - static_ip_address
```

## 🔄 CI/CD Integration

### GitHub Actions Workflows

1. **`terraform-plan.yml`** - Runs on PRs
   - Plans changes for both staging and production
   - Posts plan output as PR comment
   - No changes applied

2. **`terraform-apply.yml`** - Runs on merge to main
   - Applies changes to staging automatically
   - Requires manual approval for production
   - Uses GitHub Environments for protection

### Required GitHub Secrets

Set these in **Settings → Secrets and variables → Actions**:

```
GCP_PROJECT_ID                  # Your GCP project ID
GCP_WORKLOAD_IDENTITY_PROVIDER  # Format: projects/PROJECT_NUMBER/locations/global/workloadIdentityPools/POOL_ID/providers/PROVIDER_ID
GCP_SERVICE_ACCOUNT             # Format: github-actions@PROJECT_ID.iam.gserviceaccount.com
```

### Setting Up Workload Identity Federation

See [`terraform/README.md`](terraform/README.md) for detailed instructions on setting up Workload Identity Federation for GitHub Actions.

## 💰 Cost Estimates (Learning/Demo Configuration)

### Staging (Monthly)
- GKE: ~$25 (1 preemptible e2-small node)
- Cloud SQL: ~$10 (db-f1-micro)
- Networking: ~$5
- **Total: ~$40/month**

### Production (Monthly)
- GKE: ~$50 (1 e2-small node)
- Cloud SQL: ~$10 (db-f1-micro)
- Networking: ~$5
- **Total: ~$65/month**

**💡 Cost Saving Tips:**
- Use only staging for learning (save ~$65/month)
- Stop cluster when not in use: `gcloud container clusters resize expense-tracker-staging --num-nodes=0`
- Delete when done learning: `terragrunt destroy`
- GCP Free Tier includes $300 credit for 90 days

## 🧹 Cleanup

### Destroy Staging

```bash
cd terraform/environments/staging
terragrunt destroy
```

### Destroy Production

```bash
# First, disable deletion protection
# Edit main.tf and set deletion_protection = false

cd terraform/environments/production
terragrunt apply  # Apply the deletion_protection change
terragrunt destroy
```

## 🔧 Troubleshooting

### "API not enabled" Error
```bash
gcloud services enable container.googleapis.com compute.googleapis.com sqladmin.googleapis.com
```

### "Permission denied" Error
```bash
# Verify authentication
gcloud auth list
gcloud auth application-default login
```

### State Lock Issues
```bash
# Force unlock (use with caution)
terragrunt force-unlock LOCK_ID
```

## 📚 Next Steps

After infrastructure is provisioned:

1. ✅ **Infrastructure Created** (You are here)
2. ⏭️ **Deploy Kubernetes Manifests** - Apply configs from `k8s/` directory
3. ⏭️ **Set Up Ingress** - Configure ingress with static IP
4. ⏭️ **Configure DNS** - Point domain to static IP
5. ⏭️ **Deploy Application** - Use CD pipelines to deploy

## 📖 Additional Resources

- [Terraform Documentation](terraform/README.md)
- [GKE Best Practices](https://cloud.google.com/kubernetes-engine/docs/best-practices)
- [Cloud SQL Best Practices](https://cloud.google.com/sql/docs/postgres/best-practices)
- [Terragrunt Documentation](https://terragrunt.gruntwork.io/docs/)
