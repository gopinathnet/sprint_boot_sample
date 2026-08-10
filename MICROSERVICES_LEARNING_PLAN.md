# Microservices with Spring Boot — Financial RFQ Trade Service
### *Complete Learning Roadmap*

> **Stack**: Java 21 · Maven · Spring Boot 3.x · PostgreSQL · AMPS · Protobuf · Docker · Kubernetes  
> **Domain**: Financial RFQ (Request for Quote) Trading Platform  
> **Rule**: Concepts first → Minimal code → Real tests → Git commit → Next step

---

## 🏦 The Domain: What We Are Building

**RFQ = Request for Quote** — the standard workflow in financial markets where:

```
CLIENT          RFQ-SERVICE       PRICING-SERVICE    MARKET-DATA-SERVICE
  │                  │                  │                     │
  │── POST /rfq ───► │                  │                     │
  │                  │── price request ►│                     │
  │                  │                  │◄── AMPS feed ───────│
  │                  │◄─ quoted price ──│                     │
  │◄─ quote ─────────│                  │                     │
  │                  │                  │                     │
  │── POST /deal ───►│                  │                     │
  │                  │── publish DEAL event to AMPS ──────────►
  │◄─ confirmation ──│
```

### Final System Architecture

```
                        ┌─────────────────┐
                        │   API Gateway   │ :8080
                        │ (Spring Cloud)  │
                        └────────┬────────┘
                                 │ JWT validated
          ┌──────────────────────┼──────────────────────┐
          │                      │                       │
   ┌──────▼──────┐      ┌────────▼────────┐    ┌───────▼──────┐
   │ RFQ Service │      │  Trade Service  │    │  Auth Service │
   │   :8081     │      │    :8082        │    │    :8083      │
   └──────┬──────┘      └────────┬────────┘    └──────────────┘
          │                      │
   ┌──────▼──────┐      ┌────────▼────────┐
   │  Pricing    │      │   Risk Service  │
   │  Service    │      │    :8085        │
   │   :8084     │      └─────────────────┘
   └──────┬──────┘
          │ OpenFeign
   ┌──────▼──────────────────────────────┐
   │         Market Data Service          │
   │              :8086                   │
   └──────────────────┬───────────────────┘
                      │
              ┌───────▼────────┐
              │   AMPS Server  │  (60East AMPS)
              │  Real-time     │  - Market data feed
              │  Messaging     │  - Trade events
              └───────┬────────┘
                      │
              ┌───────▼────────┐
              │ Notification   │
              │   Service      │
              │    :8087       │
              └────────────────┘

Support Infrastructure:
  ┌─────────────┐  ┌──────────────┐  ┌──────────────┐
  │   Eureka    │  │Config Server │  │  PostgreSQL  │
  │   :8761     │  │    :8888     │  │  (per svc)   │
  └─────────────┘  └──────────────┘  └──────────────┘

  ┌─────────────┐  ┌──────────────┐  ┌──────────────┐
  │   Zipkin    │  │  Prometheus  │  │   Grafana    │
  │   :9411     │  │    :9090     │  │    :3000     │
  └─────────────┘  └──────────────┘  └──────────────┘
```

---

## Tools & Versions

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 21 (LTS) | Language |
| Spring Boot | 3.3.x | Framework |
| Spring Cloud | 2023.0.x | Microservice patterns |
| Maven | 3.9.x | Build tool |
| PostgreSQL | 16 | Relational database |
| AMPS | 5.3.x | Financial real-time messaging |
| Protobuf | 3.x | Binary API serialization |
| Docker | 26.x | Containerization |
| Kubernetes | 1.30.x | Orchestration (minikube) |
| Zipkin | 3.x | Distributed tracing |
| Prometheus + Grafana | latest | Metrics & dashboards |

---

## Testing Philosophy (applied from Step 1 onwards)

```
        ▲
        │  E2E Tests (Step 12)
        │  Contract Tests - Pact (Step 6)
        │  Integration Tests - @SpringBootTest (from Step 2)
        │  Unit Tests - JUnit 5 + Mockito (from Step 1)
        ▼
```

Every step includes:
- **Unit tests** — pure logic, no Spring context, fast
- **Integration tests** — real Spring context, real DB (Testcontainers)
- **Contract tests** — ensures API contracts between services (Pact)
- **E2E tests** — full flow across all services (Step 12+)

