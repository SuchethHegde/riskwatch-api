# RiskWatch API

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)  
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)  
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

RiskWatch is a production-ready Spring Boot API for real-time transaction risk assessment and user risk profiling. It evaluates transactions for potential fraud indicators, maintains dynamic user risk profiles, and provides RESTful endpoints for integration with fraud detection systems.

---

## Features

- Real-time risk evaluation based on transaction amount and velocity  
- User risk profiling with rolling averages and automatic flagging  
- RESTful API with OpenAPI/Swagger documentation  
- Unit and integration testing  
- Structured logging using SLF4J and Logback  
- Docker and Docker Compose support  
- PostgreSQL (production) and H2 (development) database support  
- Global exception handling  
- Interactive Swagger UI  

---

## Architecture

```
┌─────────────────┐
│   Controllers   │  REST API Layer
└────────┬────────┘
         │
┌────────▼────────┐
│    Services     │  Business Logic Layer
└────────┬────────┘
         │
┌────────▼────────┐
│  Repositories   │  Data Access Layer
└────────┬────────┘
         │
┌────────▼────────┐
│    Database     │  PostgreSQL / H2
└─────────────────┘
```

### Key Components

- TransactionController – Transaction submission and retrieval  
- UserController – User risk profile endpoints  
- RiskEvaluator – Core risk scoring engine  
- TransactionService – Transaction processing and persistence  
- UserRiskProfileService – User risk tracking logic  

---

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+ (or Maven Wrapper)
- Docker (optional)

### Run with Docker

```bash
git clone https://github.com/SuchethHegde/riskwatch-api.git
cd riskwatch-api
docker-compose up -d
```

Access:
- API: http://localhost:8080  
- Swagger UI: http://localhost:8080/swagger-ui/index.html  

### Run Locally

```bash
git clone https://github.com/SuchethHegde/riskwatch-api.git
cd riskwatch-api
./mvnw clean package
./mvnw spring-boot:run
```

Or run the JAR:

```bash
java -jar target/riskwatch-0.0.1-SNAPSHOT.jar
```

---

## API Documentation

Swagger UI: http://localhost:8080/swagger-ui.html  
OpenAPI Spec: http://localhost:8080/v3/api-docs  

### Submit Transaction

```
POST /api/v1/transactions
```

```json
{
  "transactionId": "tx-12345",
  "userId": "user-001",
  "amount": 50000.0,
  "timestamp": "2024-01-15T10:30:00Z",
  "deviceId": "device-abc",
  "location": "New York, US"
}
```

### Get User Transactions

```
GET /api/v1/transactions/user/{userId}
```

### Get User Risk Profile

```
GET /api/v1/users/{userId}/risk-profile
```

### Get Flagged Users

```
GET /api/v1/users/flagged
```

### Health Check

```
GET /api/v1/health
```

---

## Testing

Run tests:

```bash
./mvnw test
```

Coverage report:

```bash
./mvnw test jacoco:report
```

---

## Configuration

`application.yaml`

```yaml
risk:
  thresholds:
    amount: 100000
    velocity:
      limit: 3
      window-minutes: 2

spring:
  datasource:
    url: jdbc:h2:mem:riskwatchdb
```

Environment variables (production):

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/riskwatch
SPRING_DATASOURCE_USERNAME=riskwatch
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_PROFILES_ACTIVE=prod
```

---

## Docker

Build image:

```bash
docker build -t riskwatch-api:latest .
```

Run stack:

```bash
docker-compose up -d
docker-compose logs -f
docker-compose down
docker-compose down -v
```

---

## Risk Scoring Algorithm

Risk score is calculated using:

- Amount threshold → +0.3  
- High transaction velocity → +0.4  

Risk Levels:

- LOW: Score < 0.4  
- MEDIUM: 0.4 <= Score < 0.7  
- HIGH: Score >= 0.7  

Users are flagged when:

- Average risk score ≥ 0.7, or  
- 5 or more high-risk transactions  

---

## Development

```
src/
├── main/java/com/sucheth/riskwatch/
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── exception/
│   ├── model/
│   ├── repository/
│   └── service/
└── resources/application.yaml
```

Best practices followed:

- Layered architecture  
- Dependency injection  
- Transaction management  
- Input validation  
- Global error handling  
- Structured logging  

---

## Security Considerations

For production:

- Add authentication and authorization  
- Implement rate limiting  
- Enforce HTTPS  
- Secure database credentials  
- Manage secrets securely  

---

## Future Enhancements

- Database migrations (Flyway/Liquibase)  
- Redis caching  
- Kafka event streaming  
- ML anomaly detection  
- Dashboard UI  
- Multi-tenancy  
- GraphQL API  

---

## Contributing

Pull requests are welcome.

---

## License

MIT License. See LICENSE file.

---

## Author

Sucheth Hegde  
https://github.com/SuchethHegde

---

## Acknowledgments

Spring Boot team and the open source community.
