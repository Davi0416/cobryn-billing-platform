# Cobryn

**Cobryn** is a recurring billing platform built with **Java 25** and **Spring Boot 4**.

The goal of the project is to simulate a real-world billing system for SaaS products, including organization management, users, customers, plans, subscriptions, invoice generation, simulated payment processing, idempotency, webhooks, billing jobs, and event-driven asynchronous processing.

> This project is currently under development and aims to demonstrate backend engineering practices using a modern Java stack and architectural decisions inspired by real production systems.

---

## Overview

Cobryn allows organizations to manage their own recurring billing operations.

Each organization has its own business context, including:

* users;
* customers;
* plans;
* subscriptions;
* invoices;
* payments.

An organization can register customers, create plans, start subscriptions, generate invoices, and process payments.

The system is designed as a **modular monolith**, keeping deployment simple while preserving clear boundaries between different application domains.

Critical financial operations are handled synchronously and transactionally, while secondary and independent tasks can be processed asynchronously.

The goal is not to reproduce every complexity of platforms such as Stripe or Paddle, but to implement problems commonly found in real billing systems:

* transactional consistency;
* organization isolation;
* idempotency;
* state management;
* concurrency;
* asynchronous processing;
* reliable event delivery;
* retries;
* traceability;
* integration with external providers.

---

# Features

## MVP scope

* Organization management
* User registration and authentication
* JWT authentication
* Customer management
* Plan management
* Subscription creation and management
* Invoice generation
* Simulated payment processing
* Subscription lifecycle management
* Invoice lifecycle management
* Organization-level resource isolation

## Planned features

* Role-based access control
* Complete organization-level data isolation
* Idempotency keys for critical operations
* Simulated payment provider webhooks
* Idempotent webhook processing
* Scheduled invoice generation
* Overdue payment handling
* Automatic payment recovery
* Grace period for delinquent subscriptions
* Automatic cancellation of delinquent subscriptions
* Asynchronous events with RabbitMQ
* Transactional Outbox
* Idempotent consumers
* Retry with backoff
* Dead-letter queues
* Asynchronous notifications
* Asynchronous outbound webhook delivery
* Distributed billing job processing
* Redis caching
* Integration tests with Testcontainers
* API documentation with OpenAPI
* CI pipeline with GitHub Actions
* Observability and metrics

---

# Stack

## Current stack

* **Java 25**
* **Spring Boot 4**
* **Spring Data JPA**
* **PostgreSQL**
* **Flyway**
* **Docker Compose**
* **JUnit 5**
* **Maven**

## Planned technologies

* **Spring Security**
* **JWT**
* **Testcontainers**
* **RabbitMQ**
* **Redis**
* **OpenAPI / Swagger**
* **GitHub Actions**

---

# Business domain

Cobryn models a simplified recurring billing system for SaaS products.

The `Organization` represents the primary business and data-isolation boundary.

Resources belonging to different organizations must never be combined within the same billing flow.

---

# Main entities

## Organization

Represents a company that uses Cobryn to manage its billing operations.

An organization owns its own:

* users;
* customers;
* plans;
* subscriptions;
* invoices;
* payments.

Each business resource belongs to a single organization.

An organization cannot access, modify, or use resources belonging to another organization.

---

## User

Represents a person with access to an organization.

Every `User` belongs to an `Organization`.

Users may have different permissions depending on the role assigned within the organization.

Future examples:

```text
OWNER
ADMIN
MEMBER
```

---

## Customer

Represents the end customer of an organization.

Every `Customer` belongs to exactly one `Organization`.

A customer is managed by the organization and does not necessarily have direct access to Cobryn.

A customer from one organization cannot be used in subscriptions belonging to another organization.

---

## Plan

Represents a recurring commercial offering created by an organization.

Every `Plan` belongs to exactly one `Organization`.

A plan may define information such as:

* name;
* description;
* amount;
* currency;
* billing interval;
* status.

Plans are isolated by organization.

An organization cannot view, modify, or use plans belonging to another organization.

A plan may only be used in subscriptions involving customers from the same organization.

---

## Subscription

Represents the recurring relationship between a `Customer` and a `Plan`.

Every `Subscription` belongs to an `Organization`.