---

---

# PHASE 1 — Foundations
> *"Understand Spring Boot before microservices"*

---

## Step 1 — Hello RFQ: Single Microservice with REST + Protobuf

**Concepts you will learn:**
- What is a microservice vs monolith? (trade-offs, not religion)
- Spring Boot auto-configuration explained
- The Spring application context (beans, DI, IoC)
- `@SpringBootApplication`, `@RestController`, `@GetMapping`, `@RequestMapping`
- `application.yml` structure and property binding
- Maven project structure for a Spring Boot service
- **Protobuf**: what is binary serialization? Why use it in finance? (performance, schema contracts)
- How to configure a Spring Boot app to return Protobuf OR JSON based on `Accept` header
- Spring Actuator: `/actuator/health`, `/actuator/info` — why every service needs this
- `SpringApplication.run` lifecycle

**What you'll build:**
```
rfq-service/
  ├── GET  /api/v1/rfq        → returns list of RFQs (Protobuf or JSON)
  ├── GET  /api/v1/rfq/{id}   → returns single RFQ
  ├── GET  /actuator/health   → liveness check
  └── .proto schema: rfq.proto
```

**RFQ Domain Object:**
```
RFQ {
  id, clientId, instrumentId, side (BUY/SELL),
  quantity, currency, status (PENDING/QUOTED/DEALT/EXPIRED),
  createdAt, expiresAt
}
```

**Tests:**
- Unit: `RfqServiceTest` — pure business logic
- Unit: `RfqControllerTest` — MockMvc, no real HTTP
- Integration: `RfqControllerIntegrationTest` — real server, Protobuf + JSON negotiation

**Git commit:** `feat(step-1): bootstrap rfq-service with REST and Protobuf serialization`

**Installation needed:** Java 21, Maven, IDE (IntelliJ recommended)

---

## Step 2 — Persistence: Spring Data JPA + PostgreSQL + Flyway

**Concepts you will learn:**
- JPA (Jakarta Persistence API) vs raw JDBC — when each is appropriate
- Hibernate ORM: how it maps Java objects to SQL tables
- `@Entity`, `@Id`, `@Column`, `@GeneratedValue`, `@Enumerated`
- Repository pattern: `JpaRepository<T, ID>` — what queries are generated
- Writing custom JPQL and native SQL queries
- `@Transactional` — what it means, why it matters in trading systems
- **Flyway**: database schema versioning — critical for production systems
  - V1__create_rfq_table.sql, V2__add_trade_table.sql etc.
- Connection pooling with **HikariCP** (default in Spring Boot)
- **Spring Profiles**: `application-dev.yml` (H2) → `application-prod.yml` (PostgreSQL)
- **Testcontainers**: spin up a real PostgreSQL in tests — no mocks for the DB layer
- Optimistic locking with `@Version` — critical for concurrent trade updates
- Audit fields: `@CreatedDate`, `@LastModifiedDate` with `@EnableJpaAuditing`

**What you'll build:**
```
rfq-service/ (extended)
  ├── POST /api/v1/rfq          → create new RFQ (persisted)
  ├── GET  /api/v1/rfq          → list with pagination + filtering by status
  ├── GET  /api/v1/rfq/{id}     → single RFQ
  ├── PUT  /api/v1/rfq/{id}     → update status
  └── Flyway migrations: V1..V3
```

**Tests:**
- Unit: `RfqRepositoryTest` — `@DataJpaTest` with Testcontainers PostgreSQL
- Integration: full stack with real DB, real Flyway migrations
- Tests for optimistic locking conflict scenario

**Git commit:** `feat(step-2): add JPA persistence, PostgreSQL, Flyway migrations`

**Installation needed:** PostgreSQL 16 (with install script provided)

---

## Step 3 — Second Service + HTTP Communication (OpenFeign)

**Concepts you will learn:**
- Why split into multiple services? Conway's Law, team boundaries
- Synchronous vs Asynchronous inter-service communication (overview)
- `RestTemplate` (legacy, blocking) — know it, don't use it for new code
- `WebClient` (reactive, non-blocking) — when to use
- **OpenFeign** (declarative HTTP client) — the recommended approach
  - `@FeignClient`, `@GetMapping`, error decoders
  - Why it feels like writing an interface, not a client
