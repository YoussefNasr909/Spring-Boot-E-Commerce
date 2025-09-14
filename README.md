# ⚡️ E‑Commerce Microservices (Spring Boot & Spring Cloud)

[![Java](https://img.shields.io/badge/Java-17-007396?logo=java\&logoColor=white\&style=for-the-badge)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot\&logoColor=white\&style=for-the-badge)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.x-6DB33F?logo=spring\&logoColor=white\&style=for-the-badge)](https://spring.io/projects/spring-cloud)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?logo=mysql\&logoColor=white\&style=for-the-badge)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker\&logoColor=white\&style=for-the-badge)](https://www.docker.com/)

**Modular, resilient e‑commerce backend** showcasing microservices with **Spring Boot** & **Spring Cloud**. It implements a production‑style order flow (**Check → Charge → Reserve → Confirm**) with **API Gateway routing**, **Eureka service discovery**, and **Resilience4j** for fault tolerance. Clean developer experience with runnable curl tests & Postman.

---

## ✨ Key Features

* **API Gateway** for unified entry & routing
* **Service Discovery** with **Eureka**
* **DB‑per‑service** isolation (Shop, Inventory, Wallet)
* **Resilience4j** circuit breaker & retry on inter‑service calls
* Clear **order orchestration** in Shop (check stock → charge wallet → reserve stock → confirm)
* Optional **Config Server** for centralized configuration

---

## 🧱 Built With

This section lists primary frameworks/libraries used to bootstrap the project.

* [![Java 17](https://img.shields.io/badge/Java-17-007396?logo=java\&logoColor=white\&style=for-the-badge)](https://www.oracle.com/java/)
* [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot\&logoColor=white\&style=for-the-badge)](https://spring.io/projects/spring-boot)
* [![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud-Gateway-6DB33F?logo=spring\&logoColor=white\&style=for-the-badge)](https://spring.io/projects/spring-cloud-gateway)
* [![Eureka](https://img.shields.io/badge/Spring%20Cloud-Eureka-6DB33F?logo=spring\&logoColor=white\&style=for-the-badge)](https://spring.io/projects/spring-cloud)
* [![Resilience4j](https://img.shields.io/badge/Resilience4j-Fault%20Tolerance-0A84FF?style=for-the-badge)](https://resilience4j.readme.io/)
* [![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?logo=mysql\&logoColor=white\&style=for-the-badge)](https://www.mysql.com/)
* [![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker\&logoColor=white\&style=for-the-badge)](https://www.docker.com/)
* [![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven\&logoColor=white\&style=for-the-badge)](https://maven.apache.org/)
* [![OpenAPI](https://img.shields.io/badge/OpenAPI%2FSwagger-Docs-85EA2D?logo=swagger\&logoColor=black\&style=for-the-badge)](https://swagger.io/specification/)

---

## 🗺️ Architecture

```text
            +---------------------+
            |   Config Server*    |
            +---------------------+
                     │
                     ▼
            +---------------------+
            |  Eureka Discovery   |
            +---------------------+
                     ▲
                     │
           +---------------------+
           |     API Gateway     |
           +---------------------+
         /          |          \
        /           |           \
       ▼            ▼            ▼
+----------------+  +----------------+  +----------------+
|  Shop Service  |  | Inventory Svc  |  | Wallet Service |
+----------------+  +----------------+  +----------------+
```

*Config Server exists in the repo but isn’t required to run the demo.*

---

## 🧰 Prerequisites

* Java **17+**
* Maven **3.6+**
* MySQL **8.x** (running locally)

### MySQL quick setup

Create the three databases and users used by the services:

```sql
CREATE DATABASE wallet_db;      CREATE USER 'wallet_user'@'localhost' IDENTIFIED BY 'wallet_123';
GRANT ALL PRIVILEGES ON wallet_db.* TO 'wallet_user'@'localhost';

CREATE DATABASE inventory_db;   CREATE USER 'inventory_user'@'localhost' IDENTIFIED BY 'inventory_123';
GRANT ALL PRIVILEGES ON inventory_db.* TO 'inventory_user'@'localhost';

CREATE DATABASE shop_db;        CREATE USER 'shop_user'@'localhost' IDENTIFIED BY 'shop_123';
GRANT ALL PRIVILEGES ON shop_db.* TO 'shop_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## ▶️ Quickstart (Run Locally)

### 1) Clone

```bash
git clone <your-repo-url>
cd <your-repo-folder>
```

### 2) Start Core Infrastructure

```bash
# Eureka Discovery
cd discovery-service/discovery-service
mvn spring-boot:run
```

Optional: start Config Server if you plan to externalize configs.

```bash
cd ../../config-service/config-service
mvn spring-boot:run
```

### 3) Start API Gateway

```bash
cd ../../api-gateway/api-gateway
mvn spring-boot:run
```

### 4) Start Business Services (separate terminals)

```bash
# Shop Service
cd ../../shop-service/shop-service
mvn spring-boot:run

# Inventory Service
cd ../../inventory-service/inventory-service
mvn spring-boot:run

# Wallet Service
cd ../../wallet-service/wallet-service
mvn spring-boot:run
```

---

## 🔌 Default Ports

| Service           | Port | Notes                                          |
| ----------------- | ---- | ---------------------------------------------- |
| Eureka Discovery  | 8761 | [http://localhost:8761](http://localhost:8761) |
| Config Server\*   | 8888 | Optional                                       |
| API Gateway       | 8090 | Single entry point                             |
| Shop Service      | 8082 | Orchestrates orders                            |
| Wallet Service    | 8081 | Wallet balance & transactions                  |
| Inventory Service | 8083 | Products & stock                               |

*Only needed if you wire services to it.*

---

## 🧪 Smoke Tests (Quick API Checks)

> You can also go through the API Gateway (path‑based routing) if configured to forward these prefixes.

**Ping**

```bash
curl http://localhost:8081/wallet/ping
curl http://localhost:8083/inventory/ping
curl http://localhost:8082/shop/ping
```

**Wallet**

```bash
# Create a wallet
curl -X POST http://localhost:8081/wallet/create \
  -H "Content-Type: application/json" \
  -d '{"ownerEmail":"john@example.com"}'

# Deposit / Withdraw
curl -X POST http://localhost:8081/wallet/1/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount": 200.00, "reference":"init"}'
```

**Inventory**

```bash
# Create a product
curl -X POST http://localhost:8083/inventory/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-1","name":"Phone","price":499.99,"quantity":10}'

# Check + consume stock
curl -X POST http://localhost:8083/inventory/stock/check \
  -H "Content-Type: application/json" \
  -d '{"productId":1, "quantity":2}'
```

**Shop (create order)**

```bash
curl -X POST http://localhost:8082/shop/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail":"john@example.com",
    "walletId":1,
    "items":[{"productId":1, "quantity":2}]
  }'
```

---

## 📝 License

This project is licensed under the **MIT License** — feel free to use, modify, and distribute.
