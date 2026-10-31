🛒 Commerce Platform

A Java-based E-Commerce Microservices Platform built using Spring Boot and Spring Cloud. The project demonstrates how a traditional monolithic commerce application can be decomposed into independently deployable services for user management, product catalog, order processing, and inventory management.

It follows a microservices architecture with centralized API routing, service discovery, JWT-based authentication, and synchronous inter-service communication.

Project Status: Core user, catalog, order, and inventory functionalities are implemented. Payment processing is currently a scaffold, and the checkout workflow is not yet production-ready.
📌 Table of Contents
Overview
Architecture
Microservices
Technology Stack
Key Features
Checkout Workflow
Project Structure
Prerequisites
Database Configuration
Getting Started
API Endpoints
Authentication and Security
Testing
Current Limitations
Future Improvements
Contributing
License
🚀 Overview

Commerce Platform is a distributed e-commerce backend developed using Spring Boot, Spring Cloud, PostgreSQL, and Maven.

The application is divided into independent Spring Boot services, each responsible for a specific business domain.

The primary objective is to demonstrate practical microservices concepts, including:

Independent service development and deployment.
Service discovery using Netflix Eureka.
Centralized API routing using Spring Cloud Gateway.
Secure authentication using JWT.
Synchronous service communication using OpenFeign.
Database ownership and persistence using Spring Data JPA.
Inventory reservation during order processing.
Separation of business responsibilities across services.

Each business service is maintained as an independent Maven application rather than a module in a shared multi-module Maven project.

🏗️ Architecture

The application follows a microservices architecture in which each service handles a specific business responsibility and maintains its own database.

System Architecture
mermaid
flowchart TD
    Client[Client Application]

    Gateway[API Gateway<br/>Port 8080]
    Eureka[Eureka Server<br/>Port 8761]

    subgraph Microservices
        User[User Service<br/>Port 8082]
        Catalog[Catalog Service<br/>Port 8085]
        Orders[Orders Service<br/>Port 8083]
        Inventory[Inventory Service<br/>Port 8084]
        Payment[Payment Service<br/>Scaffold]
    end

    subgraph Databases
        UserDB[(User Database)]
        CatalogDB[(Catalog Database)]
        OrdersDB[(Orders Database)]
        InventoryDB[(Inventory Database)]
    end

    Client --> Gateway

    Gateway -. Service Discovery .-> Eureka

    Gateway --> User
    Gateway --> Catalog
    Gateway --> Orders
    Gateway --> Inventory

    User -. Registers .-> Eureka
    Catalog -. Registers .-> Eureka
    Orders -. Registers .-> Eureka
    Inventory -. Registers .-> Eureka

    User --> UserDB
    Catalog --> CatalogDB
    Orders --> OrdersDB
    Inventory --> InventoryDB

    User -- OpenFeign --> Orders
    Orders -- OpenFeign --> Inventory

Architecture Components
Component	Responsibility
API Gateway	Centralized entry point for client requests
Eureka Server	Service registration and discovery
User Service	Customer accounts, authentication, profiles, and addresses
Catalog Service	Product management and catalog operations
Orders Service	Order creation and retrieval
Inventory Service	Stock management and inventory reservations
Payment Service	Future payment processing integration
Communication Strategy

The application uses two primary communication mechanisms:

Client-to-Service: HTTP requests are routed through Spring Cloud Gateway.
Service-to-Service: OpenFeign clients communicate with other services using Eureka-based service discovery.

Each business service maintains its own persistence boundary using PostgreSQL.

🧩 Microservices

The repository contains seven independently organized Spring Boot applications.

Service	Port	Description	Status
API Gateway	8080	Routes external requests to downstream services	Implemented
Eureka Server	8761	Service registry and discovery	Implemented
User Service	8082	Registration, authentication, profiles, addresses	Implemented
Orders Service	8083	Order creation and retrieval	Implemented
Inventory Service	8084	Stock management and reservations	Implemented
Catalog Service	8085	Product listing and CRUD operations	Implemented
Payment Service	8080*	Payment processing foundation	Scaffold