The customer and plan associated with the subscription must belong to the same organization.

The following relationship must always be true:

```text
subscription.organizationId
    == customer.organizationId
    == plan.organizationId
```

The system must reject any attempt to create a subscription involving resources from different organizations.

The subscription manages its own lifecycle and determines when recurring charges should be generated.

---

## Invoice

Represents a charge generated for a subscription.

Every `Invoice` belongs to the same organization as its related `Subscription`.

An invoice maintains the financial state of a charge and may remain outstanding even after the subscription that generated it has been canceled.

An organization must never access or manipulate invoices belonging to another organization.

---

## Payment

Represents a payment attempt associated with an invoice.

Every `Payment` belongs to the same organization as the related `Invoice`.

A single invoice may have multiple payment attempts.

---

## Webhook Event

Represents an event received from an external provider.

Incoming events must be processed idempotently to avoid duplicate side effects.

When an event is associated with a Cobryn resource, its processing must respect the organization that owns that resource.

---

# Entity relationships

The organization acts as the isolation root of the system.

```text
Organization
    |
    +-- Users
    |
    +-- Customers
    |
    +-- Plans
    |
    +-- Subscriptions
    |       |
    |       +-- Customer
    |       |
    |       +-- Plan
    |
    +-- Invoices
    |       |
    |       +-- Subscription
    |
    +-- Payments
            |
            +-- Invoice
```

A `Subscription` connects a `Customer` and a `Plan`, but only when both belong to the same organization.

From that point onward, the rest of the financial flow remains within the same organizational boundary.

---

# Organization invariants

Some of Cobryn's core invariants are:

```text
user.organizationId == organization.id

customer.organizationId == organization.id

plan.organizationId == organization.id

subscription.organizationId == customer.organizationId

subscription.organizationId == plan.organizationId

invoice.organizationId == subscription.organizationId

payment.organizationId == invoice.organizationId
```

In business terms:

> No business relationship may cross an `Organization` boundary.

The system must prevent situations such as:

```text
Customer from Organization A
        +
Plan from Organization B
        =
Invalid Subscription
```

or:

```text
User from Organization A
        ->
Invoice from Organization B
        =
Access denied
```

---

# Subscription lifecycle

A subscription may move through the following states:

```text
PENDING -> ACTIVE -> PAST_DUE -> CANCELED
```

Additional transitions may also exist:

```text
ACTIVE -> CANCELED

PAST_DUE -> ACTIVE
```

depending on the payment recovery rules adopted by the system.

## Rules

* A subscription is initially created as `PENDING`.
* The customer and plan must belong to the same organization.
* When the first charge is confirmed, the subscription may become `ACTIVE`.
* When a relevant invoice becomes overdue, the subscription may become `PAST_DUE`.
* A later successful payment may recover a `PAST_DUE` subscription.
* After a configured delinquency period, the subscription may be canceled.
* A subscription may also be canceled manually.
* A `CANCELED` subscription cannot be directly reactivated.
* When necessary, a new purchase should create a new subscription.

---

# Invoice lifecycle

An invoice may move through the following states:

```text
OPEN -> PAID

OPEN -> OVERDUE

OPEN -> VOID

OVERDUE -> PAID

OVERDUE -> VOID
```

## Rules

* An invoice is created as `OPEN`.
* A confirmed payment changes its state to `PAID`.
* An unpaid invoice becomes `OVERDUE` after its due date.
* An invoice may be marked as `VOID` when the charge is no longer valid.
* Canceling a subscription does not automatically invalidate previously issued invoices.
* Existing debts may remain outstanding even after the subscription has ended.
* The invoice always remains associated with the organization that originated the charge.

---

# Subscription and billing flow

A simplified flow may work as follows:

```text
1. An organization creates a plan

2. The same organization registers a customer

3. A user from the organization creates a subscription
   linking the customer to the plan

4. Cobryn validates that Customer and Plan
   belong to the same Organization

5. Cobryn generates the first invoice

6. A payment attempt is created

7. The payment is processed by the simulated provider

8. The payment is confirmed

9. The invoice is marked as PAID

10. The subscription becomes ACTIVE

11. Events may be produced for
    secondary processing
```

