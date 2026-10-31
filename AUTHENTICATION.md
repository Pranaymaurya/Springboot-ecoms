# Authentication and Authorization

This document explains the authentication flow currently implemented in the e-commerce services and evaluates its fit for local development versus production. It reflects the source code as it exists now; recommendations are explicitly identified and are not claims about existing functionality.

## At a glance

The User service authenticates email/password credentials and issues a signed JWT. The User, Catalog, Orders, and Inventory services independently validate that JWT on protected HTTP requests. User forwards the caller's bearer token to Orders, and Orders forwards it to Inventory during reservation calls.

```text
Client --email/password--> User Service
Client <--signed JWT------- User Service

Client --Bearer JWT--> User Service --Bearer JWT via Feign--> Orders
                                                        Orders --Bearer JWT via Feign--> Inventory

Client --Bearer JWT--> Catalog
```

This is token-based authentication, not a central gateway session. Eureka provides service discovery; it does not authenticate a user or authorize a request.

## How a user registers and logs in

### Registration

`POST /api/users` is permitted without an existing token. The User service:

1. Requires a non-blank email and password.
2. Trims and lowercases the email, and rejects an email already in use.
3. Encodes the password using BCrypt before saving the user.
4. Returns the created user. The password property is marked write-only for JSON, so it is accepted on input but not serialized in the normal response.

Registration does not issue a JWT; the client logs in separately.

### Login

Send credentials to `POST /api/auth/login`:

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "customer@example.com",
  "password": "your-password"
}
```

On success, the response contains an `accessToken`, `tokenType` (`Bearer`), `expiresInSeconds` (3,600), and `userId`. Invalid credentials are reported as HTTP 401 with a generic message.

The login implementation looks up the normalized email and checks the stored password with BCrypt. There is also a compatibility path: if BCrypt matching fails but the stored value exactly matches the submitted password, it treats that as a legacy plaintext password, replaces it with a BCrypt hash, and continues login. This supports old records but should be removed after any legacy data has been migrated; plaintext password storage is not a recommended authentication practice.

The User service creates an HS256 JWT with:

| Claim | Meaning |
|---|---|
| `iss` | `ecommerce-user` |
| `sub` | User email |
| `userId` | User's database ID, stored as a string |
| `iat` | Token issue time |
| `exp` | Expiration, one hour after issue |

There is no refresh-token or logout/revocation endpoint. Since services are stateless, a token already issued normally remains usable until its expiration unless the signing key is changed or another revocation mechanism is introduced.

## How a request is authenticated

For a protected endpoint, send the token in the standard Authorization header:

```http
Authorization: Bearer <accessToken>
```

The resource-server security configuration in User, Catalog, Orders, and Inventory validates the JWT signature using HS256 and the configured shared secret. It also applies Spring's default JWT validators and requires issuer `ecommerce-user`; expiration and standard time claims are validated. A missing, invalid, expired, or wrongly signed token is rejected by the security filter before the controller runs.

The applications are configured as stateless: they do not create an HTTP session. CSRF is disabled in these bearer-token services, which is generally appropriate when credentials are sent explicitly in an Authorization header and are not automatically attached by a browser cookie.

## Authorization currently implemented

Authentication answers “is this token valid?” Authorization answers “is this identity allowed to do this operation?” The current authorization rules are limited:

| Service | Current access rule |
|---|---|
| User | Login and user registration are public. Other routes require a valid JWT. User and address controllers compare the requested user ID with the `userId` JWT claim; order queries are filtered by the authenticated user's ID. |
| Orders | All routes require a valid JWT. The authenticated user's `userId` claim is used to assign an order and scope order reads to that user. |
| Inventory | All routes require a valid JWT. There are no role or scope checks distinguishing a customer from an operator/admin for stock updates or reservation operations. |
| Catalog | All routes require a valid JWT. There are no role or scope checks distinguishing product administrators from ordinary customers. |
| Payment | A Spring Security dependency is declared, but there is no JWT resource-server configuration or implemented payment API. Do not assume it accepts and validates the same bearer JWT. Spring Security's default behavior may apply to any future endpoint until a deliberate security policy is added. |
| Eureka | The registry has no user-JWT authentication configured. Treat its management/registry interface as infrastructure and restrict network access. |

As a result, a valid user token is currently sufficient to reach any protected Catalog, Orders, or Inventory endpoint. There are no role, permission, or audience claims and no endpoint-level distinction such as `CUSTOMER` versus `INVENTORY_ADMIN`. This is not equivalent to a fully implemented authorization policy across every service.

## How the token reaches other services

1. The client calls the User order facade, for example `POST /api/users/{userId}/orders`, with its Bearer token.
2. User checks that the path's `userId` matches the token claim.
3. User's OpenFeign `OrderClient` forwards the incoming `Authorization` header to the Orders service discovered as `ORDER` through Eureka.
4. Orders validates the same JWT independently. It obtains `userId` from the authenticated `Jwt` principal instead of trusting a customer ID supplied in the order request.
5. Orders calls Inventory via the `INVENTORY` Feign client and forwards the Authorization header.
6. Inventory validates the JWT independently before handling the stock reservation request.

The same bearer token therefore represents the end user at each hop. It is not a separate service identity. The current source does not use a service-to-service client credential or a token exchange.

Catalog does not currently participate in the checkout call chain: Orders accepts product IDs and prices from the incoming request and does not look up or validate products/prices through Catalog. Payment is also not part of the implemented flow.

## Local use

1. Start Eureka and the User, Orders, Inventory, and Catalog services.
2. Register a user at `POST http://localhost:8082/api/users`.
3. Log in at `POST http://localhost:8082/api/auth/login`.
4. Copy `accessToken` from the login response.
5. For a protected request, set `Authorization` to `Bearer <accessToken>`.
6. To create an order through the User facade, call `POST http://localhost:8082/api/users/{userId}/orders` with the matching `userId`, a Bearer token, and the expected order-items request body.

