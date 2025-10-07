# Deployment Guide

This guide covers deploying the Expense Tracker application to Google Kubernetes Engine (GKE).

## Prerequisites

- Google Cloud Platform account
- `gcloud` CLI installed and configured
- `kubectl` installed
- `terraform` installed
- Docker installed
- GitHub repository with appropriate secrets configured

## Infrastructure Setup with Terraform

### 1. Create GCS Bucket for Terraform State

```bash
gsutil mb -p YOUR_PROJECT_ID -l us-central1 gs://YOUR_BUCKET_NAME
gsutil versioning set on gs://YOUR_BUCKET_NAME
```

### 2. Configure Terraform Backend

Edit `terraform/backend.tf` and replace `REPLACE_WITH_YOUR_BUCKET_NAME` with your bucket name.

### 3. Create Terraform Variables

For staging:
```bash
cd terraform/environments/staging
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your values
```

For production:
```bash
cd terraform/environments/production
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your values
```

### 4. Initialize and Apply Terraform

For staging:
```bash
cd terraform
terraform init
terraform workspace new staging
terraform workspace select staging
terraform plan -var-file=environments/staging/terraform.tfvars
terraform apply -var-file=environments/staging/terraform.tfvars
```

For production:
```bash
terraform workspace new production
terraform workspace select production
terraform plan -var-file=environments/production/terraform.tfvars
terraform apply -var-file=environments/production/terraform.tfvars
```

## GitHub Secrets Configuration

Configure the following secrets in your GitHub repository:

### Required Secrets

- `GCP_PROJECT_ID` - Your GCP project ID
- `GCP_SA_KEY` - Service account JSON key with permissions:
  - Container Registry Admin
  - Kubernetes Engine Admin
  - Service Account User
- `GKE_CLUSTER_STAGING` - Staging cluster name
- `GKE_CLUSTER_PRODUCTION` - Production cluster name
- `GKE_REGION` - GKE cluster region (e.g., us-central1)
- `TF_STATE_BUCKET` - GCS bucket for Terraform state

### Creating Service Account

```bash
# Create service account
gcloud iam service-accounts create github-actions \
  --display-name="GitHub Actions"

# Grant necessary roles
gcloud projects add-iam-policy-binding YOUR_PROJECT_ID \
  --member="serviceAccount:github-actions@YOUR_PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/container.admin"

gcloud projects add-iam-policy-binding YOUR_PROJECT_ID \
  --member="serviceAccount:github-actions@YOUR_PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/storage.admin"

# Create and download key
gcloud iam service-accounts keys create key.json \
  --iam-account=github-actions@YOUR_PROJECT_ID.iam.gserviceaccount.com

# Copy the contents of key.json to GCP_SA_KEY secret
cat key.json
```

## Kubernetes Secrets Setup

### 1. Create Database Secret

For staging:
```bash
kubectl create secret generic db-secret \
  --from-literal=database=expense_tracker \
  --from-literal=username=postgres \
  --from-literal=password=YOUR_SECURE_PASSWORD \
  --namespace=expense-tracker-staging
```

For production:
```bash
kubectl create secret generic db-secret \
  --from-literal=database=expense_tracker \
  --from-literal=username=postgres \
  --from-literal=password=YOUR_SECURE_PASSWORD \
  --namespace=expense-tracker-production
```

### 2. Using External Secrets Operator (Recommended for Production)

Install External Secrets Operator:
```bash
helm repo add external-secrets https://charts.external-secrets.io
helm install external-secrets \
  external-secrets/external-secrets \
  -n external-secrets-system \
  --create-namespace
```

Create SecretStore and ExternalSecret resources to sync from GCP Secret Manager.

## Manual Deployment

### 1. Build and Push Docker Images

```bash
# Authenticate Docker with GCR
gcloud auth configure-docker

# Build and push backend
cd backend
docker build -t gcr.io/YOUR_PROJECT_ID/expense-tracker-backend:v1.0.0 .
docker push gcr.io/YOUR_PROJECT_ID/expense-tracker-backend:v1.0.0

# Build and push frontend
cd ../frontend
docker build -t gcr.io/YOUR_PROJECT_ID/expense-tracker-frontend:v1.0.0 .
docker push gcr.io/YOUR_PROJECT_ID/expense-tracker-frontend:v1.0.0
```

### 2. Update Kubernetes Manifests