If the `Customer` and `Plan` belong to different organizations, the flow must stop before the subscription is created.

---

# Synchronous and asynchronous architecture

Cobryn separates operations that are part of the primary financial state from secondary operations that can be processed later.

## Synchronous flows

Operations that are critical to billing consistency must be completed within the primary transaction.

Examples:

* organization creation;
* customer creation;
* plan creation;
* subscription creation;
* ownership validation;
* invoice creation;
* payment creation and confirmation;
* invoice financial state changes;
* critical subscription state changes.

Example:

```text
HTTP Request
     |
     v
Application Service
     |
     +--> validate organization
     +--> validate invoice
     +--> process payment
     +--> persist payment
     +--> mark invoice as PAID
     +--> activate subscription
     |
     v
COMMIT
```

The request must not depend on secondary processing for the primary financial state to become consistent.

---

## Asynchronous flows

Operations that do not need to block the primary transaction can be processed later.

Examples:

* notifications;
* email delivery;
* outbound webhook delivery;
* metrics processing;
* complementary auditing;
* external integrations;
* selected billing jobs;
* batch task processing.

Example:

```text
PaymentSucceeded
        |
        v
RabbitMQ
   /       \
  v         v
Notification   Webhook
Consumer       Consumer
```

A notification failure must not invalidate an already confirmed payment.

---

# Event-driven asynchronous processing

Cobryn intends to use events to decouple secondary flows from primary processing.

Examples of events:

```text
PaymentSucceeded

PaymentFailed

InvoicePaid

InvoiceOverdue

SubscriptionActivated

SubscriptionCanceled
```

These events may trigger different processes without increasing coupling between modules.

Example:

```text
PaymentSucceeded
        |
        +--> Notification
        |
        +--> Outbound Webhook
        |
        +--> Analytics
        |
        +--> Audit
```

Not every event needs to be sent to an external broker.

Internal events may remain within the application when asynchronous or durable processing is unnecessary.

---

# RabbitMQ

RabbitMQ will be used for asynchronous processing between producers and consumers.

Example:

```text
Application
     |
     v
RabbitMQ Exchange
     |
     +--> notification.queue
     |
     +--> webhook.queue
     |
     +--> billing.queue
```

Each consumer is responsible for a specific function.

This approach enables:

* decoupling;
* parallel processing;
* retries;
* consumer scalability;
* failure isolation;
* deferred processing.

---

# Transactional Outbox

Publishing directly to RabbitMQ in the same operation that changes the database may create inconsistencies.

Problematic example:

```text
1. Payment saved
2. Invoice marked as PAID
3. Database COMMIT succeeds
4. RabbitMQ publishing fails
```

The financial state has changed, but the event was never delivered.

To reduce this risk, Cobryn intends to use the **Transactional Outbox Pattern**.

## Flow

Inside the same PostgreSQL transaction:

```text
BEGIN

INSERT Payment

UPDATE Invoice -> PAID

UPDATE Subscription -> ACTIVE

INSERT OutboxEvent -> PaymentSucceeded

COMMIT
```

After the commit:

```text
Outbox Publisher
       |
       v
Unpublished events
       |
       v
RabbitMQ
       |
       v
Consumers
```

The event is only marked as published after its publication succeeds.

This reduces the risk of divergence between persisted state and produced events.

---

# Idempotent consumers

Messaging systems commonly provide **at-least-once delivery**.

This means the same message may be delivered more than once.

Consumers must therefore be prepared to process duplicate messages safely.

Example:

```text
PaymentSucceeded
ID: evt_123
```

If `evt_123` has already been processed:

```text
receive evt_123
        |
        v
already processed?
        |
       YES
        |
        v
ignore safely
```

Duplicate delivery must never produce:

* duplicate critical notifications;
* duplicate payments;
* duplicate invoices;
* duplicate financial state changes;
* duplicate webhook effects for the same logical event.

---

# Retry and Dead-Letter Queue

Temporary failures should not necessarily cause a task to be discarded.

Example:

```text
WebhookDelivery
      |
      v
HTTP request
      |
    ERROR
      |
      v
Retry
```

Cobryn intends to use retry strategies with backoff.