- Hardcoded URLs problem → solved in Step 4
- **Global exception handling**: `@RestControllerAdvice`, `@ExceptionHandler`
- Problem Details RFC 9457 (Spring Boot 3 built-in error format)
- API versioning strategies: URL path, header, content-type

**What you'll build:**
```
pricing-service/ (NEW)
  ├── GET /api/v1/price/{instrumentId}  → returns current indicative price
  └── POST /api/v1/price/calculate      → calculates price for RFQ

rfq-service/ (extended)
  └── When creating RFQ → calls pricing-service to get indicative price
```

**Tests:**
- Unit: `PricingServiceTest` — pricing logic
- Unit: `RfqServiceTest` — mock the Feign client
- Integration: WireMock to simulate pricing-service being up/down
- Test: What happens when pricing-service returns 404? 500?

**Git commit:** `feat(step-3): add pricing-service and OpenFeign integration from rfq-service`

---

---

# PHASE 2 — Service Discovery & Configuration
> *"Remove hardcoded URLs and centralise config"*

---

## Step 4 — Service Registry (Netflix Eureka)

**Concepts you will learn:**
- The Service Discovery pattern — why it exists
- What problem does hardcoded `localhost:8084` cause in production?
- Client-side vs Server-side discovery (trade-offs)
- Eureka server internals: registry, heartbeat, self-preservation mode
- `@EnableEurekaServer`, `@EnableDiscoveryClient`
- Load balancing with **Spring Cloud LoadBalancer** (replaces Ribbon)
- Multiple instances of same service — round robin, random, custom
- Health check integration with Actuator
- Eureka web dashboard

**What you'll build:**
```
discovery-server/ (NEW) :8761
  └── Eureka dashboard with all services registered

All existing services → register to Eureka
OpenFeign → uses service name "PRICING-SERVICE" not localhost:8084
```

**Tests:**
- Integration: start Eureka + two services, verify registration
- Test: deregister a service, verify Feign client handles it

**Git commit:** `feat(step-4): add eureka discovery server, migrate from hardcoded URLs`

---

## Step 5 — Centralised Configuration (Spring Cloud Config Server)

**Concepts you will learn:**
- The 12-Factor App methodology (especially Factor III: Config)
- Why configuration in code is a problem (security, deployment)
- Spring Cloud Config Server: how it serves config from Git
- Config Client: bootstrap phase, `spring.config.import`
- Configuration hierarchy: defaults → per-service → per-profile
- `@RefreshScope` — refresh beans without restart
- `/actuator/refresh` and Spring Cloud Bus for broadcast refresh
- Encrypting sensitive config (passwords, API keys)
- Config for multiple environments: dev, staging, prod

**What you'll build:**
```
config-server/ (NEW) :8888
  └── Reads from local Git repo (or GitHub)

config-repo/ (Git repository)
  ├── application.yml         (shared defaults)
  ├── rfq-service.yml         (service-specific)
  ├── pricing-service.yml
  └── rfq-service-prod.yml    (prod overrides)
```

**Tests:**
- Integration: start config server, verify service gets correct properties
- Test: change config, call `/actuator/refresh`, verify bean reloads

**Git commit:** `feat(step-5): add config server, externalise all service configuration`

---

---

# PHASE 3 — API Gateway & Security
> *"One front door, secure by default"*

---

## Step 6 — API Gateway (Spring Cloud Gateway) + Contract Testing

**Concepts you will learn:**
- Why an API Gateway? (single entry, cross-cutting concerns)
- Spring Cloud Gateway internals: `Route`, `Predicate`, `Filter`
- Route predicates: path, header, method, weight (for canary)
- Global filters: logging, tracing, rate limiting
- Path rewriting and request/response transformation
- **Rate limiting** with Redis
- CORS configuration at the gateway
- **Consumer-Driven Contract Testing with Pact**
  - Why: prevents integration bugs without full E2E tests
  - Pact: consumer writes expectations, provider verifies them
  - `@PactTestFor`, `@Pact` annotations

**What you'll build:**
```
api-gateway/ (NEW) :8080
  /api/rfq/**     → rfq-service (with load balancing)
  /api/price/**   → pricing-service
  /api/trades/**  → trade-service
  (rate limiting: 100 req/min per client)
```

