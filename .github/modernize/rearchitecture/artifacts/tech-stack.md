# Current Tech Stack

## Languages and build

- **Language:** Java.
- **Java target property:** 21 in each module's Maven POM.
- **Build:** six independent Maven projects, each with Maven Wrapper scripts and Spring Boot Maven Plugin.
- **Parent:** Spring Boot starter parent version 4.1.1 in all six POMs.
- **Spring Cloud BOM property:** 2025.1.3 in the modules that declare dependency management.
- **Root aggregator:** none found; each service is built from its own directory.

The POMs rely on parent/BOM dependency management for most dependency versions. Direct resolved transitive dependency versions were not inventoried.

## Frameworks and dependencies

| Module | Declared runtime dependencies |
|---|---|
| `catalog` | Spring Data JPA, Spring Security, Spring Web MVC, PostgreSQL driver, optional Lombok. Eureka client dependency is commented out. |
| `user` | Spring Data JPA, Spring Security, Spring Web MVC, PostgreSQL driver, optional Lombok. Eureka client dependency is commented out. |
| `inventory` | Spring Data JPA, Spring Security, Spring Web MVC, Eureka client, PostgreSQL driver, optional Lombok. |
| `orders` | Spring Data JPA, Spring Security, Spring Web MVC, Eureka client, PostgreSQL driver, optional Lombok. |
| `payment` | Spring Data JPA, Spring Security, Spring Web MVC, Eureka client, PostgreSQL driver, optional Lombok. |
| `eureka` | Spring Web MVC and Eureka Server. |

The five business modules declare Spring Boot test starters for JPA, security, and Web MVC. Eureka declares the Web MVC test starter. The repository contains one context-load test per module.

## Runtime configuration

- Eureka Server uses configured port 8761 and has self-registration and registry fetching disabled.
- User uses configured port 8081 and has PostgreSQL/JPA/Spring Security settings in `user/src/main/resources/application.properties`.
- Catalog, inventory, orders, and payment set application names but no explicit server port.
- Only user has datasource properties in the inspected resource files.
- Catalog and user define custom SecurityFilterChain beans that disable CSRF and require authentication for every request using HTTP Basic.
- Inventory, orders, and payment include the Security dependency but no custom security configuration was found in their inspected source.

The user properties file contains credential-valued settings. This inventory intentionally does not reproduce credential values. Move environment-specific credentials outside source control before sharing or deploying the project.

## Constraints visible from source

- The current separate modules do not form a root build; CI/local tooling must select each module or add an explicit aggregator later.
- Default web ports conflict if multiple apps without explicit ports run on one host.
- Only some services currently depend on Eureka Client, and no explicit Eureka server URL is configured for the clients in source.
- JPA is declared in catalog, user, inventory, orders, and payment, but only user has datasource configuration.
- No API gateway, message broker, distributed tracing, resilience library, schema migration tool, or container/deployment configuration was found in the inspected source.
- Entity ID and association annotations in product, inventory, and order models are incomplete; persistence startup and schema generation need to be tested after correcting those mappings.

## Scope note

This is an inventory of the existing declared stack only. It does not select a replacement framework or prescribe a migration. Version claims are limited to explicit POM properties and parent versions; resolved dependency graphs were not produced.