Conceptual example:

```text
1st attempt -> immediately

2nd attempt -> after a few seconds

3rd attempt -> after a few minutes

4th attempt -> after a longer interval
```

After the maximum number of attempts is exceeded, the message may be sent to a **Dead-Letter Queue**.

```text
webhook.queue
     |
     X
     |
 retries exhausted
     |
     v
webhook.dlq
```

The DLQ preserves messages that could not be processed for later inspection or reprocessing.

---

# Asynchronous notifications

Notifications are not part of the financial confirmation of an operation.

Example:

```text
PaymentSucceeded
        |
        v
RabbitMQ
        |
        v
NotificationConsumer
        |
        +--> email
        +--> other channels
```

If notification delivery fails:

```text
Payment = SUCCEEDED

Invoice = PAID

Notification = retry
```

The payment remains confirmed.

---

# Outbound webhooks

In addition to receiving events from a payment provider, Cobryn may eventually allow external systems to receive billing-related events.

Example:

```text
SubscriptionCanceled
        |
        v
Outbox
        |
        v
RabbitMQ
        |
        v
Webhook Consumer
        |
        v
POST https://customer-system.example/webhooks
```

Delivery should support:

* unique event identifier;
* authentication or signing;
* retries;
* backoff;
* idempotency;
* delivery attempt history;
* dead-letter queue.

---

# Billing jobs

Recurring charges need to be initiated automatically.

A scheduler may identify subscriptions that are due for billing.

A simple approach would be:

```text
Scheduler
    |
    v
Find subscriptions due
    |
    v
Process sequentially
```

For larger workloads, the scheduler may only distribute work:

```text
Billing Scheduler
       |
       v
Find subscriptions due
       |
       +--> BillingRequested A
       +--> BillingRequested B
       +--> BillingRequested C
                    |
                    v
                 RabbitMQ
              /      |      \
             v       v       v
         Worker   Worker   Worker
```

Each job should be independent and idempotent.

This allows the number of consumers to increase without changing the main application flow.

---

# Payment recovery

Payment failures may start a recovery flow.

Example:

```text
PaymentFailed
      |
      v
Subscription -> PAST_DUE
      |
      +--> Notification
      |
      +--> Retry policy
```

A future policy could conceptually work like this:

```text
Day 0  -> initial attempt

Day 1  -> retry

Day 3  -> retry

Day 7  -> final retry

After grace period
       -> cancellation
```

The actual values should be defined by business rules and should not be hardcoded into infrastructure concerns.

---

# Recurring billing

After a subscription becomes active, the system may generate new invoices according to its billing interval.

```text
Subscription ACTIVE
        |
        v
Billing Scheduler
        |
        v
Billing Requested
        |
        v
Invoice OPEN
        |
        v
Payment Attempt
        |
   +----+----+
   |         |
SUCCESS    FAILURE
   |         |
   v         v
 PAID     OPEN/OVERDUE
   |         |
   v         v
 ACTIVE   PAST_DUE
```

If the charge remains unpaid beyond the configured grace period, the subscription may be canceled.

All entities involved remain associated with the same organization throughout the entire flow.

---

# Idempotency

Financial operations must not produce duplicate effects.

Cobryn intends to use idempotency keys or equivalent logical identities for critical operations.

Examples:

```text
payments

webhook processing

billing jobs

asynchronous consumers

repeatable financial operations
```

The same logical operation repeated with the same identity should produce the same result without creating duplicate financial effects.

Idempotency must also respect the organization context.

A key used by one organization must not interfere with operations from another organization.

---

# Incoming webhooks

Cobryn will simulate integration with an external payment provider.

Examples:

```text
payment.succeeded

payment.failed

payment.refunded
```

Expected flow:

```text
Payment Provider
       |
       v
Webhook Endpoint
       |
       v
Validate authenticity
       |
       v
Check idempotency
       |
       v
Persist event
       |
       v
Resolve organization/resource
       |
       v
Update billing state
       |
       v
Produce internal event
```

Events that have already been processed must not produce effects again.

---

# API overview

> The endpoints below represent the planned API and may change during development.

## Authentication

```http
POST /api/auth/register
POST /api/auth/login
```

