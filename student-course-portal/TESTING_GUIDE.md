# Testing Guide - Student Course Portal

## Complete Testing Workflow

This guide provides step-by-step instructions for testing all microservices with expected outputs.

## Prerequisites

1. All 5 services must be running on their respective ports
2. Postman installed and collection imported

## Test Sequence

### Phase 1: Create Test Data

#### 1.1 Create Students

**Request:** `POST http://localhost:8081/students`
```json
{
  "name": "Alice Johnson",
  "email": "alice.johnson@sjp.ac.lk",
  "department": "ICT",
  "phone": "0771234567"
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "name": "Alice Johnson",
  "email": "alice.johnson@sjp.ac.lk",
  "department": "ICT",
  "phone": "0771234567"
}
```

Create another student:
```json
{
  "name": "Bob Smith",
  "email": "bob.smith@sjp.ac.lk",
  "department": "Computer Science",
  "phone": "0777654321"
}
```

#### 1.2 Create Courses

**Request:** `POST http://localhost:8082/courses`
```json
{
  "courseCode": "ITS4243",
  "courseName": "Micro Services and Cloud Computing",
  "description": "Advanced course on microservices architecture",
  "credits": 3,
  "instructor": "Dr. Smith"
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "courseCode": "ITS4243",
  "courseName": "Micro Services and Cloud Computing",
  "description": "Advanced course on microservices architecture",
  "credits": 3,
  "instructor": "Dr. Smith"
}
```

Create another course:
```json
{
  "courseCode": "ITS4101",
  "courseName": "Database Management Systems",
  "description": "Introduction to database systems",
  "credits": 4,
  "instructor": "Prof. Johnson"
}
```

### Phase 2: Test Retrieval Operations

#### 2.1 Get All Students

**Request:** `GET http://localhost:8081/students`

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Alice Johnson",
    "email": "alice.johnson@sjp.ac.lk",
    "department": "ICT",
    "phone": "0771234567"
  },
  {
    "id": 2,
    "name": "Bob Smith",
    "email": "bob.smith@sjp.ac.lk",
    "department": "Computer Science",
    "phone": "0777654321"
  }
]
```

#### 2.2 Get All Courses

**Request:** `GET http://localhost:8082/courses`

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "courseCode": "ITS4243",
    "courseName": "Micro Services and Cloud Computing",
    "description": "Advanced course on microservices architecture",
    "credits": 3,
    "instructor": "Dr. Smith"
  },
  {
    "id": 2,
    "courseCode": "ITS4101",
    "courseName": "Database Management Systems",
    "description": "Introduction to database systems",
    "credits": 4,
    "instructor": "Prof. Johnson"
  }
]
```

### Phase 3: Test Inter-Service Communication (Enrollment)

#### 3.1 Successful Enrollment

**Request:** `POST http://localhost:8083/enroll`
```json
{
  "studentId": 1,
  "courseId": 1
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "studentId": 1,
  "courseId": 1,
  "enrollmentDate": "2025-11-20T14:30:45.123",
  "status": "ACTIVE"
}
```

**Check Notification Service Console:**
You should see output like:
```
========================================
ENROLLMENT NOTIFICATION
========================================
Timestamp: 2025-11-20 14:30:45
Student 1 enrolled into Course 1
Message: Enrollment successful
========================================
```

#### 3.2 Test Validation - Invalid Student ID

**Request:** `POST http://localhost:8083/enroll`
```json
{
  "studentId": 999,
  "courseId": 1
}
```

**Expected Response (400 Bad Request):**
```json
{
  "timestamp": "2025-11-20T14:35:00",
  "message": "Student not found with id: 999",
  "status": 400
}
```

#### 3.3 Test Validation - Invalid Course ID

**Request:** `POST http://localhost:8083/enroll`
```json
{
  "studentId": 1,
  "courseId": 999
}
```

**Expected Response (400 Bad Request):**
```json
{
  "timestamp": "2025-11-20T14:36:00",
  "message": "Course not found with id: 999",
  "status": 400
}
```

#### 3.4 Get Student Enrollments

**Request:** `GET http://localhost:8083/enrollments/student/1`

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "studentId": 1,
    "courseId": 1,
    "enrollmentDate": "2025-11-20T14:30:45.123",
    "status": "ACTIVE"
  }
]
```

### Phase 4: Test Result Service

#### 4.1 Create Result

**Request:** `POST http://localhost:8084/results`
```json
{
  "studentId": 1,
  "courseId": 1,
  "grade": "A",
  "marks": 88.5
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "studentId": 1,
  "courseId": 1,
  "grade": "A",
  "marks": 88.5,
  "recordedDate": "2025-11-20T14:40:00.123"
}
```

#### 4.2 Get Results by Student

**Request:** `GET http://localhost:8084/results/student/1`

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "studentId": 1,
    "courseId": 1,
    "grade": "A",
    "marks": 88.5,
    "recordedDate": "2025-11-20T14:40:00.123"
  }
]
```

#### 4.3 Update Result

**Request:** `PUT http://localhost:8084/results/1`
```json
{
  "grade": "A+",
  "marks": 95.0
}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "studentId": 1,
  "courseId": 1,
  "grade": "A+",
  "marks": 95.0,
  "recordedDate": "2025-11-20T14:40:00.123"
}
```

### Phase 5: Test Notification Service Directly

**Request:** `POST http://localhost:8085/notify/enrollment`
```json
{
  "studentId": 1,
  "courseId": 1,
  "message": "Direct notification test"
}
```

**Expected Response (200 OK):**
```json
{
  "status": "success",
  "message": "Notification sent successfully"
}
```

