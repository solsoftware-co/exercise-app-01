#!/bin/bash
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Expense Tracker Infrastructure Setup ===${NC}\n"

# Check if GCP_PROJECT_ID is set
if [ -z "$GCP_PROJECT_ID" ]; then
    echo -e "${RED}Error: GCP_PROJECT_ID environment variable not set${NC}"
    echo "Please set it with: export GCP_PROJECT_ID=your-project-id"
    exit 1
fi

echo -e "${GREEN}✓ Using GCP Project: $GCP_PROJECT_ID${NC}\n"

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo -e "${RED}Error: gcloud CLI not found${NC}"
    echo "Install it with: brew install --cask google-cloud-sdk"
    exit 1
fi

# Check if terraform is installed
if ! command -v terraform &> /dev/null; then
    echo -e "${RED}Error: terraform not found${NC}"
    echo "Install it with: brew install terraform"
    exit 1
fi

# Check if terragrunt is installed
if ! command -v terragrunt &> /dev/null; then
    echo -e "${YELLOW}Warning: terragrunt not found${NC}"
    echo "Install it with: brew install terragrunt"
    echo "You can still use terraform directly, but terragrunt is recommended"
fi

echo -e "${GREEN}Step 1: Authenticating with GCP...${NC}"
gcloud auth application-default login

echo -e "\n${GREEN}Step 2: Setting GCP project...${NC}"
gcloud config set project $GCP_PROJECT_ID

echo -e "\n${GREEN}Step 3: Enabling required GCP APIs...${NC}"
gcloud services enable \
  container.googleapis.com \
  compute.googleapis.com \
  sqladmin.googleapis.com \
  secretmanager.googleapis.com \
  cloudresourcemanager.googleapis.com \
  servicenetworking.googleapis.com

echo -e "\n${GREEN}Step 4: Creating GCS bucket for Terraform state...${NC}"
BUCKET_NAME="expense-tracker-terraform-state-${GCP_PROJECT_ID}"

if gsutil ls -b gs://$BUCKET_NAME &> /dev/null; then
    echo -e "${YELLOW}Bucket already exists: gs://$BUCKET_NAME${NC}"
else
    gsutil mb -p $GCP_PROJECT_ID -l us-central1 gs://$BUCKET_NAME
    gsutil versioning set on gs://$BUCKET_NAME
    echo -e "${GREEN}✓ Created bucket: gs://$BUCKET_NAME${NC}"
fi

echo -e "\n${GREEN}Step 5: Creating terraform.tfvars files...${NC}"

# Staging
STAGING_TFVARS="environments/staging/terraform.tfvars"
if [ ! -f "$STAGING_TFVARS" ]; then
    cat > "$STAGING_TFVARS" << EOF
project_id     = "$GCP_PROJECT_ID"
region         = "us-central1"
environment    = "staging"
cluster_name   = "expense-tracker-staging"
node_count     = 2
machine_type   = "e2-medium"
min_node_count = 1
max_node_count = 5
EOF
    echo -e "${GREEN}✓ Created $STAGING_TFVARS${NC}"
else
    echo -e "${YELLOW}$STAGING_TFVARS already exists${NC}"
fi

# Production
PRODUCTION_TFVARS="environments/production/terraform.tfvars"
if [ ! -f "$PRODUCTION_TFVARS" ]; then
    cat > "$PRODUCTION_TFVARS" << EOF
project_id     = "$GCP_PROJECT_ID"
region         = "us-central1"
environment    = "production"
cluster_name   = "expense-tracker-production"
node_count     = 3
machine_type   = "e2-standard-2"
min_node_count = 3
max_node_count = 10
EOF
    echo -e "${GREEN}✓ Created $PRODUCTION_TFVARS${NC}"
else
    echo -e "${YELLOW}$PRODUCTION_TFVARS already exists${NC}"
fi

echo -e "\n${GREEN}=== Setup Complete! ===${NC}\n"
echo -e "Next steps:"
echo -e "1. Review the generated terraform.tfvars files"
echo -e "2. Initialize Terragrunt:"
echo -e "   ${YELLOW}cd environments/staging && terragrunt init${NC}"
echo -e "3. Plan infrastructure changes:"
echo -e "   ${YELLOW}terragrunt plan${NC}"
echo -e "4. Apply infrastructure:"
echo -e "   ${YELLOW}terragrunt apply${NC}"
echo -e "\n${GREEN}For production, repeat steps 2-4 in environments/production/${NC}"
