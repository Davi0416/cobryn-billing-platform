# Cobryn

**Cobryn** is a modern subscription billing platform built with **Java 25** and **Spring Boot 4**.

The goal of this project is to simulate a real-world SaaS billing system, including customer management, subscription lifecycle, invoice generation, fake payment processing, webhook handling, idempotency, scheduled billing jobs, and asynchronous notifications.

> This project is under development and is intended to demonstrate backend engineering practices using a modern Java stack.

---

## Overview

Cobryn allows organizations to manage recurring billing for their SaaS products.

A company can create plans, register customers, start subscriptions, generate invoices, process payments, and react to payment events through webhooks.

The project is designed as a **modular monolith**, keeping deployment simple while maintaining clear domain boundaries.

---

## Main Features

### Current MVP Scope

* User registration and authentication
* JWT-based login
* Organization management
* Customer management
* Plan management
* Subscription creation
* Invoice generation
* Fake payment processing
* Subscription status management

### Planned Features

* Role-based access control
* Multi-tenant data isolation
* Idempotency keys for payment operations
* Fake payment provider webhooks
* Scheduled invoice generation
* Automatic overdue subscription cancellation
* Asynchronous notifications with RabbitMQ
* Redis caching
* Integration tests with Testcontainers
* API documentation with OpenAPI/Swagger
* CI pipeline with GitHub Actions
* Dockerized local environment

---

## Tech Stack

* **Java 25**
* **Spring Boot 4**
* **Spring Security**
* **Spring Data JPA**
* **PostgreSQL**
* **Flyway**
* **Redis**
* **RabbitMQ**
* **Docker Compose**
* **JUnit 5**
* **Testcontainers**
* **OpenAPI/Swagger**
* **GitHub Actions**

---

## Business Domain

Cobryn models a simplified SaaS billing system.

### Core Entities

* **Organization**: A company using Cobryn to manage billing.
* **User**: A person who belongs to an organization.
* **Customer**: A client of the organization.
* **Plan**: A recurring pricing plan.
* **Subscription**: A customer’s active or inactive plan subscription.
* **Invoice**: A billing document generated for a subscription.
* **Payment**: A payment attempt related to an invoice.
* **Webhook Event**: An external payment provider event received by the system.

---

## Subscription Lifecycle

A subscription can move through the following statuses:

```text
PENDING -> ACTIVE -> PAST_DUE -> CANCELED
```

### Rules

* A subscription starts as `PENDING`.
* When the first invoice is paid, the subscription becomes `ACTIVE`.
* If an invoice becomes overdue, the subscription becomes `PAST_DUE`.
* If the invoice remains unpaid after the grace period, the subscription becomes `CANCELED`.
* A canceled subscription cannot be reactivated directly.

---

## Invoice Lifecycle

An invoice can move through the following statuses:

```text
OPEN -> PAID
OPEN -> OVERDUE
OPEN -> CANCELED
```

### Rules

* An invoice is created as `OPEN`.
* A successful payment changes the invoice status to `PAID`.
* An unpaid invoice past its due date becomes `OVERDUE`.
* A canceled subscription may cancel all open invoices.

---

## Payment Flow

Cobryn uses a fake payment processor to simulate real payment behavior.

Example flow:

```text
1. Organization creates a plan
2. Organization registers a customer
3. Customer starts a subscription
4. Cobryn generates an invoice
5. Payment is processed through the fake payment provider
6. Invoice is marked as PAID
7. Subscription becomes ACTIVE
8. Notification event is published
```

---

## API Overview

### Authentication

```http
POST /api/auth/register
POST /api/auth/login
```

### Organizations

```http
GET /api/organizations/me
PATCH /api/organizations/me
```

### Customers

```http
POST /api/customers
GET /api/customers
GET /api/customers/{id}
PATCH /api/customers/{id}
DELETE /api/customers/{id}
```

### Plans