**Check Console Output:**
```
========================================
ENROLLMENT NOTIFICATION
========================================
Timestamp: 2025-11-20 14:45:00
Student 1 enrolled into Course 1
Message: Direct notification test
========================================
```

## Database Verification

### Student Service Database (Port 8081)

1. Open: http://localhost:8081/h2-console
2. JDBC URL: `jdbc:h2:mem:studentdb`
3. Username: `sa`
4. Password: (leave empty)
5. Run query: `SELECT * FROM STUDENTS`

**Expected Result:**
```
ID | NAME           | EMAIL                    | DEPARTMENT        | PHONE
1  | Alice Johnson  | alice.johnson@sjp.ac.lk  | ICT              | 0771234567
2  | Bob Smith      | bob.smith@sjp.ac.lk      | Computer Science | 0777654321
```

### Course Service Database (Port 8082)

1. Open: http://localhost:8082/h2-console
2. JDBC URL: `jdbc:h2:mem:coursedb`
3. Run query: `SELECT * FROM COURSES`

**Expected Result:**
```
ID | COURSE_CODE | COURSE_NAME                          | DESCRIPTION                    | CREDITS | INSTRUCTOR
1  | ITS4243     | Micro Services and Cloud Computing   | Advanced course...             | 3       | Dr. Smith
2  | ITS4101     | Database Management Systems          | Introduction to database...    | 4       | Prof. Johnson
```

### Enrollment Service Database (Port 8083)

1. Open: http://localhost:8083/h2-console
2. JDBC URL: `jdbc:h2:mem:enrollmentdb`
3. Run query: `SELECT * FROM ENROLLMENTS`

**Expected Result:**
```
ID | STUDENT_ID | COURSE_ID | ENROLLMENT_DATE         | STATUS
1  | 1          | 1         | 2025-11-20 14:30:45     | ACTIVE
```

### Result Service Database (Port 8084)

1. Open: http://localhost:8084/h2-console
2. JDBC URL: `jdbc:h2:mem:resultdb`
3. Run query: `SELECT * FROM RESULTS`

**Expected Result:**
```
ID | STUDENT_ID | COURSE_ID | GRADE | MARKS | RECORDED_DATE
1  | 1          | 1         | A+    | 95.0  | 2025-11-20 14:40:00
```

## Complete Test Scenario

### Scenario: Student Enrollment and Grading Flow

1. **Create Student "Carol Davis"**
   - POST http://localhost:8081/students
   - Verify student ID received (e.g., ID = 3)

2. **Create Course "Web Development"**
   - POST http://localhost:8082/courses
   - Verify course ID received (e.g., ID = 3)

3. **Enroll Carol in Web Development**
   - POST http://localhost:8083/enroll with studentId=3, courseId=3
   - Verify enrollment created
   - Check notification in console

4. **View Carol's Enrollments**
   - GET http://localhost:8083/enrollments/student/3
   - Verify enrollment appears

5. **Add Result for Carol**
   - POST http://localhost:8084/results with studentId=3, courseId=3, grade="B+", marks=82.0
   - Verify result created

6. **View Carol's Results**
   - GET http://localhost:8084/results/student/3
   - Verify result appears with correct grade

7. **Verify in Databases**
   - Check students table for Carol
   - Check courses table for Web Development
   - Check enrollments table for the enrollment record
   - Check results table for the grade

## Error Testing

### 1. Invalid Email Format (Student Service)

**Request:** `POST http://localhost:8081/students`
```json
{
  "name": "Test User",
  "email": "invalid-email",
  "department": "ICT",
  "phone": "0771234567"
}
```

**Expected:** 400 Bad Request with validation error

### 2. Missing Required Fields (Course Service)

**Request:** `POST http://localhost:8082/courses`
```json
{
  "courseCode": "ITS5000"
}
```

**Expected:** 400 Bad Request with validation errors

### 3. Non-existent Resource (Student Service)

**Request:** `GET http://localhost:8081/students/999`

**Expected:** 404 Not Found
```json
{
  "timestamp": "2025-11-20T15:00:00",
  "message": "Student not found with id: 999",
  "status": 404
}
```

## Screenshots Required for Submission

1. **All 5 services running** - Terminal windows showing each service started
2. **H2 Console access** - Screenshot of each database with data
3. **Postman requests and responses:**
   - POST /students (Create student)
   - POST /courses (Create course)
   - POST /enroll (Enrollment with inter-service communication)
   - GET /enrollments/student/{id}
   - POST /results
   - GET /results/student/{id}
4. **Notification Service console** - Showing enrollment notification output
5. **Database tables** - Screenshots of all 4 databases with data

## Performance Testing

Test with multiple concurrent requests:
- Create 10 students
- Create 5 courses
- Enroll all students in all courses (50 enrollments)
- Verify all notifications are sent
- Add results for all enrollments

All services should handle these operations without errors.

## Troubleshooting Common Issues

### Issue: Enrollment fails with "Connection refused"
**Solution:** Ensure Student Service (8081) and Course Service (8082) are running before testing Enrollment Service

### Issue: No notification appears
**Solution:** Check Notification Service (8085) is running and check its console output

### Issue: H2 Console shows empty tables
**Solution:** Verify you're using the correct JDBC URL for each service

### Issue: Port already in use
**Solution:** Stop any conflicting services or change ports in application.properties

## Success Criteria

✅ All 5 services start without errors
✅ Each service accessible on correct port
✅ CRUD operations work for Student and Course services
✅ Enrollment validates student and course IDs correctly
✅ Enrollment sends notification successfully
✅ Results can be created and retrieved
✅ All databases show correct data
✅ Error handling works as expected
✅ Inter-service communication functions properly
