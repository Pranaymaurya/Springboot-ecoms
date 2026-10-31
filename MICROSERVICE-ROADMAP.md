# Microservice Roadmap

This roadmap is based on the source currently in the repository. It distinguishes working flows from planned work; it is not a claim that the system is production-ready.

## Current state

| Area | Implemented | Not yet implemented |
|---|---|---|
| API entry and discovery | Spring Cloud Gateway routes and Eureka service discovery | Gateway route for User address endpoints; production gateway hardening |
| Customer | Registration, login, JWT issuance, profile/address APIs, and an order facade | Refresh/revocation, fine-grained roles, stable response DTO contracts |
| Catalog | Product REST CRUD | Product validation and authoritative price lookup during checkout |
| Orders | Create/read customer orders and call Inventory through OpenFeign | Durable orchestration, robust compensation and reconciliation |
| Inventory | Stock APIs and transactional order-linked reservations/releases | Operational/admin authorization and scalable reservation monitoring |
| Payment | Spring Boot application scaffold | Payment API, provider integration, persistence, and checkout integration |
| Data and operations | PostgreSQL, Spring Data JPA, Hibernate schema update | Database migrations, containerized local environment, centralized observability |

## Recommended sequence

### 1. Prepare a safe, repeatable baseline

- Replace local datasource credentials and JWT signing keys with environment-specific configuration.
- Keep secrets out of source control; rotate any key or credential that has been used outside a disposable local environment.
- Add a repeatable local environment for PostgreSQL and the services, and document startup and health checks.
- Add database migrations and verify every service can start against a clean database.

**Done when:** a developer can build and start the services from documented steps using their own local configuration, without repository-embedded credentials.

### 2. Make service contracts reliable

- Define request/response DTOs, validation rules, consistent error responses, and API versioning conventions.
- Protect user data from accidental serialization and establish explicit authorization roles for catalog and inventory administration.
- Decide which routes are public and include all intended external routes in the Gateway.
- Add controller and integration tests for the documented contracts.

**Done when:** API behavior is documented and tested, and customer permissions are distinct from administrative permissions.

### 3. Complete checkout orchestration

- Have Orders validate product identifiers and retrieve trusted prices from Catalog.
- Define order and payment states and the expected behavior for insufficient inventory, rejected payments, and timeouts.
- Integrate Payment only after its API and provider boundary are designed.
- Implement durable, idempotent retries and compensation/reconciliation for inventory and payment outcomes. Use a saga or another explicit distributed workflow; do not rely on a cross-service database transaction.

**Done when:** retries and partial failures produce a consistent, queryable order outcome without duplicate charges or unreleased stock.

### 4. Improve production readiness

- Add structured logs, metrics, tracing, health/readiness checks, and operational dashboards.
- Add container/deployment configuration and CI for builds, tests, and dependency checks.
- Review service-to-service identity, JWT key management/rotation, rate limits, and network boundaries.
- Load-test high-contention inventory reservation and checkout paths.

**Done when:** operators can deploy, observe, secure, and troubleshoot the complete system using repeatable automation.

## Architectural principles

- Each service owns its data and exposes it through an API; do not share another service's database tables.
- Keep inter-service contracts explicit and independently testable.
- Make writes and retries idempotent where requests can be repeated.
- Treat timeouts as potentially uncertain outcomes and reconcile them rather than assuming a remote operation failed.
- Prefer incremental delivery: the Payment module and any future messaging infrastructure should remain clearly identified as planned until implemented and tested.

For the implemented architecture, technology stack, startup instructions, and API overview, see the [README](./README.md). For authentication details and current token behavior, see [AUTHENTICATION.md](./AUTHENTICATION.md).
