# Student Course Portal - Microservices System

A complete microservices-based Student Course Portal system built with Spring Boot 3.2.0 for the ITS4243 - Micro Services and Cloud Computing course at the University of Sri Jayewardenepura.

## System Overview

This system consists of 5 independent microservices:

1. **Student Service** (Port 8081) - Manages student information
2. **Course Service** (Port 8082) - Manages course catalog
3. **Enrollment Service** (Port 8083) - Handles student enrollments with inter-service communication
4. **Result Service** (Port 8084) - Manages student grades and results
5. **Notification Service** (Port 8085) - Sends enrollment notifications

## Architecture

```
┌─────────────────┐     ┌─────────────────┐
│ Student Service │     │ Course Service  │
│   Port: 8081    │     │   Port: 8082    │
│   DB: studentdb │     │   DB: coursedb  │
└────────┬────────┘     └────────┬────────┘
         │                       │
         │  Validates IDs        │
         └───────┬───────────────┘
                 │
         ┌───────▼────────┐
         │  Enrollment    │
         │    Service     │
         │  Port: 8083    │
         │DB: enrollmentdb│
         └───────┬────────┘
                 │
                 │ Sends notification
                 │
         ┌───────▼────────┐
         │  Notification  │
         │    Service     │
         │  Port: 8085    │
         └────────────────┘

┌─────────────────┐
│ Result Service  │
│  Port: 8084     │
│  DB: resultdb   │
└─────────────────┘
```

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Postman (for API testing)
- Any IDE (IntelliJ IDEA, Eclipse, VS Code)

## Project Structure

```
student-course-portal/
├── student-service/
│   ├── src/main/java/com/studentportal/student/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   └── exception/
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── course-service/
│   └── [similar structure]
├── enrollment-service/
│   └── [similar structure + client/ for inter-service communication]
├── result-service/
│   └── [similar structure]
├── notification-service/
│   └── [similar structure]
└── README.md
```

## Running the Services

### Step 1: Navigate to Project Directory

```bash
cd student-course-portal
```

### Step 2: Start Each Service (In Separate Terminals)

**Terminal 1 - Student Service:**
```bash
cd student-service
mvn clean install
mvn spring-boot:run
```

**Terminal 2 - Course Service:**
```bash
cd course-service
mvn clean install
mvn spring-boot:run
```

**Terminal 3 - Enrollment Service:**
```bash
cd enrollment-service
mvn clean install
mvn spring-boot:run
```

**Terminal 4 - Result Service:**
```bash
cd result-service
mvn clean install
mvn spring-boot:run
```

**Terminal 5 - Notification Service:**
```bash
cd notification-service
mvn clean install
mvn spring-boot:run
```

### Alternative: Run Without Installing Dependencies Each Time

After the first `mvn clean install`, you can just use:
```bash
mvn spring-boot:run
```

## Service Details

### 1. Student Service (Port 8081)

**Database:** H2 (in-memory) - `studentdb`
**H2 Console:** http://localhost:8081/h2-console

**API Endpoints:**
- `GET /students` - Get all students
- `POST /students` - Create new student
- `GET /students/{id}` - Get student by ID
- `PUT /students/{id}` - Update student
- `DELETE /students/{id}` - Delete student

**Sample Request (POST /students):**
```json
{
  "name": "John Doe",
  "email": "john.doe@sjp.ac.lk",
  "department": "ICT",
  "phone": "0771234567"
}
```

### 2. Course Service (Port 8082)

**Database:** H2 (in-memory) - `coursedb`
**H2 Console:** http://localhost:8082/h2-console

**API Endpoints:**
- `GET /courses` - Get all courses
- `POST /courses` - Create new course
- `GET /courses/{id}` - Get course by ID
- `PUT /courses/{id}` - Update course
- `DELETE /courses/{id}` - Delete course

**Sample Request (POST /courses):**
```json
{
  "courseCode": "ITS4243",
  "courseName": "Micro Services and Cloud Computing",
  "description": "Advanced course on microservices architecture",
  "credits": 3,
  "instructor": "Dr. Smith"
}
```

### 3. Enrollment Service (Port 8083)

**Database:** H2 (in-memory) - `enrollmentdb`
**H2 Console:** http://localhost:8083/h2-console

**Features:**
- Validates student ID by calling Student Service
- Validates course ID by calling Course Service
- Sends notification to Notification Service after successful enrollment

**API Endpoints:**
- `POST /enroll` - Enroll student in course
- `GET /enrollments/student/{id}` - Get all enrollments for a student
- `GET /enrollments` - Get all enrollments

**Sample Request (POST /enroll):**
```json
{
  "studentId": 1,
  "courseId": 1
}
```

### 4. Result Service (Port 8084)

**Database:** H2 (in-memory) - `resultdb`
**H2 Console:** http://localhost:8084/h2-console