**Tests:**
- Contract: Pact tests between rfq-service (consumer) and pricing-service (provider)
- Integration: gateway routing tests

**Git commit:** `feat(step-6): add api-gateway with routing, rate limiting, Pact contract tests`

---

## Step 7 — Authentication & Authorisation (JWT + Spring Security)

**Concepts you will learn:**
- Session-based vs Token-based authentication
- **JWT** anatomy: Header (alg) + Payload (claims) + Signature
- Standard claims: `sub`, `iat`, `exp`, `iss`, custom: `roles`, `clientId`
- **OAuth2 / OpenID Connect** concepts: authorization server, resource server
- Spring Security filter chain: how requests flow
- `SecurityFilterChain` bean configuration (Spring Boot 3 style — no `WebSecurityConfigurerAdapter`)
- Method-level security: `@PreAuthorize("hasRole('TRADER')")`
- Where to validate tokens: Gateway (for routing) + Services (for authorization)
- **Role-based access control** for trading:
  - `ROLE_CLIENT` — can submit RFQ
  - `ROLE_TRADER` — can quote, deal
  - `ROLE_RISK` — can view risk limits
  - `ROLE_ADMIN` — full access
- Refresh tokens, token expiry
- Storing passwords: BCrypt

**What you'll build:**
```
auth-service/ (NEW) :8083
  POST /auth/register  → create user
  POST /auth/login     → returns JWT + refresh token
  POST /auth/refresh   → new access token

api-gateway
  └── JWT validation filter (before routing)

rfq-service, trade-service
  └── @PreAuthorize on endpoints
```

**Tests:**
- Unit: JWT generation and validation
- Unit: Spring Security config (MockMvc with authentication)
- Integration: full login → get token → call protected endpoint
- Test: expired token, wrong role, tampered token

**Git commit:** `feat(step-7): add auth-service with JWT, secure all service endpoints`

---

---

# PHASE 4 — Resilience & Fault Tolerance
> *"Design for failure — it will happen"*

---

## Step 8 — Circuit Breaker, Retry, Bulkhead (Resilience4j)

**Concepts you will learn:**
- **Cascading failures** — why one slow service can take down the whole system
- **Circuit Breaker pattern** (Closed → Open → Half-Open state machine)
  - `slidingWindowType`, `failureRateThreshold`, `waitDurationInOpenState`
- **Retry pattern** — when to retry, exponential backoff, jitter
- **Timeout** — why unbounded waits are deadly in trading
- **Bulkhead pattern** — isolate failures, limit concurrent calls per service
- **Fallback** — graceful degradation (return last known price vs error)
- **Resilience4j** vs Hystrix (Hystrix is dead, Resilience4j is the modern standard)
- Resilience4j + Spring Boot Actuator metrics integration

**What you'll build:**
```
rfq-service → pricing-service call wrapped with:
  ├── Circuit Breaker (open if >50% failure in 10 calls)
  ├── Retry (3 attempts, exponential backoff)
  ├── Timeout (500ms max — critical in trading)
  └── Fallback (return last cached price from Redis)

trade-service → risk-service call wrapped with:
  └── Bulkhead (max 10 concurrent calls to risk)
```

**Tests:**
- Unit: test circuit breaker state transitions
- Integration: use WireMock to simulate failures, verify circuit opens
- Test: fallback returns cached price when pricing-service is down
- Test: bulkhead rejects excess concurrent requests

**Git commit:** `feat(step-8): add resilience4j circuit breaker, retry, bulkhead, timeout`

---

---

# PHASE 5 — Real-Time Messaging with AMPS
> *"The heartbeat of a trading platform"*

---

## Step 9 — AMPS Integration: Real-Time Market Data

**Concepts you will learn:**
- **What is AMPS?** (60East Advanced Message Processing System)
  - The messaging system of choice in capital markets
  - Used at major banks, hedge funds, trading venues