Payment uses port 8080 by default, which conflicts with the API Gateway. Configure a different port if both applications need to run simultaneously.

1. User Service

Responsible for customer-related operations and authentication.

Responsibilities:

Customer registration.
Customer login.
JWT token generation.
Customer profile management.
Address management.
Customer order operations.
Forwarding order requests to Orders Service using OpenFeign.
2. Catalog Service

Responsible for managing the product catalog.

Responsibilities:

Product creation.
Product listing.
Product retrieval.
Product updates.
Product deletion.
Persistent product storage using PostgreSQL.
3. Orders Service

Responsible for order processing and order management.

Responsibilities:

Creating customer orders.
Retrieving orders.
Retrieving individual orders.
Identifying customers using JWT claims.
Communicating with Inventory Service.
Initiating inventory reservations during order creation.
4. Inventory Service

Responsible for managing product stock and inventory reservations.

Responsibilities:

Stock lookup.
Stock updates.
Inventory reservation.
Reservation retrieval.
Reservation release.
Transactional inventory updates.
5. API Gateway

Provides a centralized entry point for accessing backend services.

Responsibilities:

Routing incoming HTTP requests.
Service discovery through Eureka.
Load-balanced service routing.
Centralized API entry point.
6. Eureka Server

Provides service registration and discovery.

Responsibilities:

Registering service instances.
Maintaining service discovery information.
Supporting load-balanced routing.
Enabling service-to-service discovery.
7. Payment Service

Payment is currently a bootstrap application.

Payment APIs, payment authorization, payment capture, and checkout integration have not yet been implemented.

✨ Key Features
Microservices Architecture: Independent services organized around business capabilities.
JWT Authentication: Secure customer authentication using signed JWT tokens.
Password Security: BCrypt password hashing.
API Gateway: Centralized request routing using Spring Cloud Gateway.
Service Discovery: Dynamic service registration using Netflix Eureka.
Inter-Service Communication: Synchronous HTTP communication using Spring Cloud OpenFeign.
Database Persistence: PostgreSQL integration using Spring Data JPA and Hibernate.
Inventory Management: Stock tracking and reservation management.
Order Management: Customer order creation and retrieval.
Independent Builds: Each service has its own Maven project and Maven Wrapper.
🔄 Checkout Workflow

The current checkout process demonstrates synchronous communication between User, Orders, and Inventory services.

Request Flow
Customer registers using the User Service.
Customer logs in and receives a JWT access token.
Customer submits an order request.
User Service forwards the request to Orders Service using OpenFeign.
The bearer token is forwarded along with the request.
Orders Service extracts the customer identity from the JWT.
Orders Service persists the order.
Orders Service requests an inventory reservation from Inventory Service.
Inventory Service updates stock and stores the reservation transactionally.
Checkout Sequence Diagram
mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant User
    participant Orders
    participant Inventory
    participant DB as PostgreSQL

    Client->>Gateway: POST /api/auth/login
    Gateway->>User: Login Request
    User-->>Client: JWT Token

    Client->>Gateway: Create Order + Bearer Token
    Gateway->>User: Forward Request

    User->>Orders: OpenFeign: Create Order
    Orders->>Orders: Validate JWT
    Orders->>DB: Save Order
    DB-->>Orders: Order Saved

    Orders->>Inventory: Reserve Stock
    Inventory->>DB: Update Stock & Save Reservation
    DB-->>Inventory: Reservation Saved

    Inventory-->>Orders: Reservation Response
    Orders-->>User: Order Response
    User-->>Client: HTTP Response

