# Student Management API - Testing Guide

This guide provides detailed instructions for testing the Student Management API using Postman and other tools.

## Prerequisites

Before testing, ensure:
1. The application is running (follow README.md setup instructions)
2. Postman is installed, or use curl/browser
3. Default URL: `http://localhost:8080`

## Quick Start Testing

### 1. Verify Application is Running

Open browser and visit:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **H2 Console**: http://localhost:8080/h2-console (if using H2)

### 2. Import Postman Collection

Import the `Student-Management-API.postman_collection.json` file into Postman.

## Test Scenarios

### Scenario 1: Create and Retrieve Student

#### Step 1: Create a Student (Success)
```bash
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "email": "alice.johnson@example.com",
    "course": "Computer Science",
    "age": 22
  }'
```

**Expected Response**: HTTP 201 Created
```json
{
  "id": 1,
  "name": "Alice Johnson",
  "email": "alice.johnson@example.com",
  "course": "Computer Science",
  "age": 22
}
```

#### Step 2: Get Student by ID
```bash
curl http://localhost:8080/api/students/1
```

**Expected Response**: HTTP 200 OK
```json
{
  "id": 1,
  "name": "Alice Johnson",
  "email": "alice.johnson@example.com",
  "course": "Computer Science",
  "age": 22
}
```

### Scenario 2: Validation Testing

#### Test Invalid Email Format
```bash
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bob Smith",
    "email": "invalid-email",
    "course": "IT",
    "age": 20
  }'
```

**Expected Response**: HTTP 400 Bad Request
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed. Check 'validationErrors' for details.",
  "path": "/api/students",
  "validationErrors": {
    "email": "Email must be valid"
  }
}
```

#### Test Age Validation (Age < 18)
```bash
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Charlie Brown",
    "email": "charlie@example.com",
    "course": "CS",
    "age": 16
  }'
```

**Expected Response**: HTTP 400 Bad Request
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed. Check 'validationErrors' for details.",
  "path": "/api/students",
  "validationErrors": {
    "age": "Age must be at least 18"
  }
}
```

#### Test Name Length Validation
```bash
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "name": "A",
    "email": "test@example.com",
    "course": "CS",
    "age": 20
  }'
```

**Expected Response**: HTTP 400 Bad Request

### Scenario 3: Duplicate Email Testing

#### Step 1: Create First Student
```bash
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "name": "David Wilson",
    "email": "david@example.com",
    "course": "Data Science",
    "age": 23
  }'
```

**Expected**: HTTP 201 Created

#### Step 2: Try to Create Student with Same Email
```bash
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "name": "David Wilson Jr",
    "email": "david@example.com",
    "course": "AI",
    "age": 24
  }'
```

**Expected Response**: HTTP 409 Conflict
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Student already exists with email: 'david@example.com'",
  "path": "/api/students"
}
```

### Scenario 4: Update Operations

#### Update Student
```bash
curl -X PUT http://localhost:8080/api/students/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Updated",
    "email": "alice.updated@example.com",
    "course": "Software Engineering",
    "age": 23
  }'
```

**Expected Response**: HTTP 200 OK

### Scenario 5: Pagination and Sorting

#### Get All Students with Pagination
```bash
curl "http://localhost:8080/api/students?paginated=true&page=0&size=5&sortBy=name&direction=asc"
```

**Expected Response**: HTTP 200 OK
```json
{
  "students": [...],
  "currentPage": 0,
  "totalItems": 10,
  "totalPages": 2
}
```

#### Sort by Age (Descending)
```bash
curl "http://localhost:8080/api/students?paginated=true&page=0&size=10&sortBy=age&direction=desc"
```

### Scenario 6: Search Functionality

#### Search by Name
```bash
curl "http://localhost:8080/api/students/search?name=Alice&page=0&size=10"
```

**Expected**: Returns all students with "Alice" in their name

#### Search by Course
```bash
curl "http://localhost:8080/api/students/search?course=Computer&page=0&size=10"
```

**Expected**: Returns all students in courses containing "Computer"

#### Search by Keyword (searches both name and course)
```bash
curl "http://localhost:8080/api/students/search?keyword=science&page=0&size=10"
```

**Expected**: Returns students with "science" in name OR course

### Scenario 7: Delete Operations

#### Delete Student
```bash
curl -X DELETE http://localhost:8080/api/students/1
```

**Expected Response**: HTTP 200 OK
```json
{
  "message": "Student deleted successfully"
}
```

#### Try to Get Deleted Student
```bash
curl http://localhost:8080/api/students/1
```

**Expected Response**: HTTP 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Student not found with id: '1'",
  "path": "/api/students/1"
}
```

## Complete Test Suite

### 1. Create Multiple Students
Run these commands to populate the database:

