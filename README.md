# Expense Tracker Application

A full-stack personal expense tracking application to help users understand their spending habits.

## Features

- **Expense Entry Form**: Log expenses with amount, category, date, and optional notes
- **Expense List View**: View, edit, and delete expenses
- **Spending Summary**: Visualize spending by category with charts and monthly totals

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
