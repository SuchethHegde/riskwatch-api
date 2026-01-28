# RiskWatch API - Usage Examples

This document provides practical examples for using the RiskWatch API.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Basic Examples](#basic-examples)
- [Advanced Scenarios](#advanced-scenarios)
- [Error Handling](#error-handling)

---

## Prerequisites

Ensure the API is running:
```bash
# Check health
curl http://localhost:8080/api/v1/health
```

---

## Basic Examples

### 1. Submit a Low-Risk Transaction

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "tx-001",
    "userId": "user-001",
    "amount": 50000.0,
    "timestamp": "2024-01-15T10:30:00Z",
    "deviceId": "device-abc",
    "location": "New York, US"
  }'
```

**Expected Response:**
- Risk Level: `LOW`
- Risk Score: < 0.4

### 2. Submit a High-Amount Transaction

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "tx-002",
    "userId": "user-001",
    "amount": 150000.0,
    "timestamp": "2024-01-15T10:31:00Z"
  }'
```

**Expected Response:**
- Risk Level: `MEDIUM` or `HIGH`
- Risk Score: ≥ 0.3
- Reason: "Transaction amount exceeds threshold."

### 3. Submit Multiple Transactions (Velocity Test)

```bash
# Submit 4 transactions within 2 minutes
for i in {1..4}; do
  curl -X POST http://localhost:8080/api/v1/transactions \
    -H "Content-Type: application/json" \
    -d "{
      \"transactionId\": \"tx-velocity-$i\",
      \"userId\": \"user-002\",
      \"amount\": 10000.0,
      \"timestamp\": \"2024-01-15T10:3${i}:00Z\"
    }"
  sleep 1
done
```

**Expected Response for 4th transaction:**
- Risk Level: `MEDIUM` or `HIGH`
- Reason: "High transaction velocity detected."

### 4. Get User Transactions

```bash
curl http://localhost:8080/api/v1/transactions/user/user-001
```

### 5. Get User Risk Profile

```bash
curl http://localhost:8080/api/v1/users/user-001/risk-profile
```

### 6. Get Flagged Users

```bash
curl http://localhost:8080/api/v1/users/flagged
```

---

## Advanced Scenarios

### Scenario 1: User Flagging

To trigger user flagging, submit 5 high-risk transactions:

```bash
# Submit 5 high-amount transactions
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/v1/transactions \
    -H "Content-Type: application/json" \
    -d "{
      \"transactionId\": \"tx-flag-$i\",
      \"userId\": \"user-flag-test\",
      \"amount\": 200000.0,
      \"timestamp\": \"2024-01-15T10:4${i}:00Z\"
    }"
done

# Check if user is flagged
curl http://localhost:8080/api/v1/users/user-flag-test/risk-profile

# Verify in flagged users list
curl http://localhost:8080/api/v1/users/flagged
```

### Scenario 2: Complete Transaction Flow

```bash
# 1. Submit transaction
TRANSACTION_ID="tx-complete-001"
USER_ID="user-complete-001"

curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d "{
    \"transactionId\": \"$TRANSACTION_ID\",
    \"userId\": \"$USER_ID\",
    \"amount\": 75000.0,
    \"timestamp\": \"2024-01-15T10:30:00Z\"
  }"

# 2. Get user transactions
curl http://localhost:8080/api/v1/transactions/user/$USER_ID

# 3. Get updated risk profile
curl http://localhost:8080/api/v1/users/$USER_ID/risk-profile
```

---

## Error Handling

### Invalid Request (Missing Fields)

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": ""
  }'
```

**Expected Response:** 400 Bad Request

### User Not Found

```bash
curl http://localhost:8080/api/v1/users/non-existent-user/risk-profile
```

**Expected Response:** 404 Not Found

### Negative Amount

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "tx-invalid",
    "userId": "user-001",
    "amount": -100.0
  }'
```

**Expected Response:** 400 Bad Request

---

## Using Postman

1. Import the collection (if available)
2. Set base URL: `http://localhost:8080`
3. Use the examples above as request templates

---

## Using Swagger UI

1. Navigate to: http://localhost:8080/swagger-ui.html
2. Explore available endpoints
3. Try requests directly from the UI
4. View request/response schemas

---

## Testing Script

Save this as `test-api.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080/api/v1"

echo "=== Health Check ==="
curl -s $BASE_URL/health | jq

echo -e "\n=== Submit Transaction ==="
curl -s -X POST $BASE_URL/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "tx-test-001",
    "userId": "user-test-001",
    "amount": 50000.0
  }' | jq

echo -e "\n=== Get User Risk Profile ==="
curl -s $BASE_URL/users/user-test-001/risk-profile | jq

echo -e "\n=== Get User Transactions ==="
curl -s $BASE_URL/transactions/user/user-test-001 | jq
```

Make it executable and run:
```bash
chmod +x test-api.sh
./test-api.sh
```

---

## Notes

- All timestamps should be in ISO 8601 format (UTC)
- Transaction IDs must be unique
- Amounts must be positive numbers
- The API returns consistent JSON response wrappers
