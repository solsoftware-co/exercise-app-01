# Cloud Networking Fundamentals 🌐

A comprehensive guide to understanding networking concepts in GCP and Kubernetes.

---

## Table of Contents

1. [Networking Basics](#networking-basics)
2. [IP Addresses & CIDR Notation](#ip-addresses--cidr-notation)
3. [VPC (Virtual Private Cloud)](#vpc-virtual-private-cloud)
4. [Subnets](#subnets)
5. [IP Address Ranges](#ip-address-ranges)
6. [VPC Peering](#vpc-peering)
7. [Private vs Public IPs](#private-vs-public-ips)
8. [GKE Networking](#gke-networking)
9. [Cloud SQL Networking](#cloud-sql-networking)
10. [Common Networking Patterns](#common-networking-patterns)

---

## Networking Basics

### **What is a Network?**

A network is a group of computers that can communicate with each other. Think of it like a neighborhood where houses (computers) can talk to each other.

```
Computer A ←→ Network ←→ Computer B
```

### **IP Address**

An IP address is like a **street address** for a computer on a network.

**Example:**
- Your home: `123 Main Street`
- Computer: `10.0.0.5`

### **Types of Networks:**

| Type | Scope | Example |
|------|-------|---------|
| **LAN** (Local Area Network) | Small area (home, office) | Your home WiFi |
| **WAN** (Wide Area Network) | Large area (cities, countries) | The Internet |
| **VPC** (Virtual Private Cloud) | Cloud-based private network | Your GCP network |

---

## IP Addresses & CIDR Notation

### **IPv4 Address Structure**

An IPv4 address has **4 numbers** (0-255) separated by dots:

```
10.0.0.5
│  │ │ │
│  │ │ └─ Host (0-255)
│  │ └─── Subnet (0-255)
│  └───── Network (0-255)
└──────── Network (0-255)
```

### **CIDR Notation (Classless Inter-Domain Routing)**

CIDR notation defines a **range of IP addresses** using a slash and number:

```
10.0.0.0/24
         │
         └─ Subnet mask (how many IPs)
```

### **Understanding the /Number:**

The number after `/` tells you **how many bits are fixed** (network part) vs **how many bits are variable** (host part).

| CIDR | Fixed Bits | Variable Bits | Total IPs | Usable IPs | Example Range |
|------|------------|---------------|-----------|------------|---------------|
| `/32` | 32 | 0 | 1 | 1 | `10.0.0.5` (single IP) |
| `/24` | 24 | 8 | 256 | 254 | `10.0.0.0` - `10.0.0.255` |
| `/16` | 16 | 16 | 65,536 | 65,534 | `10.0.0.0` - `10.0.255.255` |
| `/8` | 8 | 24 | 16,777,216 | 16,777,214 | `10.0.0.0` - `10.255.255.255` |

### **Visual Example:**

```
10.0.0.0/24 = 256 IP addresses

10.0.0.0   ← First IP (network address)
10.0.0.1   ← Usable
10.0.0.2   ← Usable
...
10.0.0.254 ← Usable
10.0.0.255 ← Last IP (broadcast address)
```

### **Common CIDR Ranges:**

```
/32 = 1 IP       (single host)
/24 = 256 IPs    (small network)
/16 = 65,536 IPs (medium network)
/8  = 16M IPs    (large network)
```

---

## VPC (Virtual Private Cloud)

### **What is a VPC?**

A VPC is your **own private network in the cloud**. It's like having your own neighborhood in a city (GCP).

```
GCP (The City)
├── Your VPC (Your Neighborhood)
│   ├── Subnet 1 (Street 1)
│   ├── Subnet 2 (Street 2)
│   └── Subnet 3 (Street 3)
└── Other VPCs (Other Neighborhoods)
```

### **VPC Characteristics:**

- **Isolated**: Your VPC is separate from other customers' VPCs
- **Private**: Resources inside can't be accessed from the internet (unless you allow it)
- **Customizable**: You define IP ranges, subnets, firewall rules

### **Your VPC in Terraform:**

```hcl
resource "google_compute_network" "vpc" {
  name                    = "expense-tracker-staging-vpc"
  auto_create_subnetworks = false  # We'll create subnets manually
}
```

**What this creates:**
- A private network named `expense-tracker-staging-vpc`
- No automatic subnets (we control everything)

---

## Subnets

### **What is a Subnet?**

A subnet is a **smaller network inside your VPC**. Think of it like streets in your neighborhood.

```
VPC: 10.0.0.0/16 (Your Neighborhood)
├── Subnet 1: 10.0.0.0/24 (Main Street)
├── Subnet 2: 10.0.1.0/24 (Oak Street)
└── Subnet 3: 10.0.2.0/24 (Elm Street)
```

### **Why Use Subnets?**

1. **Organization**: Group related resources
2. **Security**: Isolate different tiers (web, app, database)
3. **Regional**: Subnets are tied to specific regions

### **Your Subnet in Terraform:**

```hcl
resource "google_compute_subnetwork" "subnet" {
  name          = "expense-tracker-staging-subnet"
  ip_cidr_range = "10.0.0.0/24"
  region        = "us-central1"
  network       = google_compute_network.vpc.name
  
  # Secondary ranges for Kubernetes
  secondary_ip_range {
    range_name    = "pods"
    ip_cidr_range = "10.1.0.0/16"
  }
  
  secondary_ip_range {
    range_name    = "services"
    ip_cidr_range = "10.2.0.0/22"
  }
}
```

**What this creates:**
- **Primary range**: `10.0.0.0/24` (256 IPs) for nodes/VMs
- **Secondary range "pods"**: `10.1.0.0/16` (65,536 IPs) for Kubernetes pods
- **Secondary range "services"**: `10.2.0.0/22` (1,024 IPs) for Kubernetes services

---

## IP Address Ranges

### **Primary vs Secondary Ranges**

#### **Primary Range:**
- Used for **VM instances** (GKE nodes, compute instances)
- Example: `10.0.0.0/24`

#### **Secondary Ranges:**
- Used for **Kubernetes pods and services**
- Alias IP ranges
- Don't consume primary range IPs

### **Visual Example:**

```
VPC: expense-tracker-staging-vpc
│
└── Subnet: 10.0.0.0/24
    │
    ├── Primary Range: 10.0.0.0/24
    │   ├── 10.0.0.1 → GKE Node 1
    │   ├── 10.0.0.2 → GKE Node 2
    │   └── 10.0.0.3 → GKE Node 3
    │
    ├── Secondary Range "pods": 10.1.0.0/16
    │   ├── 10.1.0.1 → Pod 1
    │   ├── 10.1.0.2 → Pod 2
    │   └── ... (65,536 IPs available)
    │
    └── Secondary Range "services": 10.2.0.0/22
        ├── 10.2.0.1 → Service 1
        ├── 10.2.0.2 → Service 2
        └── ... (1,024 IPs available)
```

### **Why Separate Ranges?**

1. **Scalability**: Pods can scale without consuming node IPs
2. **Flexibility**: Can have many more pods than nodes
3. **Routing**: Different routing rules for pods vs nodes

---

## VPC Peering

### **What is VPC Peering?**

VPC Peering connects **two separate networks** so they can communicate privately.

```
Your VPC                    Google Services VPC
┌─────────────────┐        ┌──────────────────┐
│                 │        │                  │
│  GKE Cluster    │◄──────►│  Cloud SQL       │
│  10.0.0.0/24    │ Peer   │  10.20.0.0/16    │
│                 │        │                  │
└─────────────────┘        └──────────────────┘
```

### **Why Use VPC Peering?**

- **Private communication**: No traffic goes over the internet
- **Faster**: Direct connection between networks
- **Secure**: No public IPs needed
- **Lower latency**: Stays within Google's network

### **VPC Peering in Your Project:**

```hcl
# Step 1: Reserve IP range for Google services
resource "google_compute_global_address" "private_ip_address" {
  name          = "expense-tracker-staging-private-ip"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  prefix_length = 16                    # /16 = 65,536 IPs
  network       = google_compute_network.vpc.id
}

# Step 2: Create the peering connection
resource "google_service_networking_connection" "private_vpc_connection" {
  network                 = google_compute_network.vpc.id
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_ip_address.name]
}
```

**What this does:**
1. Reserves `10.20.0.0/16` in your VPC for Google services
2. Creates a private tunnel between your VPC and Google's network
3. Allows Cloud SQL to get a private IP in your VPC

---

## Private vs Public IPs

### **Private IP Addresses**

**Definition**: IP addresses that are **only accessible within your network**.

**Private IP Ranges (RFC 1918):**
- `10.0.0.0/8` (10.0.0.0 - 10.255.255.255)
- `172.16.0.0/12` (172.16.0.0 - 172.31.255.255)
- `192.168.0.0/16` (192.168.0.0 - 192.168.255.255)

**Characteristics:**
- ✅ Not routable on the internet
- ✅ Free (no cost)
- ✅ More secure
- ❌ Can't be accessed from outside your network

### **Public IP Addresses**

**Definition**: IP addresses that are **accessible from the internet**.

**Characteristics:**
- ✅ Routable on the internet
- ✅ Can be accessed from anywhere
- ❌ Costs money
- ❌ Security risk (exposed to internet)

### **Comparison:**

| Aspect | Private IP | Public IP |
|--------|------------|-----------|
| **Example** | `10.0.0.5` | `34.123.45.67` |
| **Accessible from internet** | ❌ No | ✅ Yes |
| **Cost** | Free | Paid |
| **Security** | More secure | Less secure |
| **Use case** | Internal services | Public-facing services |

### **Your Project:**

```
Private IPs (Internal):
├── GKE Nodes: 10.0.0.x
├── Pods: 10.1.x.x
├── Services: 10.2.x.x
└── Cloud SQL: 10.20.x.x

Public IPs (External):
└── Load Balancer: 34.123.45.67 (for internet access)
```

---

## GKE Networking

### **GKE Network Architecture**

```
Internet
    ↓
Load Balancer (Public IP: 34.123.45.67)
    ↓
Ingress (Service IP: 10.2.0.10)
    ↓
Service (Service IP: 10.2.0.20)
    ↓
Pods (Pod IPs: 10.1.0.x)
    ↓
Nodes (Node IPs: 10.0.0.x)
```

### **IP Allocation in GKE:**

```hcl
ip_allocation_policy {
  cluster_secondary_range_name  = "pods"      # 10.1.0.0/16
  services_secondary_range_name = "services"  # 10.2.0.0/22
}
```

**What this means:**
- **Pods** get IPs from `10.1.0.0/16` (65,536 IPs)
- **Services** get IPs from `10.2.0.0/22` (1,024 IPs)
- **Nodes** get IPs from primary range `10.0.0.0/24` (256 IPs)

### **Example:**

```yaml
# Kubernetes Pod
apiVersion: v1
kind: Pod
metadata:
  name: backend
spec:
  containers:
  - name: app
    image: backend:latest
```

**When deployed:**
- Pod gets IP: `10.1.0.15` (from pods range)
- Node has IP: `10.0.0.5` (from primary range)
- Service gets IP: `10.2.0.10` (from services range)

---

## Cloud SQL Networking

### **Cloud SQL with Private IP**

**Problem**: Cloud SQL runs in Google's network, not your VPC.

**Solution**: VPC Peering creates a private connection.

### **Architecture:**

```
Your VPC: 10.0.0.0/16
├── Primary Range: 10.0.0.0/24
│   └── GKE Nodes: 10.0.0.x
│
└── Reserved for Google: 10.20.0.0/16
    └── Cloud SQL: 10.20.0.10

VPC Peering Connection
    ↓
Google Services Network
    └── Cloud SQL Instance
```

### **Connection Flow:**

```
1. Your app in GKE pod (10.1.0.15)
   ↓
2. Tries to connect to Cloud SQL (10.20.0.10)
   ↓
3. Traffic goes through VPC peering
   ↓
4. Reaches Cloud SQL in Google's network
   ↓
5. Connection established (private, fast, secure)
```

### **Configuration:**

```hcl
# Cloud SQL with private IP only
resource "google_sql_database_instance" "postgres" {
  settings {
    ip_configuration {
      ipv4_enabled    = false  # No public IP
      private_network = google_compute_network.vpc.id
    }
  }
}
```

**Result:**
- Cloud SQL gets private IP: `10.20.0.10`
- Only accessible from your VPC
- No internet exposure
- Fast, secure connection

---

## Common Networking Patterns

### **Pattern 1: Three-Tier Architecture**

```
Internet
    ↓
Load Balancer (Public IP)
    ↓
Frontend Subnet (10.0.1.0/24)
├── Web Servers
    ↓
Backend Subnet (10.0.2.0/24)
├── Application Servers
    ↓
Database Subnet (10.0.3.0/24)
└── Database (Private IP only)
```

### **Pattern 2: Microservices with Service Mesh**

```
VPC: 10.0.0.0/16
├── GKE Cluster
│   ├── Pods: 10.1.0.0/16
│   │   ├── Service A: 10.1.0.10
│   │   ├── Service B: 10.1.0.20
│   │   └── Service C: 10.1.0.30
│   └── Services: 10.2.0.0/22
│       ├── Service A: 10.2.0.10
│       ├── Service B: 10.2.0.20
│       └── Service C: 10.2.0.30
└── Cloud SQL: 10.20.0.10
```

### **Pattern 3: Hybrid Cloud**

```
On-Premises Network
    ↓
VPN / Cloud Interconnect
    ↓
GCP VPC
├── Subnet 1 (10.0.0.0/24)
└── Subnet 2 (10.0.1.0/24)
```

---

## Networking Best Practices

### **1. Plan Your IP Ranges**

```
✅ GOOD: Non-overlapping ranges
VPC 1: 10.0.0.0/16
VPC 2: 10.1.0.0/16

❌ BAD: Overlapping ranges
VPC 1: 10.0.0.0/16
VPC 2: 10.0.1.0/24  ← Overlaps with VPC 1!
```

### **2. Use Private IPs When Possible**

```
✅ GOOD: Private IP for database
Cloud SQL: 10.20.0.10 (private)

❌ BAD: Public IP for database
Cloud SQL: 34.123.45.67 (public, exposed to internet)
```

### **3. Size Your Ranges Appropriately**

```
Small project:
├── Nodes: /24 (256 IPs)
├── Pods: /20 (4,096 IPs)
└── Services: /24 (256 IPs)

Large project:
├── Nodes: /20 (4,096 IPs)
├── Pods: /14 (262,144 IPs)
└── Services: /20 (4,096 IPs)
```

### **4. Document Your IP Ranges**

Keep a map of your IP allocations:

```
VPC: expense-tracker-staging-vpc (10.0.0.0/16)
├── Primary Subnet: 10.0.0.0/24
│   └── GKE Nodes
├── Pods Range: 10.1.0.0/16
│   └── Kubernetes Pods
├── Services Range: 10.2.0.0/22
│   └── Kubernetes Services
└── Google Services: 10.20.0.0/16
    └── Cloud SQL
```

---

## Troubleshooting Networking Issues

### **Issue 1: Can't Connect to Cloud SQL**

**Symptoms:**
- Connection timeout
- "Connection refused"

**Checklist:**
- ✅ VPC peering created?
- ✅ Cloud SQL has private IP?
- ✅ Firewall rules allow traffic?
- ✅ Using correct IP address?

### **Issue 2: Pods Can't Communicate**

**Symptoms:**
- Pods can't reach each other
- Services not resolving

**Checklist:**
- ✅ Pods in same cluster?
- ✅ Network policies blocking traffic?
- ✅ Service selector correct?
- ✅ DNS working?

### **Issue 3: Out of IP Addresses**

**Symptoms:**
- "No available IP addresses"
- Pods stuck in Pending

**Solution:**
- Increase secondary range size
- Use larger CIDR block

---

## Glossary

| Term | Definition |
|------|------------|
| **CIDR** | Classless Inter-Domain Routing - IP range notation |
| **VPC** | Virtual Private Cloud - Your private network in the cloud |
| **Subnet** | Smaller network within a VPC |
| **Peering** | Connection between two VPCs |
| **Private IP** | IP address only accessible within your network |
| **Public IP** | IP address accessible from the internet |
| **NAT** | Network Address Translation - Allows private IPs to access internet |
| **Firewall** | Rules controlling network traffic |
| **Route** | Path for network traffic |
| **Gateway** | Entry/exit point for network traffic |

---

## Quick Reference

### **Your Project's IP Ranges:**

```
VPC: expense-tracker-staging-vpc
├── Region: us-central1
├── Primary Subnet: 10.0.0.0/24 (256 IPs)
│   └── GKE Nodes
├── Secondary Range "pods": 10.1.0.0/16 (65,536 IPs)
│   └── Kubernetes Pods
├── Secondary Range "services": 10.2.0.0/22 (1,024 IPs)
│   └── Kubernetes Services
└── Google Services: 10.20.0.0/16 (65,536 IPs)
    └── Cloud SQL
```

### **Common Commands:**

```bash
# List VPCs
gcloud compute networks list

# List subnets
gcloud compute networks subnets list

# Describe VPC
gcloud compute networks describe expense-tracker-staging-vpc

# List IP ranges
gcloud compute networks subnets describe expense-tracker-staging-subnet \
  --region=us-central1
```

---

## Further Reading

- [GCP VPC Documentation](https://cloud.google.com/vpc/docs)
- [GKE Networking](https://cloud.google.com/kubernetes-engine/docs/concepts/network-overview)
- [Cloud SQL Private IP](https://cloud.google.com/sql/docs/mysql/private-ip)
- [CIDR Calculator](https://www.ipaddressguide.com/cidr)

---

**Remember**: Networking is like building roads in a city. Plan your routes (IP ranges) carefully, keep private roads private (VPC), and only open gates (public IPs) where necessary! 🏗️
