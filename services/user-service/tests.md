# User Service Test Documentation

This document outlines the testing strategy, types of tests, and current test coverage for the User Service.

## Overview

- **Total Test Count:** 62 tests
- **Frameworks Used:** JUnit 5, Mockito, AssertJ, Spring Boot Test
- **Database:** H2 (In-memory, PostgreSQL compatibility mode)
- **Style:** Behavior-Driven Development (BDD) style using `Given / When / Then` comments. Tests are logically grouped using JUnit 5 `@Nested` classes.

## Testing Strategy

The testing strategy follows a **testing pyramid** approach, focusing heavily on isolated unit tests for business logic and slice tests for web layers, with focused integration tests for the data access layer.

1.  **Isolation:** Components are tested in isolation using mocks for their dependencies to ensure fast execution and precise failure isolation.
2.  **Boundary Testing:** Tests cover both "Happy Paths" (successful executions) and "Sad Paths" (expected exceptions, validation failures, not found errors).
3.  **Security Bypass in Slice Tests:** For Controller tests, the security filter chain (JWT parsing) is bypassed by explicitly mocking the `JwtAuthenticationFilter` and `JwtTokenProvider` to focus purely on controller routing and validation.
4.  **Database Isolation:** Repository tests use an H2 in-memory database. Flyway migrations and `AdminSeeder` beans are intentionally mocked (`@MockitoBean`) to prevent conflicts with Hibernate's `ddl-auto=create-drop` mechanism during tests.

## Test Types

### 1. Unit Tests (Service Layer)
- **Location:** `src/test/java/clm/user/demo/services/`
- **Annotations:** `@ExtendWith(MockitoExtension.class)`
- **Purpose:** Verifies core business logic, exception throwing (e.g., `ResourceNotFoundException`, `DatabaseValidationException`), and correct dependency interactions.
- **Coverage:**
  - `UserServiceTest`: CRUD operations, role assignments, password resets.
  - `AuthServiceTest`: User registration, authentication token generation.

### 2. Web Layer Tests (Controller Layer)
- **Location:** `src/test/java/clm/user/demo/controllers/`
- **Annotations:** `@WebMvcTest`, `@AutoConfigureMockMvc(addFilters = false)`
- **Purpose:** Slice tests that load only the web layer (Controllers, ExceptionHandlers). Verifies HTTP status codes, JSON serialization/deserialization, URL routing, and `@Valid` input validations.
- **Coverage:**
  - `UserControllerTest`: User management endpoints, verifying correct JSON responses and 404s.
  - `AuthControllerTest`: Login and Registration endpoints, verifying 400 Bad Request responses for malformed inputs (e.g., weak passwords, invalid emails).

### 3. Data Access Tests (Repository Layer)
- **Location:** `src/test/java/clm/user/demo/repositories/`
- **Annotations:** `@SpringBootTest`, `@Transactional`, `@ActiveProfiles("test")`
- **Purpose:** Integration tests verifying JPA configurations, derived queries (`findByEmail`), and fetch strategies (e.g., `@EntityGraph`).
- **Coverage:**
  - `UserRepositoryTest`: Verifies saving, retrieving, and pagination.
  - `RoleRepositoryTest`: Verifies finding roles by name.

## Running Tests

To execute the test suite locally:

```bash
mvn clean test
```

*Note: The `pom.xml` has been configured to suppress JVM warnings related to Lombok and dynamic agent loading (Mockito) on Java 21+.*