```http
POST /api/plans
GET /api/plans
GET /api/plans/{id}
PATCH /api/plans/{id}
DELETE /api/plans/{id}
```

### Subscriptions

```http
POST /api/subscriptions
GET /api/subscriptions
GET /api/subscriptions/{id}
POST /api/subscriptions/{id}/cancel
```

### Invoices

```http
GET /api/invoices
GET /api/invoices/{id}
POST /api/invoices/{id}/pay
```

### Webhooks

```http
POST /api/webhooks/payment-provider
```

---

## Project Structure

```text
src/main/java/com/cobryn

├── auth
├── organizations
├── users
├── customers
├── plans
├── subscriptions
├── invoices
├── payments
├── webhooks
├── notifications
└── shared
    ├── config
    ├── exceptions
    ├── security
    ├── pagination
    └── events
```

---

## Architecture

Cobryn follows a **modular monolith architecture**.

Each module owns its domain logic and communicates with other modules through application services and domain events when appropriate.

The main goal is to avoid a distributed system too early while keeping the codebase ready for future extraction into services if needed.

### Initial Architectural Decisions

* Use a modular monolith instead of microservices.
* Keep business rules inside service/domain layers.
* Use DTOs for API input and output.
* Avoid exposing JPA entities directly in controllers.
* Use Flyway for database migrations.
* Use Testcontainers for integration tests.
* Use domain events for billing and notification flows.

---

## Local Development

### Requirements

* Java 25
* Docker
* Docker Compose
* Maven

### Running the infrastructure

```bash
docker compose up -d
```

### Running the application

```bash
./mvnw spring-boot:run
```

### Running tests

```bash
./mvnw test
```

---

## Environment Variables

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/cobryn
DATABASE_USERNAME=cobryn
DATABASE_PASSWORD=cobryn

JWT_SECRET=change-me
JWT_EXPIRATION_MINUTES=60

REDIS_HOST=localhost
REDIS_PORT=6379

RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
```

---

## Testing Strategy

Cobryn aims to include:

* Unit tests for business rules
* Integration tests for persistence and workflows
* Controller tests for API behavior
* Security tests for protected routes
* Testcontainers-based tests for PostgreSQL-dependent features

Example test areas:

```text
- Creating a subscription generates the first invoice
- Paying an invoice activates the subscription
- Duplicate payment requests do not create duplicate payments
- Users cannot access resources from another organization
- Overdue invoices change subscription status
```

---

## Roadmap

### Phase 1 — Core MVP

* [x] Project setup with Java 25 and Spring Boot 4
* [x] PostgreSQL and Flyway setup
* [ ] Authentication with JWT
* [ ] Organization and user model
* [ ] Customer CRUD
* [ ] Plan CRUD
* [ ] Subscription creation
* [ ] Invoice generation
* [ ] Fake payment processing

### Phase 2 — Real Billing Behavior

* [ ] Subscription lifecycle rules
* [ ] Invoice status transitions
* [ ] Payment failure simulation
* [ ] Idempotency keys
* [ ] Webhook processing
* [ ] Scheduled billing jobs

### Phase 3 — Production-like Features

* [ ] Role-based access control
* [ ] Tenant isolation tests
* [ ] RabbitMQ notification events
* [ ] Redis caching
* [ ] OpenAPI documentation
* [ ] Docker Compose environment
* [ ] GitHub Actions pipeline

### Phase 4 — Polish

* [ ] Better error responses
* [ ] Request validation improvements
* [ ] API examples
* [ ] Postman or Insomnia collection
* [ ] Deployment guide
* [ ] Architecture diagram

---

## Goals

This project was created to practice and demonstrate:

* Backend architecture with Java and Spring Boot
* Real-world business rules
* Secure API design
* Database modeling
* Transaction handling
* Asynchronous processing
* Testing with real infrastructure
* Clean project documentation
* Production-oriented backend practices

---

## License

This project is licensed under the MIT License.