## Organizations

```http
POST /api/organizations
GET /api/organizations/{slug}
PATCH /api/organizations/{slug}
```

## Customers

```http
POST /api/customers
GET /api/customers
GET /api/customers/{id}
PATCH /api/customers/{id}
POST /api/customers/{id}/deactivate
```

All customers returned or modified must belong to the organization of the authenticated user.

Customers with existing financial history should not be physically deleted merely to represent deactivation.

## Plans

```http
POST /api/plans
GET /api/plans
GET /api/plans/{id}
PATCH /api/plans/{id}
POST /api/plans/{id}/deactivate
```

Every plan belongs to the organization responsible for creating it.

Users from one organization cannot query or use plans belonging to another organization.

Plans referenced by existing subscriptions should preserve their history even after they stop accepting new subscriptions.

## Subscriptions

```http
POST /api/subscriptions
GET /api/subscriptions
GET /api/subscriptions/{id}
POST /api/subscriptions/{id}/cancel
```

When creating a subscription, the system must validate that:

```text
Customer.organizationId == Plan.organizationId
```

and that both belong to the organization responsible for the operation.

## Invoices

```http
GET /api/invoices
GET /api/invoices/{id}
POST /api/invoices/{id}/pay
POST /api/invoices/{id}/void
```

## Webhooks

```http
POST /api/webhooks/payment-provider
```

---

# Architecture

Cobryn follows a **modular monolith** architecture.

Each module represents a functional area of the system and encapsulates its own responsibilities.

The application is still deployed as a single unit, but modules maintain explicit boundaries.

This approach avoids the operational complexity of microservices without turning the project into a tightly coupled monolith.

The use of messaging does not change this architectural decision.

RabbitMQ is used as an asynchronous processing mechanism, not as a justification for prematurely splitting the system into microservices.

---

# Architectural overview

```text
                         Cobryn

                   HTTP / REST API
                         |
                         v
                Application Services
                         |
                 synchronous core
                         |
                         v
                    PostgreSQL
        ┌────────────────────────────────┐
        │ Organizations                  │
        │ Customers                      │
        │ Plans                          │
        │ Subscriptions                  │
        │ Invoices                       │
        │ Payments                       │
        │ Outbox Events                  │
        └───────────────┬────────────────┘
                        |
                        v
                 Outbox Publisher
                        |
                        v
                     RabbitMQ
              /           |           \
             v            v            v
      Notifications   Webhooks     Billing Jobs
        Consumer       Consumer       Consumer
```

---

# Project structure

The general structure follows:

```text
src/main/java/com/cobryn

├── auth
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── web
│
├── organization
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── web
│
├── user
├── customer
├── plan
├── subscription
├── invoice
├── payment
├── webhook
├── notification
│
└── shared
    ├── config
    ├── exception
    ├── security
    ├── pagination
    └── event
```

Not every module needs to contain exactly the same folders.

Its internal structure depends on the responsibilities of that domain.

---

# Layer responsibilities

## Domain

Contains primarily:

* entities;
* value objects;
* invariants;
* business rules;
* states;
* domain behavior;
* domain events when appropriate.

Examples:

```text
Subscription.cancel()

Invoice.markAsPaid()

Organization.changeName()
```

The domain must not depend on HTTP, controllers, RabbitMQ, or other external infrastructure details.

---

## Application

Responsible for:

* use cases;
* operation coordination;
* transactional boundaries;
* contextual authorization;
* communication between modules;
* logical event publication;
* application contracts.

Example:

```text
CreateSubscription
        |
        +--> resolve organization
        |
        +--> load customer
        |
        +--> load plan
        |
        +--> validate ownership
        |
        +--> create subscription
        |
        +--> persist
```

---

## Infrastructure

Responsible for technical details such as:

* JPA;
* PostgreSQL;
* repository implementations;
* RabbitMQ;
* Redis;
* Outbox Publisher;
* schedulers;
* external integrations;
* payment providers.

---

## Web

Responsible for the HTTP interface:

* controllers;
* request DTOs;
* response DTOs;
* input validation;
* authentication;
* HTTP error mapping.

Internal entities should not be exposed directly through the API.

---

# Communication between modules

