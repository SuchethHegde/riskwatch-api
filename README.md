# RiskWatch API

RiskWatch is a Spring Boot–based risk assessment API that evaluates transaction and user risk levels in real time.  
It tracks transactions, evaluates potential risk patterns, and maintains a dynamic user risk profile for monitoring and fraud detection systems.

---

## Overview

RiskWatch is designed to be a modular and extensible backend system that can integrate into larger fraud detection or financial monitoring pipelines.  
It allows you to record user transactions, evaluate them for anomalies, and maintain risk scores that evolve based on behavior.

---

## Features

- Transaction risk scoring based on configurable parameters
- User risk profile tracking with rolling averages
- Configurable risk thresholds for alerting and monitoring
- RESTful endpoints for easy integration
- Clean separation of layers (Controller, Service, Repository, DTO, Model)
- Extensible and maintainable codebase

---

## Tech Stack

- Java 17  
- Spring Boot 3.x  
- Maven  
- Lombok  
- Swagger / OpenAPI  

---

## API Endpoints

| Endpoint | Method | Description |
|-----------|--------|-------------|
| `/api/transactions` | POST | Submit a transaction for risk evaluation |
| `/api/users/{id}/risk-profile` | GET | Retrieve a user's risk profile |
| `/api/health` | GET | Basic health check endpoint |

---

## Build and Run Locally

```bash
# Clone the repository
git clone https://github.com/SuchethHegde/riskwatch-api.git
cd riskwatch-api

# Build the project (skip tests for faster build)
./mvnw clean package -DskipTests

# Run the Spring Boot app
./mvnw spring-boot:run

# The API will be available at:
http://localhost:8080

# Running Tests
./mvnw test
```

## Author
- Sucheth Hegde

## License

This project is licensed under the MIT License.  
See the [LICENSE] file for details.

---

## Future Improvements

- Integrate with a real database (PostgreSQL or MongoDB)
- Add authentication and user management
- Introduce event-driven transaction streams (Kafka)
- Build a simple risk analysis dashboard
- Add ML-based anomaly detection module

---

*Note: This project is intended for educational and demonstration purposes.*
