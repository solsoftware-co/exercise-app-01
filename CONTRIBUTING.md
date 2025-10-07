# Contributing to Expense Tracker

Thank you for your interest in contributing to the Expense Tracker application!

## Development Setup

### Prerequisites

- Java 17
- Node.js 20+
- Docker & Docker Compose
- Maven (optional, Docker handles this)
- Git

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd exercise-app-02
   ```

2. **Copy environment variables**
   ```bash
   cp .env.example .env
   ```

3. **Start development environment**
   ```bash
   docker-compose -f docker-compose.dev.yml up
   ```

   This starts all services with hot-reload enabled:
   - PostgreSQL on port 5432
   - Backend on port 8080
   - Frontend on port 3000

4. **Make changes**
   - Backend: Edit files in `backend/src/` - Spring Boot DevTools will auto-restart
   - Frontend: Edit files in `frontend/src/` - Next.js will hot-reload

## Code Standards

### Backend (Java/Spring Boot)

- Use Lombok to reduce boilerplate
- Follow Spring Boot best practices
- Write unit tests for services
- Use integration tests with Testcontainers
- Follow RESTful API conventions
- Add proper validation and error handling

### Frontend (Next.js/React)

- Use TypeScript for type safety
- Follow React best practices
- Use functional components with hooks
- Use Shadcn UI components
- Write clean, readable code
- Add proper error handling

### Code Style

- **Backend**: Follow Google Java Style Guide
- **Frontend**: Use ESLint configuration provided
- **Commits**: Use conventional commit messages

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

### Integration Tests

```bash
docker-compose -f docker-compose.dev.yml up -d
# Run your integration tests
docker-compose -f docker-compose.dev.yml down
```

## Pull Request Process

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Write clean, well-documented code
   - Add tests for new features
   - Update documentation if needed

3. **Test your changes**
   - Run all tests locally
   - Test in Docker environment
   - Verify no breaking changes

4. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: add new feature"
   ```

   Use conventional commit format:
   - `feat:` - New feature
   - `fix:` - Bug fix
   - `docs:` - Documentation changes
   - `test:` - Test changes
   - `refactor:` - Code refactoring
   - `chore:` - Maintenance tasks

5. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create Pull Request**
   - Provide clear description
   - Reference any related issues
   - Ensure CI passes

## Branch Strategy

- `main` - Production-ready code
- `develop` - Development branch
- `feature/*` - Feature branches
- `bugfix/*` - Bug fix branches
- `hotfix/*` - Urgent production fixes

## Code Review

All submissions require review. We use GitHub pull requests for this purpose.

### Review Checklist

- [ ] Code follows project style guidelines
- [ ] Tests pass and coverage is maintained
- [ ] Documentation is updated
- [ ] No breaking changes (or properly documented)
- [ ] Commit messages follow conventions
- [ ] PR description is clear and complete

## Reporting Issues

### Bug Reports

Include:
- Clear description of the issue
- Steps to reproduce
- Expected vs actual behavior
- Environment details
- Screenshots if applicable

### Feature Requests

Include:
- Clear description of the feature
- Use case and benefits
- Proposed implementation (if any)

## Questions?

Feel free to open an issue for any questions or concerns.

Thank you for contributing!