Important: The current checkout implementation does not verify product prices through Catalog Service and does not integrate Payment Service. Order persistence and inventory reservation are separate operations, not one distributed atomic transaction. Failure recovery, durable saga orchestration, and reconciliation are future improvements.
🛠️ Technology Stack
Category	Technologies
Programming Language	Java 21
Backend Framework	Spring Boot 4.1.1 / 4.1.2
Microservices	Spring Cloud
API Gateway	Spring Cloud Gateway
Service Discovery	Netflix Eureka
Service Communication	Spring Cloud OpenFeign
REST APIs	Spring MVC
Security	Spring Security
Authentication	JWT (HS256)
Password Encryption	BCrypt
ORM	Hibernate
Persistence	Spring Data JPA
Database	PostgreSQL
Build Tool	Maven
Testing	JUnit, Spring Boot Test, Reactor Test
Boilerplate Reduction	Lombok

Spring Cloud Configuration:

Business services and Eureka use Spring Cloud BOM 2025.1.3.
API Gateway uses Spring Cloud BOM 2025.1.4.
📁 Project Structure
text
Commerce-Platform/
│
├── api-gateway/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
│
├── catalog/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
│
├── eureka/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
│
├── inventory/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
│
├── orders/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
│
├── payment/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
│
├── user/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
│
├── AUTHENTICATION.md
├── MICROSERVICE-ROADMAP.md
├── .gitignore
└── README.md


Each service is an independent Maven application with its own dependencies, configuration, and build lifecycle.

⚙️ Prerequisites

Before running the application, make sure the following are installed:

Requirement	Version
Java JDK	21
PostgreSQL	Compatible version
Maven	Optional (Maven Wrapper included)
Git	Latest stable version

Verify Java installation:

bash
java -version


Verify Git installation:

bash
git --version

🗄️ Database Configuration

The application uses four separate PostgreSQL databases.

Create the following databases before starting the services:

sql
CREATE DATABASE ecommerce_user;
CREATE DATABASE ecommerce_product;
CREATE DATABASE ecommerce_orders;
CREATE DATABASE ecommerce_inventory;

Database Mapping
Service	Database
User Service	ecommerce_user
Catalog Service	ecommerce_product
Orders Service	ecommerce_orders
Inventory Service	ecommerce_inventory

Configure the database connection properties in each service's application.properties.

Example:

properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce_user
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}


Use environment variables or an external secrets manager for database credentials.

Security Note: Some current configurations contain locally stored database credentials and a shared development JWT signing key. Replace these values with environment-based configuration before publishing the repository. Rotate any credentials or signing keys that have been exposed outside a disposable local environment.

🚀 Getting Started
1. Clone the Repository
bash
git clone <YOUR_GITHUB_REPOSITORY_URL>


Navigate into the project:

bash
cd Commerce-Platform

2. Configure PostgreSQL

Start PostgreSQL and create the four databases listed in the database configuration section.

Update the database connection properties for each service.

3. Start Eureka Server

Eureka should be started first because the other services register themselves with the registry.

powershell
cd eureka
.\mvnw.cmd spring-boot:run


Eureka Dashboard:

text
http://localhost:8761

4. Start User Service

Open a new terminal:

powershell
cd user
.\mvnw.cmd spring-boot:run

5. Start Catalog Service
powershell
cd catalog
.\mvnw.cmd spring-boot:run

6. Start Inventory Service
powershell
cd inventory
.\mvnw.cmd spring-boot:run

7. Start Orders Service
powershell
cd orders
.\mvnw.cmd spring-boot:run

8. Start API Gateway

Once Eureka and the downstream services are available:

powershell
cd api-gateway
.\mvnw.cmd spring-boot:run


The API Gateway will be available at:

text
http://localhost:8080

Service Startup Order
text
1. PostgreSQL
      ↓
2. Eureka Server
      ↓
3. User Service
4. Catalog Service
5. Inventory Service
6. Orders Service
      ↓
7. API Gateway


Open a separate terminal for each service.

Note: Payment Service is optional and is not required for the current application workflow.

🌐 API Endpoints

The following table provides an overview of the available API routes.

