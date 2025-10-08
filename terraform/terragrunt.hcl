# Root Terragrunt configuration
# This file is used by all environment-specific terragrunt.hcl files

# Generate backend configuration
remote_state {
  backend = "gcs"
  
  generate = {
    path      = "backend_override.tf"
    if_exists = "overwrite"
  }
  
  config = {
    bucket   = "expense-tracker-terraform-state-${get_env("GCP_PROJECT_ID", "REPLACE_ME")}"
    prefix   = "${path_relative_to_include()}/terraform.tfstate"
    project  = get_env("GCP_PROJECT_ID", "REPLACE_ME")
    location = "us-central1"
  }
}

# Generate provider configuration
generate "provider" {
  path      = "provider_override.tf"
  if_exists = "overwrite_terragrunt"
  contents  = <<EOF
terraform {
  required_version = ">= 1.5"
  
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.5"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}
EOF
}

# Input variables that are common across all environments
inputs = {
  project_id = get_env("GCP_PROJECT_ID", "REPLACE_ME")
  region     = "us-central1"
}
