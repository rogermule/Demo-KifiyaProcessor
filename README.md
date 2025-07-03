Kifiya Payment Processing Service
Demo app 
by @rogermule

How to Build & Run
Requirements:

Java 21
Maven
Docker and Docker Compose
VS Code (optional, with Maven, Docker, REST Client extensions, and other Java extentions as needed) or Intellij


Build:

Clone repo: git clone https://github.com/rogermule/Demo-KifiyaProcessor.git
Go to project: cd Demo-KifiyaProcessor
Build: mvn clean package -DskipTests
Check: target/paymentprocessing-0.0.1-SNAPSHOT.jar exists


Run:

Start: docker-compose up --build
Access endpoints:
Create payment: POST http://localhost:51318/api/payments
Get payment: GET http://localhost:51318/api/payments/{idempotencyKey}
Get status: GET http://localhost:51318/api/payments/status/{idempotencyKey}
Health: GET http://localhost:51318/actuator/health
Metrics: GET http://localhost:51318/actuator/prometheus


Sample create payment request

{
    "idempotencyKey": "rog-123",
    "amount": 100.00,
    "currency": "USD",
    "clientReference": "order-456"
}


Check RabbitMQ: http://localhost:15672 (user: guest, password: guest)
Check PostgreSQL: psql -h localhost -p 5433 -U kifiya_user -d kifiya-payment (password: grace)


Stop:

Run: docker-compose down


Design Rationale

Architecture: Hexagonal for clean separation of logic and infrastructure.
Choices:
Spring Boot 3.5.3: Easy setup for REST, JPA, RabbitMQ.
PostgreSQL: Stores payments and events reliably.
Redis: Fast rate limiting (2 transactions/second).
RabbitMQ: Handles queues and events.


Trade-offs:
Good: Modular, reliable events, Docker/Kubernetes support.
Limited: Uses mock payment provider since this is a demo app - also clearly requested to use mock.



Answers to Challenges

Concurrency and Rate Limiting:
Used Redis for 2 transactions/second limit and RabbitMQ for queuing.


State Management and Durability:
PostgreSQL and RabbitMQ store data reliably with Docker volumes.


Decoupling and Extensibility:
PaymentProvider interface and RabbitMQ events keep things flexible.
Mock provider implemented


Reliability and Failure Handling:
Transactional Outbox, retries, and duplicate checks ensure reliability.


Additional things
Kubernetes Support

Added the relevant kubernetes configuration to deploy the app. Please change the service types to LoadBalancer if you plan to host the kubernetes in the cloud service; Azure/AWS/GCP.
There is a directory in the root folder called k8s. Under it there are 4 directories.
	- app
		- holds configurations for the main app; deployment and service yaml files
	- postgres
		- holds configurations for postgres; deployment, service, and pvc (persitent volume claim). You will need to create persistent volume (pv) when deploying.
	- rabbitmq
		- holds configurations for RabbitMQ; deployment, service, and pvc (persitent volume claim). You will need to create persistent volume (pv) when deploying.
	- redis 
		- holds configurations for postgres; deployment, service, and pvc (persitent volume claim). You will need to create persistent volume (pv) when deploying.