- AMPS key concepts:
  - **Topics**: named message streams (e.g., `market.data.FX.EURUSD`)
  - **Publish/Subscribe**: producers push, consumers pull
  - **SOW (State of the World)**: AMPS unique feature — instant snapshot of current state
    - e.g., "give me the latest price for all FX pairs" → instant, no replay needed
  - **Delta messages**: AMPS sends only what changed (bandwidth efficient)
  - **Content filtering**: subscribe with SQL-like filters
    - `SELECT * FROM market.data WHERE ccy_pair = 'EURUSD'`
  - **Command types**: `subscribe`, `sow`, `sow_and_subscribe`, `publish`, `unsubscribe`
  - **Bookmark subscriptions**: resume from where you left off (like Kafka offsets)
- AMPS vs Kafka: when to use which
  - AMPS: ultra-low latency, SOW, content filtering (financial use case)
  - Kafka: high throughput, event log, replay (analytics use case)
- AMPS Java client library
- **Installing AMPS**: local setup, demo license

**What you'll build:**
```
market-data-service/ (NEW) :8086
  ├── Publishes simulated FX prices to AMPS topic:
  │     market.data.FX.{ccy_pair}  → {bid: 1.0852, ask: 1.0854, timestamp}
  ├── GET /api/v1/market-data/{pair}        → subscribe SOW from AMPS
  └── GET /api/v1/market-data/stream/{pair} → SSE stream (Server-Sent Events)

pricing-service (extended)
  └── Subscribes to AMPS market data feed
      Uses real-time price + spread to calculate RFQ quote price
```

**Real-Time Topics Architecture:**
```
AMPS Topics:
  market.data.FX.*          ← market-data-service publishes
  rfq.events                ← rfq-service publishes (RFQ_CREATED, RFQ_EXPIRED)
  trade.events              ← trade-service publishes (DEAL_DONE, DEAL_FAILED)
  risk.breach.alerts        ← risk-service publishes
```

**Tests:**
- Unit: market data price simulator logic
- Integration: embedded AMPS (or Docker AMPS) — publish and receive message
- Test: SOW query returns current state for all subscribed pairs
- Test: delta subscription only receives changed fields
- Test: content filter — subscribe only to EURUSD, verify GBPUSD not received

**Git commit:** `feat(step-9): add market-data-service with AMPS publish/subscribe and SOW`

**Installation needed:** AMPS server (install steps provided)

---

## Step 10 — Trade Lifecycle + Saga Pattern (Choreography via AMPS)

**Concepts you will learn:**
- **Distributed transactions problem** — why 2-Phase Commit (2PC) fails at scale
- **BASE vs ACID** in distributed systems
- **Eventual consistency** — what it means in practice for trading
- **Saga pattern**: long-running transactions as a sequence of local transactions
  - **Choreography**: services react to events (decentralised)
  - **Orchestration**: central coordinator tells services what to do
- Compensating transactions — the "undo" step
- **Idempotency** — why every service must handle duplicate events safely
- Outbox pattern — atomically write DB + publish event (no dual-write problem)
- **CQRS (Command Query Responsibility Segregation)**:
  - Commands change state → write model
  - Queries read state → read model (optimised for reads)
  - Why this matters in a trading system with high read:write ratio

**What you'll build:**
```
Full Trade Lifecycle via AMPS Choreography:

1. rfq-service     → publishes RFQ_CREATED to AMPS
2. pricing-service ← subscribes, calculates price → publishes PRICE_QUOTED
3. rfq-service     ← subscribes, updates RFQ status → publishes RFQ_QUOTED
4. client          → accepts quote (POST /rfq/{id}/deal)
5. rfq-service     → publishes RFQ_DEALT
6. risk-service    ← subscribes, runs risk checks → publishes RISK_APPROVED / RISK_REJECTED
7. trade-service   ← subscribes, creates trade record if approved
8. notification-service ← subscribes, sends trade confirmation

Compensation flow:
  RISK_REJECTED → rfq-service reverses RFQ to FAILED state
```

**trade-service/ (NEW) :8082**
```
POST /api/v1/trades         → create trade (from RFQ deal)
GET  /api/v1/trades         → list trades (CQRS read model)
GET  /api/v1/trades/{id}    → single trade
GET  /api/v1/trades/stream  → SSE real-time trade blotter
```

**Tests:**
- Unit: saga state machine logic
- Unit: idempotency — same event ID twice → no duplicate trade
- Integration: full saga flow with embedded AMPS
- Test: compensation — risk rejected → verify RFQ rolled back
- Test: outbox — DB transaction rollback also discards AMPS publish