Modules should avoid directly accessing each other's internal details.

Communication may happen through:

```text
Application services

Contracts

Application events

Domain events

Integration events
```

depending on the flow.

Domain events represent facts that occurred within the domain.

RabbitMQ messages are infrastructure representations used for asynchronous transport.

Therefore:

```text
Domain Event != RabbitMQ Message
```

Not every event needs to leave the application.

---

# Concurrency

Cobryn must account for scenarios where multiple operations happen simultaneously.

Examples:

* two attempts to pay the same invoice;
* two webhooks representing the same event;
* two workers trying to bill the same subscription;
* two requests using the same idempotency key;
* two executions trying to update the same financial state.

Depending on the situation, strategies may include:

* database constraints;
* locking;
* optimistic concurrency;
* idempotency;
* transactions;
* idempotent consumers.

The solution should be chosen according to the invariant that must be protected.

---

# Virtual Threads

Because the project uses Java 25, Virtual Threads may be explored for predominantly I/O-bound workloads.

They may be useful in scenarios such as:

* external HTTP calls;
* concurrent integration processing;
* webhook delivery;
* independent I/O tasks.

Virtual Threads and RabbitMQ solve different problems.

```text
Virtual Threads
    ->
execution concurrency

RabbitMQ
    ->
decoupling, durability,
and asynchronous processing
```

Virtual Threads do not replace messaging, Transactional Outbox, or idempotency.

---

# Multi-tenancy and organization isolation

Cobryn uses a **logical multi-tenancy** model.

All organizations use the same application and infrastructure, but their data remains logically isolated.

```text
Organization A

├── Customers
├── Plans
├── Subscriptions
├── Invoices
└── Payments


Organization B

├── Customers
├── Plans
├── Subscriptions
├── Invoices
└── Payments
```

No relationship may cross those boundaries.

Valid:

```text
Customer A + Plan A -> Subscription A

Customer B + Plan B -> Subscription B
```

Invalid:

```text
Customer A + Plan B -> X

Customer B + Plan A -> X
```

---

# Ownership security

Checking whether a resource exists is not enough.

The application must also verify that the resource belongs to the organization responsible for the operation.

Conceptual example:

```text
findPlanById(planId)
```

does not guarantee isolation by itself.

The lookup or validation must also consider the organization:

```text
findPlanByIdAndOrganization(planId, organizationId)
```

or use an equivalent architectural strategy that provides the same guarantee.

The same principle applies to:

* customers;
* plans;
* subscriptions;
* invoices;
* payments.

---

# Local development

## Requirements

* Java 25
* Docker
* Docker Compose

The project uses the Maven Wrapper, so a global Maven installation is not required.

## Starting the infrastructure

```bash
docker compose up -d
```

## Running the application

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## Running the tests

Linux/macOS:

```bash
./mvnw test
```

Windows:

```powershell
.\mvnw.cmd test
```

---

# Environment variables

Example local configuration:

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

Values used only by future features may remain absent until their respective functionality is implemented.

Real secrets must never be committed to the repository.

Public configuration should contain example values only, preferably through:

```text
.env.example
```

---

# Testing strategy

Cobryn aims to combine different levels of testing.

## Unit tests

Primarily responsible for business rules and domain invariants.

Examples:

```text
- a canceled subscription cannot be canceled again

- a paid invoice cannot return to OPEN

- an organization rejects invalid names

- invalid state transitions are rejected
```

---

## Integration tests

Validate interaction between application, persistence, and infrastructure.

Examples:

```text
- organization creation persists correctly

- organization slug must be unique

- transaction is rolled back on failure

- customer cannot be associated with a plan from another organization
```

---

## API tests

Validate:

* HTTP contracts;
* validation;
* status codes;
* serialization;
* authentication;
* authorization;
* error handling.

---

## Isolation tests

Organization isolation must have explicit test coverage.

Examples:

```text
- user from Organization A cannot read Customer from Organization B

- user from Organization A cannot read Plan from Organization B

- user from Organization A cannot cancel Subscription from Organization B

- Customer from Organization A cannot use Plan from Organization B

- Invoice from Organization B cannot be paid by an operation from Organization A
```

---

## Asynchronous processing tests

