# NBA Stats Service

A Spring Boot microservice for aggregating NBA player statistics in real time, leveraging **Kafka** for ingestion, **Redis** for caching, and **PostgreSQL** for persistence—all without relying on an ORM.

---

## 🔧 Prerequisites

* **Docker** & **Docker Compose**
* (Optional) **Java 17** & **Maven**, if you prefer building/running outside Docker

---

## 🚀 Local Setup

1. Clone the repo and navigate in:

   ```bash
   git clone https://github.com/matik87/nba-stats.git
   cd nba-stats
   ```
2. Clean up existing volumes and start services:

   ```bash
   docker-compose down -v
   docker-compose up --build -d
   ```
3. Kafka broker, ZooKeeper, PostgreSQL, Redis, and the API will launch. The API listens on **[http://localhost:8080](http://localhost:8080)**.

---

## 🧪 Testing

* **Unit Tests**:

  ```bash
  mvn clean test -Dtest=**/*ImplTest
  ```
* **Integration Tests** (Testcontainers + Kafka + PostgreSQL):

  ```bash
  mvn verify -DskipUnitTests
  ```

---

## 🗃️ Input Validation

All DTOs use Bean Validation to enforce business rules without ORM:

* `@NotNull` on IDs
* `@Min`/`@Max` on numeric stats
* `@DecimalMin`/`@DecimalMax` on minutesPlayed
* Validated via `@Validated` + `@Valid` in the Kafka listener—invalid messages go to a DLT.

---

## 🔄 Flow Diagram

```
==================== Ingestion Flow ====================

 [External System]
        |
        v
 [Kafka Topic: player-stats]
        |
        v
 [StatsConsumer (@KafkaListener)]
        |
        v
 [StatsService.ingestPlayerStats()]
        |
        v
 [PostgreSQL: player_stat table]


=============== Caching & Aggregation Flow ===============

 [Client: GET /api/aggregates/players/:playerId?season=:year]
        |
        v
 [AggregateService]
        |
        v
 [Redis Cache: opsForValue.get(key)]
        |
   +----+----+
   |         |
  Yes       No
   |         |
   v         v
 [Return   [JDBC SELECT
  DTO      compute aggregates]
  from         |
  Redis]       v
             [Redis opsForValue.set(key,dto)]
                  |
                  v
             [Return DTO
              from fresh compute]
```

---

## 🏗️ Architecture

* **Kafka Topic**: `player-stats` (3 partitions, manual ack). Producer is an external system.
* **Consumer**: `StatsConsumer` batches and writes to `player_stat` via JDBC.
* **Dead-Letter Topic**: `player-stats.DLT` for messages that fail validation or processing after retries.
* **Caching**: Redis holds per-player/team aggregates with configurable TTL.
* **Read API**: stateless REST endpoints for player/team aggregates with cache-first logic.
* **No ORM**: direct JDBC via `JdbcTemplate` for explicit SQL control.

---

## 💡 Design Decisions & Trade-offs

1. **Why Kafka over REST ingestion?**

    * The requirement mentions machine-to-machine input. Removing the REST POST endpoint avoids unnecessary human-facing APIs and focuses on streaming ingestion.
    * Kafka supports high throughput, partitioning, and fault-tolerant consumption out of the box.

2. **Why not Kafka Streams?**

    * Considered using Kafka Streams for in-app aggregation and state stores, but chose simpler `JdbcTemplate` + SQL for assignment scope to minimize complexity and dependencies.
    * This keeps the codebase smaller and makes aggregation logic explicit and easy to test.

3. **Database Choice**

    * **PostgreSQL**: mature, supports ACID transactions and SQL analytics for aggregates.
    * No ORM avoids session management and reduces startup overhead.

4. **Caching Strategy**

    * **Redis** for sub-millisecond read latency and to decouple heavy aggregate queries from the DB under load.
    * Cache invalidation on writes with retries via `CacheInvalidationHelper` ensures consistency.

5. **Scalability & HA**

    * **Kafka partitions + consumer concurrency** scale ingestion horizontally.
    * **Stateless services**: consumer and REST layers can be replicated behind a load balancer.
    * **Redis cluster** and Multi-AZ PostgreSQL support high availability.

6. **Maintainability**

    * **Spring Boot conventions** and Dependency Injection for easy onboarding.
    * **Method-level Bean Validation** centralizes rules in DTOs.
    * **Configuration-driven** retries, topics, TTLs, and thread counts live in `application.yml`.

---

## ☁️ Deployment (AWS)

1. Push Docker images to ECR.
2. Deploy on ECS Fargate or EKS in a private VPC.
3. Use MSK for Kafka, RDS Multi-AZ for PostgreSQL, and ElastiCache for Redis.
4. ALB in front of REST APIs, security groups restricting access.
5. HPA based on CPU or custom Kafka lag metrics.
6. Monitoring with CloudWatch and logs shipped from containers.

---

## 📜 Logging

Configured via `logback.xml`:

```xml
<root level="INFO">
  <appender-ref ref="CONSOLE" />
</root>
```

Adjust package levels (e.g. `org.springframework.kafka`) to DEBUG for more details.
