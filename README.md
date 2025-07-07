# Kifiya Payment Processing Service – Demo App

A demo application showcasing a payment processing flow using **Java 21**, **Spring Boot**, **PostgreSQL**, **Redis**, **RabbitMQ**, and **Docker**, built with **Hexagonal Architecture** for modularity and scalability.

Developed by [@rogermule](https://github.com/rogermule).

## Requirements

- Java 21
- Maven
- Docker & Docker Compose (optional for local development)
- IDE: VS Code or IntelliJ IDEA

**Recommended Extensions**:
- Maven
- Docker
- REST Client
- Java Extensions

## Build Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/rogermule/Demo-KifiyaProcessor.git
   ```

2. Navigate to the project directory:
   ```bash
   cd Demo-KifiyaProcessor
   ```

3. Build the project (skip tests for faster setup):
   ```bash
   mvn clean package -DskipTests
   ```

4. Verify build output:
   ```bash
   ls target/paymentprocessing-0.0.1-SNAPSHOT.jar
   ```

## Running the Application

Start the application and its dependencies using Docker Compose:

```bash
docker-compose up --build
```

To stop the application:

```bash
docker-compose down
```

## API Endpoints

| Action             | Method | URL                                              |
|--------------------|--------|--------------------------------------------------|
| Create Payment     | POST   | http://localhost:51318/api/payments              |
| Get Payment        | GET    | http://localhost:51318/api/payments/{idempotencyKey} |
| Get Payment Status | GET    | http://localhost:51318/api/payments/status/{idempotencyKey} |
| Health Check       | GET    | http://localhost:51318/actuator/health           |
| Metrics            | GET    | http://localhost:51318/actuator/prometheus       |

### Sample Request Payload (POST /api/payments)

```json
{
  "idempotencyKey": "rog-123",
  "amount": 100.00,
  "currency": "USD",
  "clientReference": "order-456"
}
```

## Services

### RabbitMQ Dashboard
- URL: http://localhost:15672
- Username: guest
- Password: guest

### PostgreSQL Access
- Command: `psql -h localhost -p 5433 -U kifiya_user -d kifiya-payment`
- Password: grace

## Architecture

This application follows **Hexagonal Architecture** (Ports and Adapters) to ensure a clean separation of business logic from infrastructure, promoting modularity and testability.

### Tech Stack
- Spring Boot 3.5.3: REST APIs, JPA, RabbitMQ integration
- PostgreSQL: Reliable data persistence
- Redis: Fast rate limiting (2 transactions/second)
- RabbitMQ: Message-driven event handling
- Docker: Containerized services for easy deployment

### Trade-offs
- **Modular & Extensible**: Supports Docker and Kubernetes for scalability
- **Mock Payment Provider**: Used for demo purposes due to constraints

## Challenge Responses

- **Concurrency & Rate Limiting**: Redis enforces a limit of 2 transactions/second, with RabbitMQ queuing overflow.
- **State Management & Durability**: PostgreSQL ensures persistent storage, and RabbitMQ queues are persisted via Docker volumes.
- **Decoupling & Extensibility**: The `PaymentProvider` interface allows swapping payment providers, with events handled via RabbitMQ.
- **Reliability & Failure Handling**: Implements transactional outbox, retries, and duplicate-checking for robust processing.

## Kubernetes Support

Kubernetes manifests are provided in the `/k8s` directory for deploying the application and its dependencies.

### Directory Structure
```
k8s/
├── app/          # Application deployment & service
├── postgres/     # PostgreSQL deployment, service & PVC (requires PV)
├── rabbitmq/     # RabbitMQ deployment, service & PVC (requires PV)
└── redis/        # Redis deployment, service & PVC (requires PV)
```

**Note**: For cloud deployments, update Service types to `LoadBalancer` or use an Ingress controller.

## Security Recommendations

- Store sensitive variables (e.g., passwords, API keys) in a secure vault like AWS Secrets Manager, Bitbucket Variables, or equivalent, depending on your deployment environment.
- Avoid hardcoding credentials in configuration files or source code.