**API Endpoints:**
- `POST /results` - Create new result
- `GET /results/student/{id}` - Get all results for a student
- `GET /results` - Get all results
- `GET /results/{id}` - Get result by ID
- `PUT /results/{id}` - Update result
- `DELETE /results/{id}` - Delete result

**Sample Request (POST /results):**
```json
{
  "studentId": 1,
  "courseId": 1,
  "grade": "A",
  "marks": 85.5
}
```

### 5. Notification Service (Port 8085)

**No Database Required** - Stateless service

**API Endpoints:**
- `POST /notify/enrollment` - Send enrollment notification

**Sample Request (POST /notify/enrollment):**
```json
{
  "studentId": 1,
  "courseId": 1,
  "message": "Enrollment successful"
}
```

**Output:** Prints notification to console:
```
========================================
ENROLLMENT NOTIFICATION
========================================
Timestamp: 2025-11-20 14:30:45
Student 1 enrolled into Course 1
Message: Enrollment successful
========================================
```

## Testing the System

### Complete Test Flow

1. **Create a Student:**
   ```
   POST http://localhost:8081/students
   Body: { "name": "Alice Johnson", "email": "alice@sjp.ac.lk", "department": "ICT", "phone": "0771111111" }
   ```

2. **Create a Course:**
   ```
   POST http://localhost:8082/courses
   Body: { "courseCode": "ITS4243", "courseName": "Microservices", "credits": 3, "instructor": "Dr. Smith" }
   ```

3. **Enroll Student in Course:**
   ```
   POST http://localhost:8083/enroll
   Body: { "studentId": 1, "courseId": 1 }
   ```
   - This will validate both IDs
   - Create enrollment record
   - Send notification (check notification service console)

4. **View Student Enrollments:**
   ```
   GET http://localhost:8083/enrollments/student/1
   ```

5. **Add Result:**
   ```
   POST http://localhost:8084/results
   Body: { "studentId": 1, "courseId": 1, "grade": "A", "marks": 88.5 }
   ```

6. **View Student Results:**
   ```
   GET http://localhost:8084/results/student/1
   ```

## Accessing H2 Databases

Each service has its own H2 in-memory database accessible via H2 Console:

- Student DB: http://localhost:8081/h2-console
  - JDBC URL: `jdbc:h2:mem:studentdb`
  - Username: `sa`
  - Password: (leave empty)

- Course DB: http://localhost:8082/h2-console
  - JDBC URL: `jdbc:h2:mem:coursedb`
  - Username: `sa`
  - Password: (leave empty)

- Enrollment DB: http://localhost:8083/h2-console
  - JDBC URL: `jdbc:h2:mem:enrollmentdb`
  - Username: `sa`
  - Password: (leave empty)

- Result DB: http://localhost:8084/h2-console
  - JDBC URL: `jdbc:h2:mem:resultdb`
  - Username: `sa`
  - Password: (leave empty)

## Inter-Service Communication

The Enrollment Service demonstrates REST-based inter-service communication:

1. **StudentServiceClient** - Validates student existence
2. **CourseServiceClient** - Validates course existence
3. **NotificationServiceClient** - Sends enrollment notifications

Configuration in `enrollment-service/src/main/resources/application.properties`:
```properties
student.service.url=http://localhost:8081
course.service.url=http://localhost:8082
notification.service.url=http://localhost:8085
```

## Technical Stack

- **Framework:** Spring Boot 3.2.0
- **Java Version:** 17
- **Build Tool:** Maven
- **Database:** H2 (in-memory)
- **ORM:** Spring Data JPA
- **Validation:** Spring Boot Validation
- **Documentation:** Spring REST Docs
- **Utilities:** Lombok

## Key Features Implemented

✅ 5 Independent Microservices
✅ Each service runs on separate port
✅ Each service has its own database
✅ REST API inter-service communication
✅ Proper JSON DTOs for request/response
✅ Complete CRUD operations
✅ Exception handling with custom error responses
✅ Request validation
✅ H2 console for database inspection

## Troubleshooting

### Port Already in Use
If you get "Port already in use" error:
- Check if another service is running on that port
- Stop the conflicting process or change the port in `application.properties`

### Service Not Connecting
- Ensure all dependent services are running (Student and Course services must be running before testing Enrollment)
- Check firewall settings
- Verify port numbers in application.properties

### Database Connection Issues
- H2 is in-memory, data is lost on restart
- Check H2 console URL matches the service port
- Ensure JDBC URL is correct

## API Testing with Postman

Import the provided Postman collection (`Student-Course-Portal.postman_collection.json`) for ready-to-use API requests for all services.

## Contributors

**Student Name:** [Your Name]
**Registration Number:** [Your Registration Number]
**Course:** ITS4243 - Micro Services and Cloud Computing
**University:** University of Sri Jayewardenepura

## License

This project is created for educational purposes as part of the ITS4243 course lab assessment.