```bash
# Student 1
curl -X POST http://localhost:8080/api/students -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","course":"Computer Science","age":20}'

# Student 2
curl -X POST http://localhost:8080/api/students -H "Content-Type: application/json" \
  -d '{"name":"Jane Smith","email":"jane@example.com","course":"Data Science","age":22}'

# Student 3
curl -X POST http://localhost:8080/api/students -H "Content-Type: application/json" \
  -d '{"name":"Bob Johnson","email":"bob@example.com","course":"Software Engineering","age":21}'

# Student 4
curl -X POST http://localhost:8080/api/students -H "Content-Type: application/json" \
  -d '{"name":"Alice Williams","email":"alice@example.com","course":"Cybersecurity","age":23}'

# Student 5
curl -X POST http://localhost:8080/api/students -H "Content-Type: application/json" \
  -d '{"name":"Charlie Brown","email":"charlie@example.com","course":"Artificial Intelligence","age":24}'
```

### 2. Test All Endpoints

```bash
# Get all students
curl http://localhost:8080/api/students

# Get all students (paginated)
curl "http://localhost:8080/api/students?paginated=true&page=0&size=3"

# Get student by ID
curl http://localhost:8080/api/students/1

# Update student
curl -X PUT http://localhost:8080/api/students/1 -H "Content-Type: application/json" \
  -d '{"name":"John Updated","email":"john.updated@example.com","course":"CS","age":21}'

# Search by name
curl "http://localhost:8080/api/students/search?name=John"

# Search by course
curl "http://localhost:8080/api/students/search?course=Science"

# Search by keyword
curl "http://localhost:8080/api/students/search?keyword=AI"

# Delete student
curl -X DELETE http://localhost:8080/api/students/5
```

## Testing with Swagger UI

1. Open http://localhost:8080/swagger-ui.html
2. Browse all available endpoints
3. Click on any endpoint to expand
4. Click "Try it out"
5. Fill in the parameters/body
6. Click "Execute"
7. View the response

## Expected HTTP Status Codes

| Operation | Success Code | Error Codes |
|-----------|--------------|-------------|
| Create Student | 201 Created | 400 (validation), 409 (duplicate) |
| Get Student | 200 OK | 404 (not found) |
| Update Student | 200 OK | 400 (validation), 404 (not found), 409 (duplicate) |
| Delete Student | 200 OK | 404 (not found) |
| Search | 200 OK | - |
| Get All | 200 OK | - |

## Performance Testing

Test pagination with large datasets:

```bash
# Create 50 students (use a script)
for i in {1..50}; do
  curl -X POST http://localhost:8080/api/students -H "Content-Type: application/json" \
    -d "{\"name\":\"Student$i\",\"email\":\"student$i@example.com\",\"course\":\"Course$i\",\"age\":$((18 + i % 10))}"
done

# Test pagination
curl "http://localhost:8080/api/students?paginated=true&page=0&size=10"
curl "http://localhost:8080/api/students?paginated=true&page=1&size=10"
```

## Validation Checklist

- [ ] Create student with valid data (201)
- [ ] Create student with invalid email (400)
- [ ] Create student with age < 18 (400)
- [ ] Create student with duplicate email (409)
- [ ] Get existing student (200)
- [ ] Get non-existent student (404)
- [ ] Update existing student (200)
- [ ] Update non-existent student (404)
- [ ] Delete existing student (200)
- [ ] Delete non-existent student (404)
- [ ] Get all students without pagination (200)
- [ ] Get all students with pagination (200)
- [ ] Sort by name ascending (200)
- [ ] Sort by age descending (200)
- [ ] Search by name (200)
- [ ] Search by course (200)
- [ ] Search by keyword (200)
- [ ] Access Swagger UI (200)
- [ ] Access H2 Console (200, if using H2)

## Screenshots

When testing, capture screenshots of:

1. Successful POST request (201)
2. GET request with pagination
3. PUT request (200)
4. DELETE request (200)
5. Validation error (400)
6. Duplicate email error (409)
7. Not found error (404)
8. Swagger UI page
9. Search results
10. H2 Console (if using H2)

## Database Verification (H2 Console)

1. Open: http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:studentdb`
3. Username: `sa`
4. Password: (empty)
5. Run queries:

```sql
-- View all students
SELECT * FROM students;

-- Count students
SELECT COUNT(*) FROM students;

-- Students by course
SELECT course, COUNT(*) FROM students GROUP BY course;
```

## Troubleshooting

### Application not starting
- Check if port 8080 is available
- Verify Java 17 is installed
- Check application logs

### Connection refused
- Ensure application is running
- Verify correct port
- Check firewall settings

### 404 errors
- Verify correct endpoint URL
- Check application context path
- Ensure resource exists (for GET/PUT/DELETE)

## Automated Testing Script

Save this as `test-api.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080/api/students"

echo "Creating students..."
curl -X POST $BASE_URL -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","course":"CS","age":20}'

echo -e "\n\nGetting all students..."
curl $BASE_URL

echo -e "\n\nSearching students..."
curl "$BASE_URL/search?keyword=Test"

echo -e "\n\nTest completed!"
```

Run with:
```bash
chmod +x test-api.sh
./test-api.sh
```

---

**Happy Testing!** 🚀
