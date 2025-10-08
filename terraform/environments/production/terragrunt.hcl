# Production environment configuration
include "root" {
  path = find_in_parent_folders()
}

# Point to the root terraform module
terraform {
  source = "../.."
}

# Environment-specific inputs (minimal for learning/demo)
inputs = {
  environment    = "production"
  cluster_name   = "expense-tracker-production"
  node_count     = 1              # Single node for learning
  min_node_count = 1
  max_node_count = 2              # Max 2 nodes if needed
  machine_type   = "e2-small"     # Smallest machine type (2 vCPU, 2GB RAM)
}
