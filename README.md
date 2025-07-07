💸 Kifiya Payment Processing Service – Demo App
By @rogermule

A demo application showcasing a payment processing flow using Java 21, Spring Boot, PostgreSQL, Redis, RabbitMQ, and Docker, following Hexagonal Architecture.

🛠️ Requirements
Java 21
Maven
Docker & Docker Compose
(Optional) VS Code or IntelliJ

Recommended Extensions: Maven, Docker, REST Client, Java extensions

⚙️ Build Instructions
# Clone the repository
git clone https://github.com/rogermule/Demo-KifiyaProcessor.git

# Navigate to the project directory
cd Demo-KifiyaProcessor

# Build the project (skip tests)
mvn clean package -DskipTests

# Check if the build was successful
ls target/paymentprocessing-0.0.1-SNAPSHOT.jar

🚀 Run the Application
docker-compose up --build

🔗 Endpoints
Action	Method	URL
Create payment	POST	http://localhost:51318/api/payments
Get payment	GET	http://localhost:51318/api/payments/{idempotencyKey}
Get payment status	GET	http://localhost:51318/api/payments/status/{idempotencyKey}
Health check	GET	http://localhost:51318/actuator/health
Metrics	GET	http://localhost:51318/actuator/prometheus

🧪 Sample Request Payload (POST /api/payments)
{
  "idempotencyKey": "rog-123",
  "amount": 100.00,
  "currency": "USD",
  "clientReference": "order-456"
}

🐇 Services
RabbitMQ Dashboard: http://localhost:15672

Username: guest
Password: guest

PostgreSQL Access:
psql -h localhost -p 5433 -U kifiya_user -d kifiya-payment

# Password: grace

🛑 Stop the App
docker-compose down
🧱 Design Overview
📐 Architecture
Hexagonal Architecture: Clean separation of business logic and infrastructure.

🧰 Tech Stack
Spring Boot 3.5.3 – REST, JPA, RabbitMQ integration
PostgreSQL – Reliable data persistence
Redis – Fast rate limiting (2 tx/sec)
RabbitMQ – Message-driven event handling

⚖️ Trade-offs
✅ Modular and extensible with Docker/Kubernetes support
❗ Uses a mock payment provider (as per demo constraints)

✅ Challenge Responses
Concurrency & Rate Limiting
→ Redis limits to 2 transactions/second. RabbitMQ queues the overflow.

State Management & Durability
→ PostgreSQL stores the data. RabbitMQ queues are persisted using Docker volumes.

Decoupling & Extensibility
→ PaymentProvider interface keeps providers swappable. Events sent via RabbitMQ.

Reliability & Failure Handling
→ Transactional outbox, retries, and duplicate-checking logic ensure robust processing.


☸️ Kubernetes Support
Kubernetes manifests are included under the /k8s directory.

⚠️ For cloud deployments, change all Service types to LoadBalancer or use Ingess

Directory Structure:

k8s/
├── app/        # Application deployment & service
├── postgres/   # PostgreSQL deployment, service & PVC (requires PV)
├── rabbitmq/   # RabbitMQ deployment, service & PVC (requires PV)
└── redis/      # Redis deployment, service & PVC (requires PV)


⚖️ Final points
- Critical variables like password and other things should be stored in secure place like AWS Secrets, Bitbucket Variables or other places depending on the implementation requirement.
