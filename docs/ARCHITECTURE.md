# Cobryn Architecture

Cobryn is designed as a **modular monolith**.

The system is divided into business modules, each responsible for a specific part of the billing domain. This keeps the codebase simple to run and deploy while still enforcing clear boundaries between features.

---

## Why Modular Monolith?

A billing platform has several domains that could eventually become independent services, such as payments, invoices, subscriptions, and notifications.

However, starting with microservices would add unnecessary complexity in the early stages of the project.

The modular monolith approach allows Cobryn to:

- Keep local development simple
- Avoid premature distributed system complexity
- Maintain clear domain boundaries
- Share a single database initially
- Extract modules into services later if needed

---

## Main Modules

### Auth Module

Responsible for:

- User registration
- User login
- Password hashing
- JWT generation
- Authentication filters

---

### Organizations Module

Responsible for:

- Organization creation
- Organization profile management
- Tenant ownership rules

Each user belongs to an organization, and most business resources are scoped by organization.

---

### Customers Module

Responsible for:

- Customer creation
- Customer updates
- Customer listing
- Customer ownership validation

Customers represent the clients being billed by an organization.

---

### Plans Module

Responsible for:

- Plan creation
- Plan pricing
- Billing interval configuration
- Plan activation/deactivation

Plans define how much a customer should be charged and how often.

---

### Subscriptions Module

Responsible for:

- Creating subscriptions
- Canceling subscriptions
- Managing subscription status
- Connecting customers to plans
- Reacting to invoice and payment events

A subscription represents a recurring billing relationship between a customer and a plan.

---

### Invoices Module

Responsible for:

- Invoice generation
- Invoice due dates
- Invoice status transitions
- Invoice lookup
- Overdue invoice detection

Invoices are generated from subscriptions and paid through the payment flow.

---

### Payments Module

Responsible for:

- Fake payment processing
- Payment attempts
- Payment status
- Idempotency keys
- Payment-provider simulation

The payment module does not integrate with a real provider. It simulates successful and failed payments for learning and testing purposes.

---

### Webhooks Module

Responsible for:

- Receiving fake payment provider events
- Validating webhook payloads
- Preventing duplicate webhook processing
- Triggering invoice and subscription updates

---

### Notifications Module

Responsible for:

- Publishing notification events
- Consuming billing-related events
- Simulating email notifications

This module can later be extracted into a separate service.

---

## Shared Module

The shared module contains infrastructure and cross-cutting concerns:

```text
shared
├── config
├── exceptions
├── security
├── pagination
├── validation
└── events
```

The shared module should not contain business rules.

---

## Dependency Rule

Business modules should avoid unnecessary direct coupling.

Preferred communication styles:

```text
Controller -> Application Service -> Domain Logic -> Repository
```

For cross-module communication, Cobryn may use:

```text
Application service calls
Domain events
Async events through RabbitMQ
```

---

## Database Strategy

Cobryn uses PostgreSQL as the main database.

Database changes are managed through Flyway migrations.

Migration files should follow this pattern:

```text
src/main/resources/db/migration
├── V1__create_users_table.sql
├── V2__create_organizations_table.sql
├── V3__create_customers_table.sql
```

---

## Security Strategy

Cobryn uses JWT-based authentication.

Planned security rules:

- Public access only for authentication endpoints
- Protected access for all business endpoints
- User must belong to an organization
- Users can only access resources from their own organization
- Admin-only actions should be protected by roles

---

## Error Handling Strategy

Cobryn should return consistent API errors.

Example:

```json
{
  "timestamp": "2026-07-14T13:00:00Z",
  "status": 404,
  "error": "Resource not found",
  "message": "Customer not found",
  "path": "/api/customers/123"
}
```

Common errors:

- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`
- `409 Conflict`
- `422 Unprocessable Entity`
- `500 Internal Server Error`

---

## Testing Strategy

Cobryn should include multiple levels of testing.

### Unit Tests

Used for isolated business rules.

Examples:

- Subscription status transitions
- Invoice due date calculations
- Payment status changes
- Idempotency behavior

### Integration Tests

Used for database-backed flows.

Examples:

- Creating a subscription persists the subscription and invoice
- Paying an invoice updates invoice and subscription status
- Tenant isolation prevents cross-organization access

### API Tests

Used for controller behavior.

Examples:

- Invalid request returns validation error
- Protected endpoint requires authentication
- Authenticated user can create a customer

---

## Future Extraction Candidates

If the project grows, these modules could become separate services:

```text
notifications-service
payments-service
billing-service
```

For now, keeping them inside the same application avoids unnecessary operational complexity.
