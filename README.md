# Expense Tracker Application

A full-stack personal expense tracking application to help users understand their spending habits.

## Features

- **Expense Entry Form**: Log expenses with amount, category, date, and optional notes
- **Expense List View**: View, edit, and delete expenses
- **Spending Summary**: Visualize spending by category with charts and monthly totals
- **Budget Tracking**: Set monthly budget limits and track spending progress
  - Set and update monthly budget limit
  - Real-time budget status with visual progress bar
  - Automatic warnings at 80% budget usage
  - Over-budget alerts when spending exceeds limit
  - Remaining budget calculation

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.2
- Spring Data JPA
- PostgreSQL
- Lombok
- Maven

### Frontend
- Next.js 14
- React 18
- TypeScript
- Tailwind CSS
- Shadcn UI Components
- Recharts for visualizations

### DevOps
- Docker & Docker Compose
- Kubernetes (GKE)
- GitHub Actions (CI/CD)
- Terraform (Infrastructure as Code)
- Google Cloud Platform

## Getting Started

### Prerequisites

- Java 17
- Node.js 20+
- Docker & Docker Compose
- Maven (optional, Docker handles this)

### Local Development with Docker Compose

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd exercise-app-02
   ```

2. **Copy environment variables**
   ```bash
   cp .env.example .env
   ```

3. **Start the application (Development mode with hot-reload)**
   ```bash
   docker-compose -f docker-compose.dev.yml up
   ```

   This will start:
   - PostgreSQL database on port 5432
   - Backend API on http://localhost:8080
   - Frontend on http://localhost:3000

4. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api
   - API Health Check: http://localhost:8080/actuator/health

### Sample Data Seeding

The application automatically seeds sample expense data when running in **development mode** (`dev` profile). This happens automatically when you start the application with `docker-compose.dev.yml`.

**What gets seeded:**
- 25 sample expense records
- Mix of current month and previous month expenses
- All expense categories (Groceries, Transportation, Entertainment, Utilities, Other)
- Realistic amounts and descriptions

**Automatic Seeding:**
When you run `docker-compose -f docker-compose.dev.yml up`, the backend automatically:
1. Checks if the database is empty
2. Seeds sample data if no expenses exist
3. Skips seeding if data already exists

**Manual Database Reset:**
To clear the database and reseed with fresh data:

```bash
# Stop containers and remove volumes (this deletes all data)
docker-compose -f docker-compose.dev.yml down -v

# Start fresh with new seed data
docker-compose -f docker-compose.dev.yml up
```

**Note:** Seeding only works in development mode. Production deployments will start with an empty database.

### Local Development without Docker

#### Backend
```bash
cd backend
mvn spring-boot:run
```

#### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Production Build

```bash
docker-compose up --build
```

## API Endpoints

### Expenses
- `GET /api/expenses` - Get all expenses
- `GET /api/expenses/{id}` - Get expense by ID
- `POST /api/expenses` - Create new expense
- `PUT /api/expenses/{id}` - Update expense
- `DELETE /api/expenses/{id}` - Delete expense

### Summary
- `GET /api/expenses/summary/by-category` - Get spending by category
- `GET /api/expenses/summary/monthly` - Get current month total

### Budget
- `GET /api/budget` - Get current budget settings
- `POST /api/budget` - Set or update monthly budget limit
- `GET /api/budget/status` - Get budget status with spending progress

## Project Structure

```
exercise-app-02/
├── backend/                 # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile          # Production Dockerfile
│   ├── Dockerfile.dev      # Development Dockerfile
│   └── pom.xml
├── frontend/               # Next.js frontend
│   ├── src/
│   │   ├── app/
│   │   ├── components/
│   │   └── lib/
│   ├── Dockerfile          # Production Dockerfile
│   ├── Dockerfile.dev      # Development Dockerfile
│   └── package.json
├── k8s/                    # Kubernetes manifests
│   ├── base/
│   └── overlays/
├── terraform/              # Infrastructure as Code
│   ├── environments/
│   └── modules/
├── .github/
│   └── workflows/          # CI/CD pipelines
├── docker-compose.yml      # Production compose
├── docker-compose.dev.yml  # Development compose
└── README.md
```

## Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## Deployment

See [DEPLOYMENT.md](./DEPLOYMENT.md) for detailed deployment instructions.

## Environment Variables

See [.env.example](./.env.example) for all required environment variables.

## License

MIT
