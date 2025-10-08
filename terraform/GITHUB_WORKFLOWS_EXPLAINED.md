# GitHub Workflows for Terraform Automation 🤖

This guide explains how the Terraform GitHub Actions workflows work and when they run.

## Overview

We have **two workflows** that automate Terraform infrastructure management:

| Workflow | File | When It Runs | What It Does | Changes Infrastructure? |
|----------|------|--------------|--------------|------------------------|
| **Terraform Plan** | `terraform-plan.yml` | On Pull Requests | Shows preview of changes | ❌ No |
| **Terraform Apply** | `terraform-apply.yml` | On merge to `main` | Actually applies changes | ✅ Yes |

---

## 🔍 Workflow 1: `terraform-plan.yml`

### **Purpose: Preview Infrastructure Changes**

This workflow is your **safety net**. It shows you exactly what Terraform will do **before** any changes are made.

### **When It Runs:**

```yaml
on:
  pull_request:
    paths:
      - 'terraform/**'
      - '.github/workflows/terraform-*.yml'
```

**Triggers:**
- ✅ When you **create or update a Pull Request**
- ✅ Only if you modified files in `terraform/` directory
- ✅ Or if you modified the Terraform workflows themselves

**Does NOT run:**
- ❌ On direct pushes to `main` branch
- ❌ If you only changed `backend/` or `frontend/` code
- ❌ On merge (that's when `terraform-apply.yml` runs)

### **What It Does:**

#### **Job 1: Plan Staging**

```yaml
plan-staging:
  name: Plan Staging
  runs-on: ubuntu-latest
```

**Steps:**

1. **Checkout code** (`actions/checkout@v4`)
   - Gets your latest code from the PR

2. **Authenticate to Google Cloud** (`google-github-actions/auth@v2`)
   - Uses Workload Identity Federation (no service account keys!)
   - Requires secrets: `GCP_WORKLOAD_IDENTITY_PROVIDER`, `GCP_SERVICE_ACCOUNT`

3. **Set up Cloud SDK** (`google-github-actions/setup-gcloud@v2`)
   - Installs `gcloud` CLI

4. **Setup Terraform** (`hashicorp/setup-terraform@v3`)
   - Installs Terraform v1.7.0

5. **Setup Terragrunt**
   - Downloads and installs Terragrunt v0.55.1

6. **Terragrunt Init**
   ```bash
   cd terraform/environments/staging
   terragrunt init
   ```
   - Initializes Terraform
   - Downloads providers (Google Cloud)
   - Configures remote state backend

7. **Terragrunt Plan**
   ```bash
   terragrunt plan -out=tfplan
   ```
   - Shows what infrastructure changes will happen
   - Saves plan to `tfplan` file

8. **Comment PR with Plan**
   - Posts the plan output as a comment on your PR
   - Shows resources to add/change/destroy

#### **Job 2: Plan Production**

Same steps as staging, but runs in `terraform/environments/production/`

### **Example Plan Output:**

When you create a PR, you'll see a comment like this:

```
#### Terraform Plan (Staging) 📝

<details><summary>Show Plan</summary>

Terraform will perform the following actions:

  # google_container_node_pool.primary_nodes will be updated in-place
  ~ resource "google_container_node_pool" "primary_nodes" {
      ~ node_count = 1 -> 2
    }

Plan: 0 to add, 1 to change, 0 to destroy.

</details>

*Pusher: @your-username, Action: `pull_request`*
```

### **How to Read the Plan:**

- **`+`** = Resource will be **created**
- **`~`** = Resource will be **modified** (in-place)
- **`-/+`** = Resource will be **destroyed and recreated**
- **`-`** = Resource will be **destroyed**

**Summary line:**
```
Plan: 2 to add, 1 to change, 0 to destroy
```
- 2 new resources created
- 1 existing resource modified
- 0 resources deleted

---

## ✅ Workflow 2: `terraform-apply.yml`

### **Purpose: Actually Apply Infrastructure Changes**

This workflow **makes the changes** to your infrastructure. It runs automatically after you merge a PR.

### **When It Runs:**

```yaml
on:
  push:
    branches:
      - main
    paths:
      - 'terraform/**'
      - '.github/workflows/terraform-*.yml'
```

**Triggers:**
- ✅ When you **merge a PR to `main`** branch
- ✅ Only if the merged PR modified `terraform/` files
- ✅ Or if you modified the Terraform workflows

**Does NOT run:**
- ❌ On Pull Requests (that's when `terraform-plan.yml` runs)
- ❌ On pushes to other branches
- ❌ If you only changed non-Terraform files

### **What It Does:**

#### **Job 1: Apply Staging**

```yaml
apply-staging:
  name: Apply Staging
  runs-on: ubuntu-latest
  environment: staging
```

**Steps:**

1. **Checkout code**
2. **Authenticate to Google Cloud**
3. **Set up Cloud SDK**
4. **Setup Terraform**
5. **Setup Terragrunt**
6. **Terragrunt Init**
   ```bash
   cd terraform/environments/staging
   terragrunt init
   ```
7. **Terragrunt Apply**
   ```bash
   terragrunt apply -auto-approve
   ```
   - **Actually creates/modifies/destroys infrastructure**
   - `-auto-approve` means no manual confirmation needed
   - Changes are applied to GCP

#### **Job 2: Apply Production**

```yaml
apply-production:
  name: Apply Production
  runs-on: ubuntu-latest
  environment: production
  needs: apply-staging  # Waits for staging to succeed
```

**Important:**
- **Runs AFTER staging succeeds** (`needs: apply-staging`)
- Uses GitHub **Environment protection rules**
- Can require **manual approval** before running

---

## 🔄 Complete Workflow: From PR to Deployment

### **Scenario: You want to increase node count from 1 to 2**

#### **Step 1: Make Changes**

```bash
# Create a new branch
git checkout -b increase-nodes

# Edit the file
# terraform/environments/staging/terragrunt.hcl
# Change: node_count = 1 → node_count = 2

# Commit and push
git add terraform/environments/staging/terragrunt.hcl
git commit -m "Increase staging nodes to 2"
git push origin increase-nodes
```

#### **Step 2: Create Pull Request**

Go to GitHub and create a PR from `increase-nodes` → `main`

#### **Step 3: `terraform-plan.yml` Runs Automatically**

GitHub Actions starts:
- ✅ Runs `terragrunt plan` for staging
- ✅ Runs `terragrunt plan` for production
- ✅ Posts plan output as PR comment

**PR Comment shows:**
```
Plan: 0 to add, 1 to change, 0 to destroy

~ google_container_node_pool.primary_nodes
    node_count: 1 → 2
```

#### **Step 4: Review the Plan**

You (and your team) review the plan:
- ✅ **Looks good?** → Approve the PR
- ❌ **Unexpected changes?** → Fix and push again (plan runs again)

#### **Step 5: Merge the PR**

Click "Merge pull request" on GitHub

#### **Step 6: `terraform-apply.yml` Runs Automatically**

GitHub Actions starts:
1. ✅ Applies changes to **staging** first
   - Node count increases from 1 to 2
   - GKE cluster scales up
2. ⏸️ Waits for staging to succeed
3. ✅ Applies changes to **production**
   - Node count increases from 1 to 2
   - GKE cluster scales up

#### **Step 7: Infrastructure Updated!**

Your GKE clusters now have 2 nodes each. ✨

---

## 🔐 GitHub Environments & Protection Rules

### **What are GitHub Environments?**

Environments allow you to add **protection rules** before deploying:

```yaml
environment: production  # References GitHub Environment
```

### **Protection Rules You Can Set:**

1. **Required Reviewers**
   - Require specific people to approve before deployment
   - Example: "Senior DevOps must approve production deploys"

2. **Wait Timer**
   - Add a delay before deployment
   - Example: "Wait 5 minutes before deploying to production"

3. **Deployment Branches**
   - Only allow deployments from specific branches
   - Example: "Only deploy from `main` branch"

### **How to Set Up (Optional):**

1. Go to GitHub repo → **Settings** → **Environments**
2. Create environment: `production`
3. Add protection rules:
   - ✅ Required reviewers: `@your-username`
   - ✅ Wait timer: 5 minutes
4. Save

Now when `terraform-apply.yml` runs:
- Staging deploys automatically
- Production **waits for your approval**
- You get a notification to approve/reject

---

## 🔒 Required GitHub Secrets

Both workflows need these secrets set in your repository:

| Secret Name | Description | Example |
|-------------|-------------|---------|
| `GCP_PROJECT_ID` | Your GCP project ID | `my-project-123` |
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | Workload Identity Provider | `projects/123/locations/global/workloadIdentityPools/github-pool/providers/github` |
| `GCP_SERVICE_ACCOUNT` | Service account email | `github-actions@my-project.iam.gserviceaccount.com` |

### **How to Set Secrets:**

1. Go to GitHub repo → **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Add each secret above

---

## 📊 Workflow Comparison

### **terraform-plan.yml**

| Aspect | Details |
|--------|---------|
| **Trigger** | Pull Request created/updated |
| **Purpose** | Preview changes |
| **Actions** | `terragrunt plan` |
| **Output** | PR comment with plan |
| **Changes Infrastructure?** | ❌ No |
| **When to use** | Every time you change Terraform code |

### **terraform-apply.yml**

| Aspect | Details |
|--------|---------|
| **Trigger** | PR merged to `main` |
| **Purpose** | Apply changes |
| **Actions** | `terragrunt apply -auto-approve` |
| **Output** | Infrastructure created/modified/destroyed |
| **Changes Infrastructure?** | ✅ Yes |
| **When to use** | After reviewing and approving plan |

---

## 🎯 Best Practices

### **1. Always Review Plans Before Merging**

```
❌ BAD:  Merge PR without reading plan
✅ GOOD: Read plan, verify changes, then merge
```

### **2. Never Skip the Plan Step**

```
❌ BAD:  Push directly to main (bypasses plan)
✅ GOOD: Always create PR → review plan → merge
```

### **3. Use Descriptive PR Titles**

```
❌ BAD:  "Update terraform"
✅ GOOD: "Increase staging node count from 1 to 2"
```

### **4. Add Comments to PRs**

Explain **why** you're making infrastructure changes:
```markdown
## Changes
- Increase node count to 2

## Reason
Application needs more resources during peak hours

## Impact
- Cost: +$25/month
- Capacity: +100% compute
```

### **5. Test in Staging First**

```
✅ Apply to staging → verify → then apply to production
```

---

## 🚨 What If Something Goes Wrong?

### **Plan Shows Unexpected Changes**

**Problem:** Plan wants to destroy resources you need

**Solution:**
1. ❌ **Don't merge the PR!**
2. Review your Terraform changes
3. Fix the issue
4. Push again (plan runs automatically)
5. Verify plan looks correct
6. Then merge

### **Apply Fails**

**Problem:** `terraform-apply.yml` fails during deployment

**Solution:**
1. Check GitHub Actions logs
2. Common issues:
   - API quota exceeded
   - Permission denied
   - Resource conflict
3. Fix the issue
4. Create new PR with fix
5. Merge to retry

### **Need to Rollback**

**Problem:** Applied changes broke something

**Solution:**
1. Create PR reverting the changes
2. Review plan (should show reverting to previous state)
3. Merge to apply rollback

---

## 🔍 Debugging Workflows

### **View Workflow Runs:**

1. Go to GitHub repo → **Actions** tab
2. Click on a workflow run
3. Click on a job (e.g., "Plan Staging")
4. Expand steps to see logs

### **Common Issues:**

#### **"Workload Identity Provider not found"**
- Check `GCP_WORKLOAD_IDENTITY_PROVIDER` secret is set correctly
- Verify Workload Identity Federation is configured in GCP

#### **"Permission denied"**
- Check service account has necessary IAM roles
- Verify `GCP_SERVICE_ACCOUNT` secret is correct

#### **"State lock"**
- Another workflow is running
- Wait for it to finish
- Or force unlock: `terragrunt force-unlock LOCK_ID`

---

## 📚 Summary

### **The Golden Rule:**

```
Pull Request → Plan (preview) → Review → Merge → Apply (deploy)
```

### **Key Takeaways:**

1. **`terraform-plan.yml`** = Preview (safe, no changes)
2. **`terraform-apply.yml`** = Deploy (makes actual changes)
3. **Always review plans** before merging
4. **Staging deploys first**, then production
5. **GitHub Environments** add extra protection for production

### **Workflow Diagram:**

```
You make changes
       ↓
Create Pull Request
       ↓
terraform-plan.yml runs (automatic)
       ↓
Shows preview in PR comment
       ↓
You review plan
       ↓
Looks good? → Merge PR
       ↓
terraform-apply.yml runs (automatic)
       ↓
Staging deployed
       ↓
Production deployed (optional: requires approval)
       ↓
Infrastructure updated! ✨
```

---

## 🎓 Learning Resources

- **GitHub Actions Docs**: https://docs.github.com/en/actions
- **Terraform GitHub Actions**: https://github.com/hashicorp/setup-terraform
- **Workload Identity Federation**: https://cloud.google.com/iam/docs/workload-identity-federation

---

**Remember:** These workflows are your **infrastructure safety net**. They prevent accidents, provide visibility, and ensure all changes are reviewed before deployment. Use them! 🛡️
