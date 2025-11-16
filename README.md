# Student Management REST API

A comprehensive REST API for managing student information built with Spring Boot 3+, featuring CRUD operations, pagination, sorting, search capabilities, and full Swagger documentation.

## Features

- **CRUD Operations**: Create, Read, Update, and Delete students
- **Validation**: Input validation with proper error messages (age > 18, valid email format, etc.)
- **Service Layer Architecture**: Proper layered architecture (Controller → Service → Repository)
- **Global Exception Handling**: Centralized error handling with meaningful HTTP status codes
- **Pagination & Sorting**: Efficient data retrieval with customizable pagination
- **Search Functionality**: Search students by name, course, or keyword
- **Swagger UI**: Interactive API documentation
- **Multiple Database Support**: H2 (in-memory), MySQL, PostgreSQL
- **Docker Support**: Containerized application with Docker and Docker Compose
- **Proper HTTP Status Codes**: 200, 201, 400, 404, 409, 500

## Technology Stack

- **Spring Boot**: 3.2.0
- **Java**: 17
- **Spring Data JPA**: For database operations
- **Spring Validation**: For input validation
- **Lombok**: To reduce boilerplate code
- **Springdoc OpenAPI**: For Swagger documentation
- **Databases**: H2, MySQL 8.0, PostgreSQL 16
- **Maven**: Build tool
- **Docker**: Containerization

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose (for containerized deployment)
- MySQL 8.0 or PostgreSQL 16 (optional, for production database)

## Project Structure

```
student-management-api/
├── src/
│   ├── main/
│   │   ├── java/com/student/management/
│   │   │   ├── controller/        # REST Controllers
│   │   │   ├── service/           # Business Logic
│   │   │   ├── repository/        # Data Access Layer
│   │   │   ├── entity/            # JPA Entities
│   │   │   ├── dto/               # Data Transfer Objects
│   │   │   ├── exception/         # Custom Exceptions & Global Handler
│   │   │   ├── config/            # Configuration Classes
│   │   │   └── StudentManagementApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-h2.properties
│   │       ├── application-mysql.properties
│   │       └── application-postgresql.properties
│   └── test/
├── database/                       # SQL Scripts
│   ├── mysql-schema.sql
│   └── postgresql-schema.sql
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Getting Started

### Option 1: Running with Maven (H2 Database)

1. **Clone the repository**
```bash
git clone <repository-url>
cd student-management-api
```

2. **Build the project**
```bash
mvn clean install
```

3. **Run the application**
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080` with H2 in-memory database.

### Option 2: Running with MySQL

1. **Start MySQL database**
```bash
# Install and start MySQL
sudo systemctl start mysql

# Create database
mysql -u root -p
CREATE DATABASE studentdb;
```

2. **Load sample data (optional)**
```bash
mysql -u root -p studentdb < database/mysql-schema.sql
```

3. **Update credentials** in `src/main/resources/application-mysql.properties` if needed

4. **Run with MySQL profile**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

### Option 3: Running with PostgreSQL

1. **Start PostgreSQL database**
```bash
# Install and start PostgreSQL
sudo systemctl start postgresql

# Create database
sudo -u postgres psql
CREATE DATABASE studentdb;
```

2. **Load sample data (optional)**
```bash
psql -U postgres -d studentdb -f database/postgresql-schema.sql
```

3. **Update credentials** in `src/main/resources/application-postgresql.properties` if needed

4. **Run with PostgreSQL profile**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgresql
```

### Option 4: Running with Docker

#### Using H2 Database (Default)
```bash
# Build and run
docker-compose up student-api-h2
```

#### Using MySQL
```bash
# Build and run with MySQL profile
docker-compose --profile mysql up
```
The API will be available at `http://localhost:8081`

#### Using PostgreSQL
```bash
# Build and run with PostgreSQL profile
docker-compose --profile postgres up
```
The API will be available at `http://localhost:8082`

## API Endpoints

### Base URL
```
http://localhost:8080/api/students
```

### 1. Create Student
- **Method**: POST
- **Endpoint**: `/api/students`
- **Status Code**: 201 Created

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "course": "Computer Science",
  "age": 20
}
```

**Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "course": "Computer Science",
  "age": 20
}
```

### 2. Get Student by ID
- **Method**: GET
- **Endpoint**: `/api/students/{id}`
- **Status Code**: 200 OK

**Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "course": "Computer Science",
  "age": 20
}
```

### 3. Get All Students (Simple)
- **Method**: GET
- **Endpoint**: `/api/students`
- **Status Code**: 200 OK

**Response:**
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "course": "Computer Science",
    "age": 20
  }
]
```

### 4. Get All Students (Paginated & Sorted)
- **Method**: GET
- **Endpoint**: `/api/students?paginated=true&page=0&size=10&sortBy=name&direction=asc`
- **Status Code**: 200 OK