Service	Endpoint	Description
User	POST /api/users	Register a customer
User	POST /api/auth/login	Authenticate a customer
User	/api/addresses/**	Address management
User	/api/users/{userId}/orders/**	Customer order operations
Catalog	/api/products	Product management
Orders	POST /orders	Create an order
Orders	GET /orders	Retrieve orders
Orders	GET /orders/{id}	Retrieve an order by ID
Inventory	/inventory/{productId}	Stock operations
Inventory	/reservations	Inventory reservations
Inventory	/reservations/order/{orderId}	Retrieve reservations by order
API Access

During development, services can be accessed directly using their respective ports.

Alternatively, use the API Gateway:

text
http://localhost:8080

Note: The API Gateway currently does not have a configured route for /api/addresses/**. Address endpoints can be accessed directly through the User Service on port 8082.

Refer to individual service controllers for complete HTTP methods, request payloads, and response structures.

🔐 Authentication and Security

The application uses JWT-based authentication with Spring Security.

Authentication Flow
Customer registers through User Service.
Customer submits login credentials.
User Service verifies the password using BCrypt.
A signed JWT is generated using HS256.
The client includes the token in subsequent protected requests.

Example authorization header:

http
Authorization: Bearer <JWT_TOKEN>


Protected services independently validate incoming JWTs.

Security Components
JWT-based authentication.
HS256 token signing.
Spring Security Resource Server.
BCrypt password hashing.
Bearer token forwarding between services.

For detailed authentication configuration, JWT claims, and service-specific authorization rules, refer to:

AUTHENTICATION.md

🧪 Testing

Each service maintains its own test suite.

To execute tests, navigate to the respective service directory and run:

powershell
.\mvnw.cmd test


Example:

powershell
cd orders
.\mvnw.cmd test


Repeat this process for the other services.

Since the repository contains independent Maven applications, tests and builds must be executed separately for each service.

⚠️ Current Limitations

The project demonstrates core microservices concepts but is not yet production-ready.

Known limitations include:

Payment Service is currently a scaffold.
Checkout does not verify product prices through Catalog Service.
Payment authorization and capture are not integrated.
Order creation and inventory reservation do not participate in a shared distributed transaction.
Durable saga orchestration and compensation workflows are not implemented.
Retry handling and idempotency mechanisms need improvement.
Administrative authorization for product and inventory operations needs further development.
Credentials and JWT signing keys require complete externalization.
Database schema management currently relies on Hibernate rather than a migration framework.
API documentation and centralized observability are not yet implemented.
Address API routes are not currently exposed through the API Gateway.
🛣️ Future Improvements

The following improvements are planned to make the platform more robust and production-oriented.

Integrate Catalog Service into the checkout workflow.
Implement payment APIs and payment gateway integration.
Implement order lifecycle management.
Introduce Saga Pattern for distributed transaction management.
Add compensating transactions for failed inventory reservations.
Implement idempotent request handling and retry mechanisms.
Add role-based access control for administrative operations.
Externalize all credentials and JWT signing keys.
Introduce Flyway or Liquibase for database migrations.
Add Swagger/OpenAPI documentation.
Implement centralized logging and distributed tracing.
Add Docker and Docker Compose for local development.
Improve automated unit and integration test coverage.
Add CI/CD pipelines using GitHub Actions.
🤝 Contributing

Contributions, suggestions, and feedback are welcome.

If you would like to contribute:

Fork the repository.
Create a new feature branch.
Make your changes.
Run the relevant service tests.
Commit and push your changes.
Open a Pull Request.

For major changes, consider opening an issue first to discuss the proposed improvements.

📄 License

This project currently does not specify a license.

If you intend to make it open source, consider adding a LICENSE file to define how others may use, modify, and distribute the project.

👨‍💻 About the Project

Commerce Platform is a backend development project focused on exploring Java, Spring Boot, Spring Cloud, and Microservices Architecture through a practical e-commerce application.

It demonstrates the implementation of independently organized services, service discovery, centralized API routing, JWT authentication, database persistence, and synchronous inter-service communication.

Built with Java 21 and Spring Boot.
