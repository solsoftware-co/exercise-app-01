# What is Terragrunt? 🤔

Terragrunt is a **thin wrapper around Terraform** that provides extra tools for keeping your Terraform configurations DRY (Don't Repeat Yourself), managing remote state, and working with multiple environments.

## The Problem Terragrunt Solves

### Without Terragrunt (The Pain)

Imagine you have staging and production environments. With plain Terraform, you'd do this:

```
terraform/
├── staging/
│   ├── backend.tf       # Backend config for staging
│   ├── provider.tf      # Provider config for staging
│   ├── main.tf          # Infrastructure code (DUPLICATED!)
│   ├── variables.tf     # Variables (DUPLICATED!)
│   └── terraform.tfvars # Staging values
└── production/
    ├── backend.tf       # Backend config for production (DUPLICATED!)
    ├── provider.tf      # Provider config for production (DUPLICATED!)
    ├── main.tf          # Infrastructure code (DUPLICATED!)
    ├── variables.tf     # Variables (DUPLICATED!)
    └── terraform.tfvars # Production values
```

**Problems:**
- ❌ Code duplication (main.tf, variables.tf copied everywhere)
- ❌ Backend config hardcoded in each environment
- ❌ Changes require updating multiple files
- ❌ Easy to have drift between environments
- ❌ More code to maintain

### With Terragrunt (The Solution)

```
terraform/
├── terragrunt.hcl       # Root config (DRY!)
├── main.tf              # Infrastructure code (ONCE!)
├── variables.tf         # Variables (ONCE!)
└── environments/
    ├── staging/
    │   └── terragrunt.hcl    # Just staging-specific values
    └── production/
        └── terragrunt.hcl    # Just production-specific values
```

**Benefits:**
- ✅ Write infrastructure code once
- ✅ Backend config auto-generated
- ✅ Provider config auto-generated
- ✅ Only environment-specific values differ
- ✅ Easy to keep environments in sync

---

## How Terragrunt Works

### 1. Root Configuration (`terragrunt.hcl`)

This file lives at the root of your Terraform directory and defines **common configuration** for all environments:

```hcl
# terraform/terragrunt.hcl

# Generate backend configuration automatically
remote_state {
  backend = "gcs"
  
  generate = {
    path      = "backend_override.tf"
    if_exists = "overwrite"
  }
  
  config = {
    bucket   = "my-terraform-state-${get_env("GCP_PROJECT_ID")}"
    prefix   = "${path_relative_to_include()}/terraform.tfstate"
    project  = get_env("GCP_PROJECT_ID")
  }
}

# Generate provider configuration automatically
generate "provider" {
  path      = "provider_override.tf"
  if_exists = "overwrite_terragrunt"
  contents  = <<EOF
provider "google" {
  project = var.project_id
  region  = var.region
}
EOF
}

# Common inputs for all environments
inputs = {
  project_id = get_env("GCP_PROJECT_ID")
  region     = "us-central1"
}
```

**What this does:**
- 📝 **Generates `backend_override.tf`** - No need to hardcode backend config
- 📝 **Generates `provider_override.tf`** - No need to duplicate provider config
- 🔧 **Sets common variables** - Project ID, region, etc.
- 🌍 **Uses environment variables** - `get_env("GCP_PROJECT_ID")` reads from shell

### 2. Environment Configuration

Each environment has a simple `terragrunt.hcl` that **only contains what's different**:

```hcl
# terraform/environments/staging/terragrunt.hcl

# Include the root configuration
include "root" {
  path = find_in_parent_folders()
}

# Point to the root Terraform code
terraform {
  source = "../.."
}

# Environment-specific inputs
inputs = {
  environment    = "staging"
  cluster_name   = "expense-tracker-staging"
  node_count     = 1
  machine_type   = "e2-small"
}
```

**What this does:**
- 📦 **Includes root config** - Inherits backend, provider, common vars
- 📂 **Points to Terraform code** - Uses the shared `main.tf`
- ⚙️ **Overrides specific values** - Only what's different for staging

---

## Real-World Example: Your Project

### What Happens When You Run `terragrunt apply`

```bash
cd terraform/environments/staging
terragrunt apply
```

**Step-by-step:**

1. **Terragrunt reads `terragrunt.hcl`** in staging directory
2. **Finds root `terragrunt.hcl`** using `find_in_parent_folders()`
3. **Generates `backend_override.tf`**:
   ```hcl
   terraform {
     backend "gcs" {
       bucket = "expense-tracker-terraform-state-your-project-id"
       prefix = "environments/staging/terraform.tfstate"
     }
   }
   ```
4. **Generates `provider_override.tf`**:
   ```hcl
   provider "google" {
     project = "your-project-id"
     region  = "us-central1"
   }
   ```
5. **Merges inputs** from root + environment:
   ```hcl
   project_id     = "your-project-id"      # From root
   region         = "us-central1"          # From root
   environment    = "staging"              # From environment
   cluster_name   = "expense-tracker-staging"  # From environment
   node_count     = 1                      # From environment
   machine_type   = "e2-small"             # From environment
   ```
6. **Runs Terraform** with all the generated files and merged inputs

---

## Key Terragrunt Features

### 1. DRY Backend Configuration

**Without Terragrunt:**
```hcl
# Hardcoded in every environment
terraform {
  backend "gcs" {
    bucket = "my-terraform-state-staging"
    prefix = "terraform/state"
  }
}
```

**With Terragrunt:**
```hcl
# Auto-generated based on environment
remote_state {
  backend = "gcs"
  config = {
    bucket = "my-state-${get_env("PROJECT_ID")}"
    prefix = "${path_relative_to_include()}/terraform.tfstate"
  }
}
```

### 2. DRY Provider Configuration

**Without Terragrunt:**
```hcl
# Duplicated in every environment
provider "google" {
  project = "my-project-id"
  region  = "us-central1"
}
```

**With Terragrunt:**
```hcl
# Auto-generated from root config
generate "provider" {
  path = "provider_override.tf"
  contents = <<EOF
provider "google" {
  project = var.project_id
  region  = var.region
}
EOF
}
```

### 3. Environment Variables

```hcl
# Read from shell environment
project_id = get_env("GCP_PROJECT_ID", "default-value")
```

### 4. Path Helpers

```hcl
# Find parent terragrunt.hcl
include "root" {
  path = find_in_parent_folders()
}

# Get relative path for state files
prefix = "${path_relative_to_include()}/terraform.tfstate"
# staging:    "environments/staging/terraform.tfstate"
# production: "environments/production/terraform.tfstate"
```

---

## Terragrunt Commands

### Basic Commands (Same as Terraform)

```bash
terragrunt init      # Initialize Terraform
terragrunt plan      # Show what will change
terragrunt apply     # Apply changes
terragrunt destroy   # Destroy infrastructure
terragrunt output    # Show outputs
```

### Terragrunt-Specific Commands

```bash
# Run command in all subdirectories
terragrunt run-all apply

# Validate all configurations
terragrunt validate-all

# Show generated backend config
terragrunt terragrunt-info
```

---

## Your Project Structure Explained

```
terraform/
├── terragrunt.hcl              # 🌍 Root: Common config for all environments
├── main.tf                     # 🏗️ Infrastructure code (GKE, Cloud SQL, etc.)
├── variables.tf                # 📋 Variable definitions
├── outputs.tf                  # 📤 Output values
└── environments/
    ├── staging/
    │   └── terragrunt.hcl      # ⚙️ Staging: node_count=1, machine_type=e2-small
    └── production/
        └── terragrunt.hcl      # ⚙️ Production: node_count=1, machine_type=e2-small
```

**When you run `terragrunt apply` in staging:**
- Uses `main.tf` (shared infrastructure code)
- Generates backend config pointing to `environments/staging/terraform.tfstate`
- Merges inputs: common (project_id, region) + staging-specific (node_count, machine_type)
- Creates 1 e2-small node in staging cluster

**When you run `terragrunt apply` in production:**
- Uses **same** `main.tf` (no duplication!)
- Generates backend config pointing to `environments/production/terraform.tfstate`
- Merges inputs: common (project_id, region) + production-specific (node_count, machine_type)
- Creates 1 e2-small node in production cluster

---

## Why Use Terragrunt?

### ✅ Pros
- **DRY**: Write infrastructure code once, reuse everywhere
- **Less code**: No duplicated backend/provider configs
- **Consistency**: Environments stay in sync
- **Flexibility**: Easy to override per environment
- **State management**: Automatic state file organization
- **Scalability**: Easy to add new environments

### ⚠️ Cons
- **Learning curve**: One more tool to learn
- **Abstraction**: Adds layer on top of Terraform
- **Debugging**: Slightly harder to debug generated files

---

## When to Use Terragrunt

### ✅ Use Terragrunt When:
- You have multiple environments (staging, production, dev)
- You want to keep code DRY
- You need consistent backend configuration
- You're managing multiple projects/teams

### ❌ Skip Terragrunt When:
- Single environment only
- Very simple Terraform setup
- Team unfamiliar with Terraform (learn Terraform first)

---

## Learning Resources

- **Official Docs**: https://terragrunt.gruntwork.io/docs/
- **Getting Started**: https://terragrunt.gruntwork.io/docs/getting-started/quick-start/
- **Best Practices**: https://terragrunt.gruntwork.io/docs/features/keep-your-terraform-code-dry/

---

## Quick Comparison

| Feature | Terraform Only | Terraform + Terragrunt |
|---------|---------------|------------------------|
| **Code Duplication** | High (copy main.tf everywhere) | Low (write once) |
| **Backend Config** | Hardcoded per environment | Auto-generated |
| **Provider Config** | Duplicated per environment | Auto-generated |
| **Environment Differences** | Separate tfvars files | Separate terragrunt.hcl |
| **Learning Curve** | Moderate | Moderate + Terragrunt |
| **Maintenance** | More files to update | Fewer files to update |

---

## TL;DR

**Terragrunt = Terraform + DRY + Auto-generated configs**

Instead of copying Terraform code for each environment, you:
1. Write infrastructure code **once** (`main.tf`)
2. Define **common config** in root `terragrunt.hcl`
3. Define **environment-specific values** in each environment's `terragrunt.hcl`
4. Terragrunt generates backend/provider configs and merges inputs automatically

**Result:** Less code, more consistency, easier maintenance! 🎉