In local properties, `security.jwt.secret` is configured in the JWT-producing/validating services. All JWT-enabled services must use exactly the same secret for this design to work. Never copy a real secret into documentation, source control, an issue, or a client collection. Use an environment variable or secret manager for local and deployed environments.

## Is this good practice?

### Good foundations already present

- Passwords are normally stored with BCrypt rather than reversible encryption or plaintext.
- Login failures use a generic message, avoiding a direct distinction between an unknown account and a wrong password.
- JWTs have an issuer, issue time, expiration, and a bounded lifetime.
- APIs are stateless and validate tokens at each protected service rather than assuming that service discovery implies trust.
- Orders derives the order owner from the validated token and scopes order reads by user ID.
- User checks ownership for user/address/order-facade paths, and forwards the bearer token rather than dropping authentication at the service boundary.
- The User password field is configured as write-only for JSON serialization.

These are useful foundations for a learning/local microservice project, but they do not by themselves make the system production-ready.

### Important gaps before production

1. **Shared HMAC signing secret:** Every service that can validate an HS256 JWT also has the material needed to mint one. A compromise of any verifier can therefore compromise tokens accepted by all of them. For a larger production system, prefer a dedicated identity provider or signing service using asymmetric keys (for example RS256/ES256), with services receiving only public verification keys, plus planned rotation and key IDs.
2. **Secret handling:** The JWT secret and database credentials are configured in application properties in this repository. Treat any committed development credentials as exposed; replace them, externalize secrets, and use strong unique production secrets. Use HTTPS/TLS for client-to-service and service-to-service traffic.
3. **No fine-grained permissions:** Roles/scopes, token audience validation, and resource-specific policies are not implemented. In particular, a customer JWT can currently authenticate to inventory stock-management endpoints. Add explicit authorization rules and restrict internal/admin endpoints.
4. **Token audience and service identity:** Tokens do not declare a service-specific audience, and downstream calls forward the end user's token unchanged. Decide whether each call should act as the user, as the calling service, or both. If both are needed, use an explicit service identity/token-exchange design and validate audience and scopes.
5. **Legacy plaintext-password fallback:** The login compatibility path compares a stored password directly to the submitted value. Remove this path once legacy records have been migrated to BCrypt and reject non-BCrypt stored credentials.
6. **Brute-force controls:** No rate limiting, progressive delay, account lockout policy, or authentication monitoring is visible in the implementation. Add controls appropriate to the deployment and avoid permanently locking accounts in a way attackers can abuse.
7. **Revocation and refresh:** There is no refresh, logout, or revocation mechanism. Choose short access-token lifetimes and a documented reauthentication policy, or implement a secure refresh/revocation approach if the product needs it.
8. **Payment and Eureka security:** Payment needs an explicit resource-server policy before payment endpoints are added. Protect Eureka with network boundaries and, if exposed beyond a trusted private network, authentication/TLS appropriate to the deployment.
9. **API response boundaries:** The password is write-only, which is good, but APIs return JPA entities. Prefer explicit request/response DTOs and tests that assert sensitive/internal fields are never returned.
10. **Order integrity:** Orders currently trusts prices supplied in the order request. Retrieve authoritative product prices from Catalog (or another trusted pricing boundary) before accepting payment/order totals.

## Summary

The project has a working JWT-based authentication pattern across User, Catalog, Orders, and Inventory, with bearer-token forwarding on the implemented User → Orders → Inventory flow. It does **not** currently provide complete authorization across every microservice: Payment has no JWT resource-server configuration, Eureka is not user-authenticated, and the business services mostly authorize “valid token” rather than specific roles or scopes. The implementation is a reasonable development baseline, but it needs stronger key management, fine-grained authorization, the removal of plaintext-password compatibility, and production operational controls before handling real users or payments.
