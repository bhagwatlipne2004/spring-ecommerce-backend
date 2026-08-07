# Spring Boot Notes

## Layered Architecture

Request

↓

Controller

↓

Service

↓

Repository

↓

Database

Response

↑

DTO

↑

Mapper

---

## Controller

Responsibilities:

- Receive HTTP Request
- Validate Request
- Call Service
- Return Response

Business logic should NOT be here.

---

## Service

Responsibilities:

- Business Logic
- Validation
- Transactions
- Calling Repository

---

## Repository

Responsibilities:

- Database Operations

Never put business logic here.

---

## Dependency Injection

Constructor Injection

Example:

```java
public ProductServiceImpl(ProductRepository repository){
    this.repository = repository;
}
```

Benefits:

- Easier testing
- Immutable dependencies
- Recommended by Spring

---

## ResponseEntity

Return HTTP responses.

Examples:

```java
ResponseEntity.ok(response)
```

```java
ResponseEntity.noContent().build()
```

```java
ResponseEntity.status(HttpStatus.CREATED)
```