Asynchronous flows must also have dedicated coverage.

Examples:

```text
- outbox event is stored in the same transaction as the operation

- financial rollback also removes the outbox event

- unpublished event remains available for retry

- duplicated message does not create duplicated effects

- failing consumer performs retries

- message exceeding retry limit is sent to DLQ

- notification failure does not affect payment state

- duplicated worker does not create two invoices for the same billing cycle
```

---

## Testcontainers

Once introduced, Testcontainers will be used to validate behavior that depends on real infrastructure, especially:

* PostgreSQL;
* RabbitMQ;
* Redis.

The goal is to avoid relying exclusively on test doubles or in-memory databases when infrastructure behavior is part of what needs to be validated.

---

# Roadmap

## Phase 1 — Foundation

* [x] Project setup with Java 25 and Spring Boot 4
* [x] PostgreSQL
* [x] Flyway
* [x] Docker Compose
* [x] Initial modular monolith structure
* [x] Initial organization domain
* [ ] User domain
* [ ] JWT authentication

---

## Phase 2 — Billing core

* [ ] Customers
* [ ] Organization-owned plans
* [ ] Subscriptions
* [ ] Customer and Plan ownership validation
* [ ] Invoices
* [ ] Simulated payments
* [ ] Subscription lifecycle
* [ ] Invoice lifecycle

---

## Phase 3 — Financial robustness

* [ ] Idempotency keys
* [ ] Payment failure simulation
* [ ] Payment recovery
* [ ] Webhook processing
* [ ] Webhook idempotency
* [ ] Scheduled billing jobs
* [ ] Concurrency control for critical operations

---

## Phase 4 — Multi-tenancy and security

* [ ] Complete organization-level isolation
* [ ] Ownership validation for all resources
* [ ] Organization isolation tests
* [ ] Role-based access control
* [ ] Security tests
* [ ] Authentication hardening

---

## Phase 5 — Asynchronous processing

* [ ] Application events
* [ ] RabbitMQ
* [ ] Transactional Outbox
* [ ] Outbox Publisher
* [ ] Asynchronous consumers
* [ ] Idempotent consumers
* [ ] Retry with backoff
* [ ] Dead-letter queues
* [ ] Asynchronous notifications
* [ ] Asynchronous webhook delivery
* [ ] Distributed billing jobs
* [ ] Messaging integration tests
* [ ] Evaluate Virtual Threads for I/O-bound workloads

---

## Phase 6 — Infrastructure and observability

* [ ] Redis
* [ ] Testcontainers
* [ ] OpenAPI / Swagger
* [ ] GitHub Actions pipeline
* [ ] Structured logging
* [ ] Metrics
* [ ] Health checks
* [ ] Queue and consumer metrics
* [ ] DLQ observability

---

## Phase 7 — Polish

* [ ] Standardize error responses
* [ ] Improve request validation
* [ ] Add API usage examples
* [ ] Add Postman or Insomnia collection
* [ ] Add architectural documentation
* [ ] Add architecture diagram
* [ ] Add deployment guide

---

# Technical goals

This project was created to practice and demonstrate:

* backend architecture with Java and Spring Boot;
* modular monolith architecture;
* domain modeling;
* REST APIs;
* secure API design;
* relational data modeling;
* transaction management;
* concurrency;
* idempotency;
* asynchronous processing;
* event-driven architecture;
* messaging with RabbitMQ;
* Transactional Outbox;
* idempotent consumers;
* retries and dead-letter queues;
* job processing;
* external system integration;
* multi-tenancy;
* data isolation;
* ownership-based authorization;
* testing with real infrastructure;
* technical documentation;
* production-oriented backend practices.

---

# Scope

Cobryn is an educational and portfolio project.

It is not intended to replace a real payment platform and does not process real financial transactions.

The payment processor used by the project will be simulated in order to reproduce the same kinds of engineering problems found in real financial integrations.

The main focus of the project is on the engineering decisions involved in building a reliable billing system, including:

* consistency;
* isolation;
* idempotency;
* concurrency;
* state evolution;
* reliable event delivery;
* fault tolerance;
* asynchronous processing.

---

# License

This project is licensed under the MIT License.
