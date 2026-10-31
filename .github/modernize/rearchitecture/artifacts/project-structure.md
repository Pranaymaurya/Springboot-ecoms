# Project Structure

## Project type

Backend Java monorepository containing six independent Spring Boot/Maven applications. There is no root Maven aggregator, frontend application, or shared build module in the inspected workspace.

## Top-level modules

| Module | Main-source Java files | Functional area | Entry point |
|---|---:|---|---|
| `catalog` | 6 | Product catalog CRUD | `CatalogApplication` |
| `user` | 10 | User and address CRUD | `UserApplication` |
| `inventory` | 3 | Inventory/reservation model scaffold | `InventoryApplication` |
| `orders` | 3 | Order/order-item model scaffold | `OrdersApplication` |
| `payment` | 1 | Payment application scaffold | `PaymentApplication` |
| `eureka` | 1 | Service registry | `EurekaApplication` |

There are six test-source Java files, one per application. Each is a Spring Boot context-load test.

## Layers and functional areas

- **Catalog:** `controller` (product REST API), `service` (product operations), `repo` (JPA repository), `model` (Product entity), `config` (security).
- **User:** `controller` (user and address REST APIs), `service` (user/address operations), `repo` (JPA repositories), `model` (User and Address entities), `config` (security).
- **Inventory:** `model` only; no API, service, or repository source yet.
- **Orders:** `model` only; no API, service, or repository source yet.
- **Payment:** application bootstrap only.
- **Eureka:** application bootstrap with `@EnableEurekaServer`.

There is no source-level cross-service client, event/message handler, API gateway, or shared Java module identified in the inspected main source.

## Current externally reachable project APIs

- Catalog: `/api/products` supports collection and ID-based GET, POST, PUT, DELETE.
- User: `/api/users` supports collection and ID-based GET, POST, PUT, DELETE.
- User: `/api/addresses` supports collection and ID-based GET, POST by user, user-filtered GET, PUT, DELETE.
- Eureka: server runtime enabled on configured port 8761; registry endpoints are provided by Spring Cloud Netflix Eureka rather than project-authored controllers.
- Inventory, orders, and payment: no project-authored REST endpoints found.

## Configuration and build

Each module has its own `pom.xml`, Maven wrapper, `src/main/java`, `src/main/resources/application.properties`, and `src/test/java`. There is no root build descriptor.

Each `application.properties` sets a Spring application name. Eureka sets port 8761 and disables self-registration/registry fetching. User sets port 8081 and contains PostgreSQL/JPA/Spring Security configuration. Catalog, inventory, orders, and payment do not set a port; absent other configuration they use Spring Boot's default port, so they cannot all be started concurrently without distinct ports. Only user has a datasource URL in source configuration.

## Observed runtime flow

The implemented product path is HTTP controller → service → Spring Data repository → configured persistence (catalog currently lacks datasource settings). The implemented user/address path is HTTP controller → service → repositories → the user-configured PostgreSQL database. There are no source-level connections from Orders to Catalog/Inventory/Payment or from any business service to another.

## Verification boundary

This document is based on repository structure and source inspection. The six context-load tests are present but were not run as part of this documentation task. Generated `target` content is excluded from this source inventory.