Update image tags in `k8s/overlays/staging/kustomization.yaml` or `k8s/overlays/production/kustomization.yaml`.

### 3. Deploy to Kubernetes

For staging:
```bash
kubectl apply -k k8s/overlays/staging
```

For production:
```bash
kubectl apply -k k8s/overlays/production
```

### 4. Verify Deployment

```bash
# Check pods
kubectl get pods -n expense-tracker-staging

# Check services
kubectl get services -n expense-tracker-staging

# Check ingress
kubectl get ingress -n expense-tracker-staging

# View logs
kubectl logs -f deployment/staging-backend -n expense-tracker-staging
kubectl logs -f deployment/staging-frontend -n expense-tracker-staging
```

## CI/CD Workflow

### Automatic Deployments

- **Staging**: Automatically deploys when code is pushed to `develop` branch
- **Production**: Automatically deploys when code is pushed to `main` branch or a version tag is created

### Workflow Steps

1. **CI Pipeline** (on PR):
   - Run backend tests
   - Run frontend tests
   - Build Docker images

2. **CD Pipeline - Staging** (on merge to develop):
   - Build and push Docker images to GCR
   - Deploy to staging GKE cluster
   - Verify deployment

3. **CD Pipeline - Production** (on merge to main):
   - Build and push Docker images to GCR
   - Deploy to production GKE cluster
   - Run smoke tests
   - Verify deployment

## Monitoring and Logging

### View Logs

```bash
# Backend logs
kubectl logs -f deployment/backend -n expense-tracker-production

# Frontend logs
kubectl logs -f deployment/frontend -n expense-tracker-production
```

### Access GCP Monitoring

1. Go to GCP Console
2. Navigate to Monitoring > Dashboards
3. View metrics for GKE cluster and workloads

### Set Up Alerts

Configure alerts in GCP Monitoring for:
- High CPU/Memory usage
- Pod restarts
- Failed health checks
- Error rates

## Rollback

### Rollback to Previous Version

```bash
# View rollout history
kubectl rollout history deployment/backend -n expense-tracker-production

# Rollback to previous version
kubectl rollout undo deployment/backend -n expense-tracker-production

# Rollback to specific revision
kubectl rollout undo deployment/backend --to-revision=2 -n expense-tracker-production
```

## Scaling

### Manual Scaling

```bash
# Scale backend
kubectl scale deployment/backend --replicas=5 -n expense-tracker-production

# Scale frontend
kubectl scale deployment/frontend --replicas=5 -n expense-tracker-production
```

### Horizontal Pod Autoscaler

HPA is already configured in `k8s/base/hpa.yaml` and will automatically scale based on CPU and memory usage.

## Troubleshooting

### Pods Not Starting

```bash
# Describe pod to see events
kubectl describe pod POD_NAME -n expense-tracker-production

# Check logs
kubectl logs POD_NAME -n expense-tracker-production
```

### Database Connection Issues

```bash
# Check if postgres pod is running
kubectl get pods -n expense-tracker-production | grep postgres

# Check database secret
kubectl get secret db-secret -n expense-tracker-production -o yaml

# Test connection from backend pod
kubectl exec -it POD_NAME -n expense-tracker-production -- /bin/sh
# Inside pod: nc -zv postgres 5432
```

### Ingress Not Working

```bash
# Check ingress status
kubectl describe ingress expense-tracker-ingress -n expense-tracker-production

# Verify backend service
kubectl get service backend -n expense-tracker-production

# Check if load balancer is provisioned
gcloud compute forwarding-rules list
```

## Cleanup

### Delete Kubernetes Resources

```bash
kubectl delete namespace expense-tracker-staging
kubectl delete namespace expense-tracker-production
```

### Destroy Terraform Infrastructure

```bash
cd terraform
terraform workspace select staging
terraform destroy -var-file=environments/staging/terraform.tfvars

terraform workspace select production
terraform destroy -var-file=environments/production/terraform.tfvars
```

## Security Best Practices

1. **Never commit secrets** to version control
2. **Use External Secrets Operator** for production
3. **Enable Workload Identity** for GKE pods
4. **Regularly update dependencies** and base images
5. **Implement network policies** to restrict pod-to-pod communication
6. **Use managed certificates** for HTTPS
7. **Enable audit logging** in GKE
8. **Implement RBAC** for Kubernetes access control
