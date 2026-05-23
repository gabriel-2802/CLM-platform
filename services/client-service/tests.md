# Test Documentation

## Overview
The `client-service` utilizes **JUnit 5**, **Mockito**, and **AssertJ** for testing. The Security contexts are verified using **Spring Security Test** (`@WithMockUser`, `@WebMvcTest`). There are currently **151** passing tests in the test suite.

## Test Types
The test suite consists of two primary layers:
1. **Controller Tests (`@WebMvcTest`)**: Focus on testing HTTP endpoints, request/response validation, HTTP status codes, and authorization rules. Dependencies (Services) are mocked using `@MockBean`.
2. **Service Tests (`@ExtendWith(MockitoExtension.class)`)**: Unit tests focusing on the core business logic. Dependencies (Repositories, external clients) are mocked using `@Mock`.

## Test Suites

### 1. Client Controller & Service
- **`ClientControllerTest`** 
  - Tests CRUD operations on Clients, role-based access (`MANAGER`, `ADMIN`, `USER`), and handles `ResourceNotFoundException`.
- **`ClientServiceTest`** 
  - Validates client creation, partial updates, listing, and template field mapping.
  
### 2. Task Controller & Service
- **`TaskControllerTest`** 
  - Tests task fetching by client, creation, updating, and removal. Validates that unauthorized users cannot access or modify tasks.
- **`TaskServiceTest`** 
  - Tests the business rules around tasks, mapping entities to DTOs, and proper exception throwing when tasks don't exist.

### 3. WorkPoint Controller & Service
- **`WorkPointControllerTest`** 
  - Validates endpoints for fetching, adding, updating, and deleting work points for a specific client.
- **`WorkPointServiceTest`** 
  - Tests business logic associated with managing work points, handling non-existent clients, and validating constraints.

### 4. Details Controller & Service
- **`DetailsControllerTest`** 
  - Tests endpoints for creating/replacing and patching client details, expecting 201/200, and ensuring proper validation errors (400) or missing resources (404) are handled correctly.
- **`DetailsServiceTest`** 
  - Tests saving details, updating specific fields (patching), and fetching client details.

### 5. History Controller & Service
- **`HistoryControllerTest`** 
  - Validates endpoints for inserting/updating history records and fetching lists of history per client. Validates JSON payload requirements.
- **`HistoryServiceTest`** 
  - Tests UPSERT logic, retrieval, and deletion of history entries per year.

### 6. Client Assignment Controller & Service
- **`ClientAssignmentControllerTest`** 
  - Tests endpoints assigning users to clients, replacing assignments, removing assignments, and fetching assignments.
- **`ClientAssignmentServiceTest`** 
  - Validates the assignment of user IDs to client IDs, ensuring duplicate assignments are handled or ignored, and testing the removal/replacement of sets of users.

### 7. Enums Controller
- **`EnumsControllerTest`**
  - Tests public accessibility of the Enums listing endpoint.
