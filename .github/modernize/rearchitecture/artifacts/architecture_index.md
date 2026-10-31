# Architecture Index

## Assessment scope

This index describes the project as inspected, not the proposed end-state design. The repository has four implemented externally-triggerable application/API surfaces represented here: catalog product REST, user REST, address REST, and the Eureka server runtime. Inventory, orders, and payment do not yet expose project-authored business API entrypoints; their existing scaffolds are described in the root [microservice roadmap](../../../../MICROSERVICE-ROADMAP.md).

**This index is not the full contract. Do not implement from this file alone; follow the artifact paths below.**

## Implementation Guide

### Global artifacts

- `unit_graph.yaml`
  - Who reads: all workers.
  - Use for: existing unit boundaries, triggers, source anchors, dynamic entrypoints, dependencies, and shared references.
  - How to consume: locate the assigned unit; source anchors establish current evidence and are not an implementation task list.
- `wire_contracts.yaml`
  - Who reads: workers changing or implementing the listed HTTP APIs.
  - Use for: existing request/response paths and signatures.
  - How to consume: filter `contracts` by `unit`; preserve observed behavior unless an approved API change supersedes it.
- `shared_modules.yaml`
  - Who reads: user and address API workers.
  - Use for: the User, Address, and UserRepo source used by both units.
  - How to consume: filter by `used_by_units`; use only the listed fields/methods.
- `cross_unit_state.yaml`
  - Who reads: all workers.
  - Use for: implicit state passed across units.
  - How to consume: currently `flows: []`; no such source-level flow was identified.
- `project-structure.md`, `tech-stack.md`, `data-model.md`
  - Who reads: design and implementation workers.
  - Use for: current module boundaries, declared stack, entities and persistence relationships.
  - How to consume: these are source inventories, not target-design decisions.
- `seams.yaml`: not emitted; no explicit/inferred partial rewrite seam was identified for this documentation task.
- `migration_boundary.yaml`: not emitted; no migration/rewrite scope was requested.

### Unit: `catalog-product-api`

- External trigger: HTTP methods under `/api/products`.
- Must read:
  - `units/catalog-product-api/behavior.yaml` — operations, observed side effects and error paths.
  - `units/catalog-product-api/bindings.yaml` — route, security, and config bindings.
  - `units/catalog-product-api/unit_decomposition.yaml` — advisory split candidates only (`commit: false`).
- Relevant global rows:
  - `wire_contracts.yaml` where `unit: catalog-product-api`.
  - `shared_modules.yaml` where `used_by_units` includes `catalog-product-api` (currently none).
- Before DONE report: artifact paths read, preserved behavior, unresolved/deferred contracts, and build/test/runtime evidence.

### Unit: `user-account-api`

- External trigger: HTTP methods under `/api/users`.
- Must read:
  - `units/user-account-api/behavior.yaml` — operations and observed response/error behavior.
  - `units/user-account-api/bindings.yaml` — routes, security, and persistence configuration.
  - `units/user-account-api/unit_decomposition.yaml` — advisory split candidates only (`commit: false`).
- Relevant global rows:
  - `wire_contracts.yaml` where `unit: user-account-api`.
  - `shared_modules.yaml` where `used_by_units` includes `user-account-api` (User, Address, UserRepo).
- Before DONE report: artifact paths read, preserved behavior, unresolved/deferred contracts, and build/test/runtime evidence.

### Unit: `user-address-api`

- External trigger: HTTP methods under `/api/addresses`.
- Must read:
  - `units/user-address-api/behavior.yaml` — operations, transaction boundary, and missing-user/address paths.
  - `units/user-address-api/bindings.yaml` — routes, transaction binding, security, and persistence configuration.
  - `units/user-address-api/unit_decomposition.yaml` — advisory split candidates only (`commit: false`).
- Relevant global rows:
  - `wire_contracts.yaml` where `unit: user-address-api`.
  - `shared_modules.yaml` where `used_by_units` includes `user-address-api` (User, Address, UserRepo).
- Before DONE report: artifact paths read, preserved behavior, unresolved/deferred contracts, and build/test/runtime evidence.

### Unit: `eureka-registry-runtime`

- External trigger: Eureka Server runtime on the configured registry port; endpoints are framework-provided.
- Must read:
  - `units/eureka-registry-runtime/behavior.yaml` — observed server role and runtime boundaries.
  - `units/eureka-registry-runtime/bindings.yaml` — server enablement and registry configuration.
  - `units/eureka-registry-runtime/unit_decomposition.yaml` — advisory split candidates only (`commit: false`).
- Relevant global rows:
  - `wire_contracts.yaml` has no project-authored Eureka endpoint rows.
  - `shared_modules.yaml` has no shared source module for this unit.
- Before DONE report: artifact paths read, preserved server configuration, unresolved/deferred contracts, and build/test/runtime evidence.

## Completion evidence

For any implementation task, report the artifact paths read, observed behavior preserved, any contract changes explicitly approved, unresolved work deferred, and the exact build/test/runtime checks run. The existing context-load tests are not evidence of REST contract or checkout-flow correctness.