**Git commit:** `feat(step-10): add trade-service, saga choreography, CQRS, outbox pattern`

---

---

# PHASE 6 — Protobuf & gRPC
> *"Performance-first APIs for inter-service communication"*

---

## Step 11 — Protobuf Schemas + gRPC Inter-Service Calls

**Concepts you will learn:**
- **Protobuf (Protocol Buffers)** deep dive:
  - `.proto` schema files: `message`, `field types`, `enum`, `repeated`, `oneof`
  - Why binary serialisation? (vs JSON: 3-10x smaller, faster encoding/decoding)
  - Field numbers — backward compatibility rules
  - `protoc` compiler + Maven plugin
  - How Spring Boot serves both Protobuf and JSON from same endpoint
- **gRPC**:
  - What is gRPC? HTTP/2 + Protobuf + code generation
  - Service definitions in `.proto`: `rpc`, `stream`
  - Unary vs Server-streaming vs Client-streaming vs Bidirectional streaming
  - `grpc-spring-boot-starter`
  - When to use gRPC vs REST in microservices
  - gRPC for internal service calls (rfq → pricing, trade → risk)
- Protobuf versioning and backward compatibility

**What you'll build:**
```
shared-proto/ (Maven module)
  ├── rfq.proto         → RFQ messages
  ├── trade.proto       → Trade messages
  ├── market_data.proto → Price tick messages
  └── risk.proto        → Risk check request/response

rfq-service → pricing-service: gRPC call (PriceCalculationService)
trade-service → risk-service: gRPC call (RiskCheckService)

REST endpoints: still serve JSON (for external clients)
gRPC: for internal service-to-service calls
```

**Tests:**
- Unit: Protobuf serialisation/deserialisation round-trip
- Unit: gRPC service tests with `@GrpcTest` and `InProcessServer`
- Integration: real gRPC call across two services
- Test: backward compatibility — old client, new proto schema

**Git commit:** `feat(step-11): add shared protobuf schemas, gRPC for internal service calls`

---

---

# PHASE 7 — Observability
> *"You cannot manage what you cannot measure"*

---

## Step 12 — Distributed Tracing, Metrics & Structured Logging

**Concepts you will learn:**
- **The Three Pillars of Observability**: Logs, Metrics, Traces
- **Structured Logging**:
  - SLF4J + Logback configuration
  - Why structured JSON logs matter (machine-parseable)
  - MDC (Mapped Diagnostic Context) — add `traceId`, `userId`, `rfqId` to every log
  - Log levels: TRACE, DEBUG, INFO, WARN, ERROR — when to use each
- **Distributed Tracing**:
  - What is a trace? What is a span?
  - Trace propagation via HTTP headers (`traceparent`, `tracestate`)
  - **Micrometer Tracing** (replaces Sleuth in Spring Boot 3)
  - **Zipkin** — trace visualisation: see full RFQ → Quote → Deal → Trade flow
  - **OpenTelemetry** — industry standard (brief intro)
- **Metrics with Micrometer**:
  - Counter, Gauge, Timer, DistributionSummary
  - Custom business metrics:
    - `rfq.created.count`, `rfq.dealt.latency`, `trade.notional.total`
    - `pricing.latency.p99` — track if pricing meets SLA
  - `/actuator/prometheus` endpoint
- **Prometheus + Grafana**:
  - Prometheus scrapes metrics from all services
  - Grafana dashboard: RFQ volumes, trade P&L, latency heatmaps
- **Alerting**: Prometheus alert rules
- **Health checks**: liveness vs readiness probes (important for K8s)

**What you'll build:**
```
Every service:
  ├── Structured JSON logging with traceId in every log line
  ├── Micrometer traces exported to Zipkin
  ├── Custom business metrics → /actuator/prometheus
  └── Meaningful health indicators

Grafana Dashboard:
  ├── RFQ volume per minute
  ├── Quote acceptance rate
  ├── Trade execution latency (p50, p95, p99)
  ├── Circuit breaker state per service
  └── AMPS message throughput
```

**Tests:**
- Unit: custom metric counters increment correctly
- Integration: verify trace propagates through rfq → pricing → market-data chain
- Test: health endpoint returns DOWN when DB unavailable

**Git commit:** `feat(step-12): add distributed tracing, Prometheus metrics, Grafana dashboards`

