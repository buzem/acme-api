# ACME University API

A Spring Boot REST API for managing university lecturers and students with many-to-many relationships.

## Quick Start

### Prerequisites

- Java 17
- Gradle 8

### Running the Application

**Option 1: Local Development with H2 in-memory database**
```bash
./gradlew bootRun
```

**Option 2: Docker with PostgreSQL**
```bash
docker-compose up --build
```

The API will be available at http://localhost:8080

### Running Tests

```bash
./gradlew test
```

### Testing the API

Run the demo script to create a lecturer and assign a student:

```bash
./test-demo.sh
```

## API Documentation

Once the application is running, API documentation can be found here:

http://localhost:8080/swagger-ui.html

## Design Decisions, Choices, and Considerations

### 1. Database Optimization

- **N+1 Problem**: Without optimization, loading a lecturer and accessing their students triggers N+1 queries (1 query for lecturer + N queries for each student)
- **EntityGraph Solution**: `@EntityGraph(attributePaths = {"students"})` fetches lecturer and all students in a single JOIN query instead of multiple database round trips
- **Method Separation**: `findByLecturerId()` loads only lecturer data (lazy loading), while `findLecturerWithStudents()` uses EntityGraph for complete data
- **Circular Reference Prevention**: `@ToString(exclude = "students")` prevents infinite loops when entities reference each other in many-to-many relationships

### 2. Dual ID Architecture

There are two IDs for each entity:

- **Natural key**: `lecturerId` or `studentId`, used for uniqueness checks and API request parameters
- **Database key**: `id` (auto-generated), used for internal JPA operations and foreign key relationships

### 3. Student Reuse Logic

- Same `studentId` + same name/surname → Reuse existing student
- Same `studentId` + different name/surname → Throw error (prevents data corruption)
- **Business Rule**: Allows many-to-many relationships while maintaining data integrity

## Rate Limiting

- **Limit**: 20 requests per 10 seconds