# Quick Start Guide - Student Management API

Get up and running in 5 minutes!

## Prerequisites Check

```bash
# Check Java version (need 17+)
java -version

# Check Maven version
mvn -version

# Check Docker (optional)
docker --version
```

## Option 1: Run with H2 (Fastest - 2 minutes)

```bash
# 1. Clone and navigate
cd student-management-api

# 2. Run directly
mvn spring-boot:run

# 3. Open browser
# Swagger UI: http://localhost:8080/swagger-ui.html
# H2 Console: http://localhost:8080/h2-console
```

Done! ✅

## Option 2: Run with Docker (3 minutes)

```bash
# 1. Build and run
docker-compose up student-api-h2

# 2. Open browser
# Swagger UI: http://localhost:8080/swagger-ui.html
```

Done! ✅

## Quick Test with curl

```bash
# Create a student
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "course": "Computer Science",
    "age": 20
  }'

# Get all students
curl http://localhost:8080/api/students

# Get student by ID
curl http://localhost:8080/api/students/1
```

## Quick Test with Swagger

1. Open: http://localhost:8080/swagger-ui.html
2. Click on "POST /api/students"
3. Click "Try it out"
4. Enter student data
5. Click "Execute"
6. See the response!

## Switch to MySQL (Optional)

```bash
# 1. Start MySQL
sudo systemctl start mysql

# 2. Create database
mysql -u root -p
> CREATE DATABASE studentdb;
> EXIT;

# 3. Run with MySQL profile
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## Common Commands

```bash
# Build project
mvn clean install

# Run tests
mvn test

# Build JAR
mvn clean package

# Run JAR
java -jar target/student-management-api-1.0.0.jar

# Build Docker image
docker build -t student-api .

# Run Docker container
docker run -p 8080:8080 student-api
```

## Troubleshooting

### Port 8080 in use?
```bash
# Find process
lsof -i :8080

# Kill process
kill -9 <PID>

# Or change port in application.properties
server.port=8081
```

### Maven dependencies not downloading?
```bash
# Force update
mvn clean install -U
```

### Can't connect to database?
```bash
# Check if MySQL is running
sudo systemctl status mysql

# Start MySQL
sudo systemctl start mysql
```

## Important URLs

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **H2 Console**: http://localhost:8080/h2-console
- **Base API**: http://localhost:8080/api/students

## Default Credentials

### H2 Database
- URL: `jdbc:h2:mem:studentdb`
- Username: `sa`
- Password: (empty)

### MySQL (in docker-compose)
- Host: `localhost:3306`
- Database: `studentdb`
- Username: `root`
- Password: `root`

### PostgreSQL (in docker-compose)
- Host: `localhost:5432`
- Database: `studentdb`
- Username: `postgres`
- Password: `postgres`

## Next Steps

1. ✅ Read [README.md](README.md) for detailed documentation
2. ✅ Check [TESTING_GUIDE.md](TESTING_GUIDE.md) for testing instructions
3. ✅ Import [Postman Collection](Student-Management-API.postman_collection.json)
4. ✅ Explore Swagger UI
5. ✅ Test all endpoints

## Student Entity Structure

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "course": "Computer Science",
  "age": 20
}
```

## API Endpoints Quick Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/students | Create student |
| GET | /api/students | Get all students |
| GET | /api/students/{id} | Get student by ID |
| PUT | /api/students/{id} | Update student |
| DELETE | /api/students/{id} | Delete student |
| GET | /api/students/search | Search students |

## Need Help?

Check the main [README.md](README.md) for:
- Detailed API documentation
- Error response formats
- Validation rules
- Advanced features
- Docker deployment

---

Ready to code! 🚀