---

---

# PHASE 8 — Containerisation & Deployment
> *"Run the same anywhere"*

---

## Step 13 — Docker & Docker Compose

**Concepts you will learn:**
- What is a container vs a VM?
- `Dockerfile` anatomy: `FROM`, `COPY`, `RUN`, `EXPOSE`, `ENTRYPOINT`
- Multi-stage builds: build in one stage, run in a smaller image
- **Jib** (Google) — build Docker images without writing Dockerfiles
- Image layers and caching — why order matters for build speed
- **Docker Compose**:
  - `services`, `networks`, `volumes`
  - Service dependencies: `depends_on` + `healthcheck`
  - Environment variable injection
  - Named volumes for PostgreSQL persistence
- Container networking: bridge, host, overlay
- Pushing to Docker Hub / GitHub Container Registry
- `.dockerignore` — keep images small
- Security: run as non-root user

**What you'll build:**
```
docker-compose.yml → one command starts everything:
  ├── postgres-rfq        :5432
  ├── postgres-trade      :5433
  ├── amps-server         :9007
  ├── discovery-server    :8761
  ├── config-server       :8888
  ├── api-gateway         :8080
  ├── auth-service        :8083
  ├── rfq-service         :8081
  ├── pricing-service     :8084
  ├── market-data-service :8086
  ├── trade-service       :8082
  ├── risk-service        :8085
  ├── notification-service:8087
  ├── zipkin              :9411
  ├── prometheus          :9090
  └── grafana             :3000
```

**Tests:**
- E2E: `docker-compose up` → full RFQ → Deal → Trade flow test
- Test: container health checks pass before dependent service starts

**Git commit:** `feat(step-13): dockerize all services, add docker-compose for local dev`

---

## Step 14 — Kubernetes (Minikube)

**Concepts you will learn:**
- Kubernetes architecture: control plane, nodes, kubelet
- Core objects: **Pod**, **Deployment**, **Service**, **Ingress**, **Namespace**
- **ConfigMap** and **Secret** (replaces Spring Cloud Config for K8s deployments)
- Why Eureka is less necessary in K8s (K8s service discovery built-in)
- **Readiness vs Liveness probes** — how K8s decides if a pod is healthy
- **Horizontal Pod Autoscaler (HPA)** — scale based on CPU/memory/custom metrics
- Rolling deployments — zero downtime updates
- **Persistent Volume Claims** — stateful services (PostgreSQL) in K8s
- **Resource requests and limits** — critical for trading workloads
- `kubectl` commands: apply, get, describe, logs, exec, port-forward
- **Helm**: the package manager for K8s — `Chart.yaml`, `values.yaml`, templates
- **Ingress Controller** (nginx) — external traffic routing

**What you'll build:**
```
k8s/
  ├── namespaces/
  │     └── trading-platform.yml
  ├── deployments/
  │     ├── rfq-service-deployment.yml
  │     ├── trade-service-deployment.yml
  │     └── ... (all services)
  ├── services/
  │     └── ... (ClusterIP, LoadBalancer)
  ├── configmaps/
  │     └── trading-config.yml
  ├── secrets/
  │     └── db-credentials.yml
  ├── ingress/
  │     └── trading-ingress.yml
  └── hpa/
        └── rfq-service-hpa.yml (scale when RFQ volume spikes)

helm/
  └── trading-platform/ (Helm chart for the whole system)
```

**Tests:**
- Deploy to minikube, run E2E test suite against K8s endpoints
- Test: kill an rfq-service pod, verify K8s restarts it
- Test: HPA scales up under simulated load

**Git commit:** `feat(step-14): add kubernetes manifests and helm chart`

---

---

# PHASE 9 — Advanced Patterns
> *"Production-grade capabilities"*

---

## Step 15 — Event Sourcing + Audit Trail

**Concepts you will learn:**
- **Event Sourcing**: store events, not state
  - Why it matters in financial systems (regulatory audit requirements)
  - Event store vs traditional database
  - Replaying events to rebuild state
  - Snapshots for performance
- **Append-only event log** for all trade mutations
- GDPR and regulatory compliance considerations
- **Axon Framework** (optional intro) — event sourcing + CQRS framework

