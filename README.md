# Financial RFQ Trading Platform — Microservices with Spring Boot 3

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Protobuf 3.25.5](https://img.shields.io/badge/Protobuf-3.25.5-blue.svg)](https://protobuf.dev/)
[![Build & Tests](https://img.shields.io/badge/Tests-12%2F12%20Passed-success.svg)]()

A hands-on, production-grade microservices learning platform built with **Java 21**, **Spring Boot 3.x**, **Maven**, and **Google Protocol Buffers (Protobuf)**, modeling a real-time capital markets **Request for Quote (RFQ)** and trade execution workflow.

---

## 🏛️ System Architecture

```
                        ┌─────────────────┐
                        │   API Gateway   │ :8080 (Step 6)
                        │ (Spring Cloud)  │
                        └────────┬────────┘
                                 │ JWT Validated (Step 7)
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
   ┌──────▼──────┐      ┌────────▼────────┐    ┌────────▼───────┐
   │ RFQ Service │      │  Trade Service  │    │  Auth Service  │
   │    :8081    │      │     :8082       │    │     :8083      │
   │  [Step 1]   │      │   [Step 10]     │    │   [Step 7]     │
   └──────┬──────┘      └────────┬────────┘    └────────────────┘
          │                      │
   ┌──────▼──────┐      ┌────────▼────────┐
   │   Pricing   │      │  Risk Service   │
   │   Service   │      │     :8085       │
   │    :8084    │      │   [Step 10]     │
   │  [Step 3]   │      └─────────────────┘
   └──────┬──────┘
          │
   ┌──────▼──────────────────────────────┐
   │         Market Data Service         │ :8086 (Step 9)
   └──────────────────┬──────────────────┘
                      │
              ┌───────▼────────┐
              │   AMPS Server  │ (60East AMPS - Step 9)
              │  Real-Time SOW │ - Market data feed
              │   & Pub/Sub    │ - Trade events
              └────────────────┘
```

---

## 🛠️ Technology Stack

| Component | Technology | Version | Purpose |
|---|---|---|---|
| **Language** | Java | 21 (LTS) | Modern Java features, records, pattern matching |
| **Framework** | Spring Boot | 3.3.4 | Core microservice framework & auto-configuration |
| **Build Tool** | Apache Maven | 3.9.x (Wrapper) | Multi-module build management |
| **Serialization** | Google Protobuf | 3.25.5 | High-speed binary wire format + strict schema contracts |
| **Observability** | Spring Boot Actuator | 3.3.4 | `/actuator/health` and `/actuator/info` endpoints |
| **Testing** | JUnit 5, AssertJ, MockMvc | Latest | Full test pyramid (Unit, Controller Slice, Integration) |

---

## 📂 Project Structure

```
sprint_boot_sample/
├── pom.xml                                      # Root parent POM (manages dependencies & plugins)
├── mvnw / mvnw.cmd / .mvn/                      # Maven Wrapper (zero install required)
├── .gitignore                                   # Ignore rules for target, IDE, and OS files
├── README.md                                    # Project documentation and run guide
├── MICROSERVICES_LEARNING_PLAN.md               # 16-step complete learning roadmap
└── rfq-service/                                 # [Step 1] RFQ Microservice Module
    ├── pom.xml                                  # Module POM with Protobuf compiler plugin
    └── src/
        ├── main/
        │   ├── proto/
        │   │   └── rfq.proto                    # Protobuf messages & enums (CreateRfqRequest, RfqResponse)
        │   ├── java/com/trading/rfq/
        │   │   ├── RfqApplication.java          # Spring Boot main application entry point
        │   │   ├── config/
        │   │   │   └── ProtobufConfig.java      # Content negotiation converters (JSON & binary Protobuf)
        │   │   ├── controller/
        │   │   │   └── RfqController.java       # REST endpoints (GET /api/v1/rfq, POST /api/v1/rfq)
        │   │   └── service/
        │   │       └── RfqService.java          # In-memory thread-safe domain logic
        │   └── resources/
        │       └── application.yml              # Port 8081, Actuator health & info configuration
        └── test/java/com/trading/rfq/
            ├── RfqApplicationTests.java         # Full Spring Context load & Actuator health E2E test
            ├── controller/
            │   └── RfqControllerTest.java       # MockMvc Web layer tests (JSON & Protobuf validation)
            └── service/
                └── RfqServiceTest.java          # Unit tests for domain business rules
```

---

## 🚀 Quick Start & Prerequisites

### 1. Prerequisites
- **Java 21**: Make sure Java 21 is active in your terminal.
  ```bash
  export JAVA_HOME=/opt/homebrew/opt/openjdk@21
  ```

---

### 2. Run Automated Test Suite (12/12 Tests)
Run all unit, WebMvc slice, and integration tests:

```bash
./mvnw clean test
```

**Test Breakdown:**
- `RfqServiceTest`: 4 unit tests covering domain rules, state validation, and seed data.
- `RfqControllerTest`: 5 slice tests verifying HTTP 200/201/404 and JSON/Protobuf content negotiation.
- `RfqApplicationTests`: 3 integration tests verifying Spring Boot bootstrap and Actuator health probes.

---

### 3. Run the RFQ Microservice Locally

#### Option A: Maven Dev Server
```bash
./mvnw -pl rfq-service spring-boot:run
```

#### Option B: Build and Run Fat JAR
```bash
# Package executable JAR
./mvnw clean package -DskipTests

# Start the application
java -jar rfq-service/target/rfq-service-1.0.0-SNAPSHOT.jar
```
The service will start on port **`8081`**.

---

## 📡 Live API Verification with `curl`

Once the service is running, test the endpoints in another terminal:

### 1. Health & Actuator Liveness Probe
```bash
curl -s http://localhost:8081/actuator/health | jq .
```
```json
{
  "status": "UP",
  "components": {
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

---

### 2. List RFQs (JSON Format)
```bash
curl -s -H "Accept: application/json" http://localhost:8081/api/v1/rfq | jq .
```
```json
{
  "rfqs": [
    {
      "id": "RFQ-1001",
      "clientId": "GOLDMAN-SACHS",
      "instrumentId": "EUR/USD",
      "side": "BUY",
      "quantity": 5000000.0,
      "currency": "USD",
      "status": "QUOTED",
      "price": 1.0854,
      "createdAt": "1787179602757",
      "expiresAt": "1787179662757"
    }
  ]
}
```

---

### 3. Create a New RFQ (POST JSON)
```bash
curl -s -X POST http://localhost:8081/api/v1/rfq \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "clientId": "JPMORGAN",
    "instrumentId": "USD/JPY",
    "side": "BUY",
    "quantity": 10000000.0,
    "currency": "USD"
  }' | jq .
```
```json
{
  "id": "RFQ-AB2720F6",
  "clientId": "JPMORGAN",
  "instrumentId": "USD/JPY",
  "side": "BUY",
  "quantity": 10000000.0,
  "currency": "USD",
  "status": "PENDING",
  "createdAt": "1787179614897",
  "expiresAt": "1787179644897"
}
```

---

### 4. Fetch High-Speed Binary Protobuf Payload
Request raw binary Protobuf bytes by setting `Accept: application/x-protobuf`:

```bash
curl -s -H "Accept: application/x-protobuf" http://localhost:8081/api/v1/rfq | xxd -p
```
```
0a410a0c5246512d414232373230463612084a504d4f5247414e1a075553...
```

---

## 🗺️ Microservices Learning Roadmap Overview

For the detailed syllabus, design decisions, and concept breakdown, refer to [MICROSERVICES_LEARNING_PLAN.md](file:///Users/Home_i79700/sprint_boot_sample/MICROSERVICES_LEARNING_PLAN.md).

| Step | Name | Status | Key Topics |
|---|---|---|---|
| **Step 1** | **Hello RFQ + Protobuf** | ✅ **Complete** | Spring Boot, REST, Content Negotiation, Protobuf 3 |
| **Step 2** | **JPA + PostgreSQL + Flyway** | ⏳ Next | JPA, Hibernate, Flyway Migrations, Testcontainers |
| **Step 3** | **Pricing Service + OpenFeign** | ⏳ Planned | Declarative HTTP client, WireMock, Problem Details |
| **Step 4** | **Eureka Service Discovery** | ⏳ Planned | Service registry, Client-side load balancing |
| **Step 5** | **Spring Cloud Config Server** | ⏳ Planned | Externalized configuration, `@RefreshScope` |
| **Step 6** | **API Gateway + Pact** | ⏳ Planned | Spring Cloud Gateway, Rate limiting, Contract tests |
| **Step 7** | **Auth Service + JWT** | ⏳ Planned | Spring Security 6, OAuth2 concepts, RBAC |
| **Step 8** | **Resilience4j** | ⏳ Planned | Circuit breaker, Retry, Bulkhead, Fallback |
| **Step 9** | **AMPS Real-Time Messaging** | ⏳ Planned | 60East AMPS, SOW, Pub/Sub, Content filtering |
| **Step 10** | **Trade Saga + CQRS** | ⏳ Planned | Saga choreography, Outbox pattern, CQRS |
| **Step 11** | **gRPC Inter-Service Calls** | ⏳ Planned | Shared Protobuf schemas, gRPC unary & streaming |
| **Step 12** | **Observability** | ⏳ Planned | Distributed tracing (Zipkin), Prometheus, Grafana |
| **Step 13** | **Docker & Compose** | ⏳ Planned | Multi-stage Dockerfiles, Docker Compose |
| **Step 14** | **Kubernetes & Helm** | ⏳ Planned | K8s deployments, HPA, ConfigMaps, Ingress, Helm |
| **Step 15** | **Event Sourcing Audit Trail** | ⏳ Planned | Append-only event store, state replay |
| **Step 16** | **OpenAPI & Versioning** | ⏳ Planned | Swagger 3.1, URL/Header versioning |