**Query Parameters:**
- `paginated`: Enable pagination (true/false)
- `page`: Page number (0-indexed)
- `size`: Number of items per page
- `sortBy`: Field to sort by (name, email, course, age, id)
- `direction`: Sort direction (asc/desc)

**Response:**
```json
{
  "students": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john.doe@example.com",
      "course": "Computer Science",
      "age": 20
    }
  ],
  "currentPage": 0,
  "totalItems": 10,
  "totalPages": 1
}
```

### 5. Update Student
- **Method**: PUT
- **Endpoint**: `/api/students/{id}`
- **Status Code**: 200 OK

**Request Body:**
```json
{
  "name": "John Updated",
  "email": "john.updated@example.com",
  "course": "Software Engineering",
  "age": 21
}
```

### 6. Delete Student
- **Method**: DELETE
- **Endpoint**: `/api/students/{id}`
- **Status Code**: 200 OK

**Response:**
```json
{
  "message": "Student deleted successfully"
}
```

### 7. Search Students
- **Method**: GET
- **Endpoint**: `/api/students/search`
- **Status Code**: 200 OK

**Query Parameters:**
- `name`: Search by name (partial match, case-insensitive)
- `course`: Search by course (partial match, case-insensitive)
- `keyword`: Search by name or course (partial match, case-insensitive)
- `page`: Page number (0-indexed)
- `size`: Number of items per page
- `sortBy`: Field to sort by
- `direction`: Sort direction (asc/desc)

**Examples:**
```
GET /api/students/search?name=John&page=0&size=5
GET /api/students/search?course=Computer&page=0&size=10&sortBy=name
GET /api/students/search?keyword=science&page=0&size=10
```

## Validation Rules

- **Name**: Required, 2-100 characters
- **Email**: Required, valid email format, unique
- **Course**: Required, 2-100 characters
- **Age**: Required, must be at least 18, maximum 100

## Error Responses

### 400 Bad Request (Validation Error)
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed. Check 'validationErrors' for details.",
  "path": "/api/students",
  "validationErrors": {
    "email": "Email must be valid",
    "age": "Age must be at least 18"
  }
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Student not found with id: '999'",
  "path": "/api/students/999"
}
```

### 409 Conflict (Duplicate Email)
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Student already exists with email: 'john@example.com'",
  "path": "/api/students"
}
```

## Swagger Documentation

Access the interactive API documentation at:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

Swagger provides:
- Complete API documentation
- Interactive testing interface
- Request/response examples
- Schema definitions

## H2 Console

When using H2 database, access the console at:

- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: jdbc:h2:mem:studentdb
- **Username**: sa
- **Password**: (leave empty)

## Testing with Postman

1. Import the API endpoints into Postman
2. Set the base URL: `http://localhost:8080/api/students`
3. Test each endpoint with sample data

### Sample Test Flow:

1. **Create a student** (POST)
2. **Get all students** (GET)
3. **Get student by ID** (GET)
4. **Update student** (PUT)
5. **Search students** (GET with query params)
6. **Delete student** (DELETE)

## Database Configuration

### Switching Between Databases

Change the active profile in `application.properties`:

```properties
spring.profiles.active=h2      # For H2
spring.profiles.active=mysql   # For MySQL
spring.profiles.active=postgresql  # For PostgreSQL
```

Or set environment variable:
```bash
export SPRING_PROFILES_ACTIVE=mysql
```

## Building and Running Tests

```bash
# Run tests
mvn test

# Build JAR file
mvn clean package

# Run JAR file
java -jar target/student-management-api-1.0.0.jar
```

## Docker Commands

```bash
# Build Docker image
docker build -t student-management-api .

# Run with H2
docker-compose up student-api-h2

# Run with MySQL
docker-compose --profile mysql up

# Run with PostgreSQL
docker-compose --profile postgres up

# Stop all containers
docker-compose down

# Remove volumes
docker-compose down -v
```

## Troubleshooting

### Port Already in Use
If port 8080 is already in use, change the port in `application.properties`:
```properties
server.port=8081
```

### Database Connection Issues
- Verify database is running
- Check credentials in application properties
- Ensure database exists
- Check firewall settings

### Maven Build Issues
```bash
# Clean and rebuild
mvn clean install -U

# Skip tests if needed
mvn clean install -DskipTests
```

## Project Highlights

✅ Spring Boot 3.2.0
✅ Service Layer Architecture
✅ Input Validation with proper error messages
✅ Global Exception Handling
✅ Proper HTTP Status Codes
✅ Swagger/OpenAPI Documentation
✅ Pagination & Sorting
✅ Search Functionality
✅ Docker Support
✅ Multiple Database Support
✅ RESTful API Best Practices

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the Apache License 2.0

## Contact

For questions or support, please contact: support@studentmanagement.com

---

**Note**: This project is created as part of a practical assignment for demonstrating Spring Boot REST API development skills.