**What you'll build:**
```
trade-service extended:
  ├── TradeEvent table: all state changes persisted as events
  │     TRADE_CREATED, RISK_APPROVED, TRADE_CONFIRMED, TRADE_SETTLED
  ├── GET /api/v1/trades/{id}/history → full audit trail
  └── State reconstruction from events
```

**Git commit:** `feat(step-15): add event sourcing audit trail to trade-service`

---

## Step 16 — API Documentation, Versioning & Developer Experience

**Concepts you will learn:**
- **OpenAPI 3.1 / Swagger** with `springdoc-openapi`
- Documenting Protobuf APIs (buf.build)
- API versioning strategies:
  - URL versioning: `/v1/`, `/v2/`
  - Header versioning: `Accept: application/vnd.trading.v2+json`
- **Backward compatibility** rules — what constitutes a breaking change
- **API changelog** and deprecation strategy
- **Postman collections** — shareable API tests
- Developer portal concepts

**Git commit:** `feat(step-16): add OpenAPI documentation, versioning strategy`

---

---

## Summary: Complete Learning Map

| Step | Name | Phase | Key Tech Learned |
|------|------|-------|-----------------|
| 1 | Hello RFQ + Protobuf | Foundations | Spring Boot, REST, Protobuf serialization |
| 2 | JPA + PostgreSQL + Flyway | Foundations | JPA, Hibernate, Testcontainers, Flyway |
| 3 | OpenFeign + Error Handling | Foundations | OpenFeign, WireMock, Problem Details RFC 9457 |
| 4 | Eureka Discovery | Discovery | Service Registry, Client-side LB |
| 5 | Config Server | Configuration | 12-Factor, Spring Cloud Config, @RefreshScope |
| 6 | API Gateway + Pact | Gateway | Spring Cloud Gateway, Rate Limiting, Contract Tests |
| 7 | JWT + Spring Security | Security | OAuth2 concepts, JWT, @PreAuthorize, RBAC |
| 8 | Resilience4j | Resilience | Circuit Breaker, Retry, Bulkhead, Timeout |
| 9 | AMPS Market Data | Messaging | AMPS publish/subscribe, SOW, content filtering |
| 10 | Trade Saga + CQRS | Patterns | Saga choreography, CQRS, Outbox, Idempotency |
| 11 | Protobuf + gRPC | Protocols | gRPC streaming, .proto schemas, bi-directional |
| 12 | Observability | Operations | Zipkin, Prometheus, Grafana, MDC logging |
| 13 | Docker + Compose | Deployment | Multi-stage Dockerfile, Compose, Jib |
| 14 | Kubernetes + Helm | Deployment | K8s objects, HPA, Helm charts |
| 15 | Event Sourcing | Advanced | Audit trail, event replay, append-only log |
| 16 | OpenAPI + Versioning | Developer Experience | Swagger, API versioning, Postman |

---

## Technology Decisions Confirmed

| Decision | Choice | Reason |
|----------|--------|--------|
| Build tool | **Maven** | Widely used in enterprise Java |
| Java version | **Java 21** | Latest LTS, virtual threads (Project Loom) |
| Messaging | **AMPS** | Industry standard in capital markets |
| Serialization | **Protobuf + JSON** | Performance (internal) + compatibility (external) |
| Database | **PostgreSQL 16** | Production-grade, ACID, rich feature set |
| Tests | **JUnit 5 + Mockito + Testcontainers + Pact** | Full pyramid |
| Tracing | **Zipkin + Micrometer** | Spring Boot 3 native |
| Metrics | **Prometheus + Grafana** | Industry standard |
| Deployment | **Docker + Kubernetes + Helm** | Production ready |

---

## Git Branch Strategy

```
main
  └── step-01/hello-rfq-protobuf
  └── step-02/jpa-postgresql-flyway
  └── step-03/openfeign-error-handling
  ...
  └── step-16/openapi-versioning
```

Each step: create branch → implement → test → commit → merge to main → tag

---

> [!IMPORTANT]
> **Ready to start?** Just say **"execute step 1"** and I will:
> 1. Explain all Step 1 concepts in detail with diagrams
> 2. Provide any installation steps needed
> 3. Write the minimal production-quality code with full comments
> 4. Write all tests
> 5. Guide you through the git commit
> 
> Then we move to Step 2 only after you say so.
