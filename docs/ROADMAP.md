# Cobryn Roadmap

This roadmap defines the initial development phases for Cobryn.

The goal is to build the project incrementally, starting with a strong backend foundation and evolving into a production-like billing platform.

---

## Phase 1 — Project Foundation

Goal: create the base application structure.

* [ ] Initialize Spring Boot 4 project
* [ ] Configure Java 25
* [ ] Configure Maven
* [ ] Add PostgreSQL with Docker Compose
* [ ] Add Flyway
* [ ] Add global exception handling
* [ ] Add request validation
* [ ] Add basic health endpoint
* [ ] Add initial README documentation

Expected result:

```text
The application runs locally and connects to PostgreSQL.
```

---

## Phase 2 — Authentication and Tenancy

Goal: support authenticated users and organization-based data ownership.

* [ ] Create users table
* [ ] Create organizations table
* [ ] Implement user registration
* [ ] Implement login
* [ ] Generate JWT tokens
* [ ] Protect private endpoints
* [ ] Attach authenticated user to requests
* [ ] Ensure users belong to organizations

Expected result:

```text
Users can register, log in, and access protected endpoints.
```

---

## Phase 3 — Core Billing Resources

Goal: create the main billing entities.

* [ ] Create customers module
* [ ] Create plans module
* [ ] Create subscriptions module
* [ ] Create invoices module
* [ ] Add CRUD operations for customers
* [ ] Add CRUD operations for plans
* [ ] Allow creating subscriptions
* [ ] Generate first invoice when subscription is created

Expected result:

```text
An organization can create customers, plans, subscriptions, and invoices.
```

---

## Phase 4 — Payment Flow

Goal: simulate payment processing.

* [ ] Create payments module
* [ ] Add fake payment provider
* [ ] Allow paying an invoice
* [ ] Mark invoice as paid after successful payment
* [ ] Activate subscription after first successful payment
* [ ] Simulate payment failure
* [ ] Store payment attempts

Expected result:

```text
Invoices can be paid through a fake payment provider.
```

---

## Phase 5 — Idempotency and Webhooks

Goal: make payment operations safer and more realistic.

* [ ] Add Idempotency-Key support
* [ ] Prevent duplicate payment processing
* [ ] Create webhook endpoint
* [ ] Simulate payment provider events
* [ ] Store processed webhook events
* [ ] Prevent duplicate webhook handling

Expected result:

```text
Payment operations and webhook events are safe against duplicated requests.
```

---

## Phase 6 — Scheduled Billing Jobs

Goal: support recurring billing behavior.

* [ ] Generate recurring invoices
* [ ] Detect overdue invoices
* [ ] Move subscriptions to PAST_DUE
* [ ] Cancel subscriptions after grace period
* [ ] Add scheduled jobs
* [ ] Add tests for subscription lifecycle transitions

Expected result:

```text
Cobryn can manage recurring invoice generation and overdue subscriptions.
```

---

## Phase 7 — Async Notifications

Goal: add asynchronous event-driven behavior.

* [ ] Add RabbitMQ to Docker Compose
* [ ] Publish event when invoice is paid
* [ ] Publish event when payment fails
* [ ] Publish event when subscription is canceled
* [ ] Consume events in notification module
* [ ] Simulate email sending

Expected result:

```text
Billing events trigger asynchronous notifications.
```

---

## Phase 8 — Testing and Quality

Goal: make the project reliable and portfolio-ready.

* [ ] Add unit tests for domain rules
* [ ] Add integration tests with Testcontainers
* [ ] Add controller tests
* [ ] Add security tests
* [ ] Add tenant isolation tests
* [ ] Add GitHub Actions pipeline
* [ ] Add test coverage badge if desired

Expected result:

```text
The project has automated tests and CI validation.
```

---

## Phase 9 — Documentation and Polish

Goal: make the project easy to understand and present.

* [ ] Add OpenAPI/Swagger
* [ ] Add API usage examples
* [ ] Add Postman or Insomnia collection
* [ ] Add architecture diagram
* [ ] Add database diagram
* [ ] Add local setup guide
* [ ] Add deployment guide
* [ ] Improve README with screenshots

Expected result:

```text
The repository looks professional and is easy to evaluate.
```
