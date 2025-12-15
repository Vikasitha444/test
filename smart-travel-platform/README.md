# Smart Travel Booking Platform

## Assignment 02: Inter-Service Communication Using REST API, Feign Client, and WebClient

A distributed travel booking backend platform containing 6 microservices built with Spring Boot 3+ and Java 17+.

---

## Architecture Diagram

```
                                    +------------------+
                                    |                  |
                                    |  Payment Service |
                                    |    (Port 8084)   |
                                    |                  |
                                    +--------+---------+
                                             |
                                             | WebClient
                                             v
+------------------+    WebClient    +------------------+    Feign Client    +------------------+
|                  |<----------------|                  |------------------>|                  |
|  User Service    |                 |  Booking Service |                   |  Flight Service  |
|   (Port 8081)    |                 |    (Port 8080)   |                   |   (Port 8082)    |
|                  |                 |   ORCHESTRATOR   |                   |                  |
+------------------+                 +--------+---------+                   +------------------+
                                             |
                                             | Feign Client
                                             v
+------------------+    WebClient    +------------------+
|                  |<----------------|                  |
|  Notification    |                 |  Hotel Service   |
|    Service       |                 |   (Port 8083)    |
|   (Port 8085)    |                 |                  |
+------------------+                 +------------------+
```

---

## Communication Flow

| From Service     | To Service          | Method       |
|------------------|---------------------|--------------|
| Booking Service  | User Service        | WebClient    |
| Booking Service  | Flight Service      | Feign Client |
| Booking Service  | Hotel Service       | Feign Client |
| Booking Service  | Notification Service| WebClient    |
| Payment Service  | Booking Service     | WebClient    |

---

## Services Overview

| Service              | Port | Description                           |
|----------------------|------|---------------------------------------|
| Booking Service      | 8080 | Main orchestrator for booking flow    |
| User Service         | 8081 | Manages user data                     |
| Flight Service       | 8082 | Manages flight data and availability  |
| Hotel Service        | 8083 | Manages hotel data and availability   |
| Payment Service      | 8084 | Processes payments                    |
| Notification Service | 8085 | Sends notifications to users          |

---

## Booking Flow Summary

1. User sends a booking request to Booking Service
2. Booking Service validates user via **WebClient** (User Service)
3. Booking Service checks flight availability via **Feign Client** (Flight Service)
4. Booking Service checks hotel availability via **Feign Client** (Hotel Service)
5. Booking Service calculates total cost (flight price + hotel price)
6. Booking Service stores booking as **PENDING**
7. Booking Service sends notification via **WebClient** (Notification Service)
8. Payment Service processes payment and confirms booking via **WebClient**
9. Booking status updates to **CONFIRMED**

---

## Project Structure

```
smart-travel-platform/
├── booking-service/
│   └── src/main/java/com/travel/bookingservice/
│       ├── client/           # Feign Clients
│       ├── config/           # WebClient Configuration
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── exception/
│       ├── repository/
│       └── service/
├── user-service/
├── flight-service/
├── hotel-service/
├── payment-service/
├── notification-service/
├── Smart-Travel-Platform.postman_collection.json
└── README.md
```

---

## Technologies Used

- Java 17
- Spring Boot 3.2.0
- Spring Cloud OpenFeign
- Spring WebFlux (WebClient)
- Spring Data JPA
- H2 Database (In-Memory)
- Lombok

---

## How to Run

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Step-by-Step Instructions

**1. Clone the repository**

**2. Start each service in a separate terminal**

Open 6 terminal windows and run each service:

**Terminal 1 - User Service:**
```bash
cd smart-travel-platform/user-service
./mvnw spring-boot:run
```

**Terminal 2 - Flight Service:**
```bash
cd smart-travel-platform/flight-service
./mvnw spring-boot:run
```

**Terminal 3 - Hotel Service:**
```bash
cd smart-travel-platform/hotel-service
./mvnw spring-boot:run
```

**Terminal 4 - Payment Service:**
```bash
cd smart-travel-platform/payment-service
./mvnw spring-boot:run
```

**Terminal 5 - Notification Service:**
```bash
cd smart-travel-platform/notification-service
./mvnw spring-boot:run
```

**Terminal 6 - Booking Service (Start this LAST):**
```bash
cd smart-travel-platform/booking-service
./mvnw spring-boot:run
```

**Note:** Start Booking Service last because it depends on other services.

---

## Testing the Application

### Using Postman

1. Import `Smart-Travel-Platform.postman_collection.json` into Postman
2. Test each service endpoint

### Sample Booking Request

**POST** `http://localhost:8080/api/bookings`

```json
{
    "userId": 1,
    "flightId": 1,
    "hotelId": 1,
    "travelDate": "2025-01-10"
}
```

**Expected Response:**
```json
{
    "success": true,
    "message": "Booking created successfully",
    "data": {
        "id": 1,
        "userId": 1,
        "flightId": 1,
        "hotelId": 1,
        "travelDate": "2025-01-10",
        "totalCost": 400.0,
        "status": "PENDING",
        "createdAt": "2025-01-10T10:00:00",
        "updatedAt": "2025-01-10T10:00:00"
    }
}
```

---

## API Endpoints

### Booking Service (Port 8080)
| Method | Endpoint                      | Description            |
|--------|-------------------------------|------------------------|
| POST   | /api/bookings                 | Create new booking     |
| GET    | /api/bookings                 | Get all bookings       |
| GET    | /api/bookings/{id}            | Get booking by ID      |
| GET    | /api/bookings/user/{userId}   | Get bookings by user   |
| PUT    | /api/bookings/{id}/confirm    | Confirm booking        |
| PUT    | /api/bookings/{id}/cancel     | Cancel booking         |

### User Service (Port 8081)
| Method | Endpoint                  | Description        |
|--------|---------------------------|--------------------|
| POST   | /api/users                | Create new user    |
| GET    | /api/users                | Get all users      |
| GET    | /api/users/{id}           | Get user by ID     |
| GET    | /api/users/{id}/validate  | Validate user      |

### Flight Service (Port 8082)
| Method | Endpoint                       | Description              |
|--------|--------------------------------|--------------------------|
| POST   | /api/flights                   | Create new flight        |
| GET    | /api/flights                   | Get all flights          |
| GET    | /api/flights/{id}              | Get flight by ID         |
| GET    | /api/flights/{id}/availability | Check flight availability|

### Hotel Service (Port 8083)
| Method | Endpoint                      | Description             |
|--------|-------------------------------|-------------------------|
| POST   | /api/hotels                   | Create new hotel        |
| GET    | /api/hotels                   | Get all hotels          |
| GET    | /api/hotels/{id}              | Get hotel by ID         |
| GET    | /api/hotels/{id}/availability | Check hotel availability|

### Payment Service (Port 8084)
| Method | Endpoint                       | Description              |
|--------|--------------------------------|--------------------------|
| POST   | /api/payments                  | Process payment          |
| GET    | /api/payments                  | Get all payments         |
| GET    | /api/payments/{id}             | Get payment by ID        |
| GET    | /api/payments/booking/{id}     | Get payment by booking   |

### Notification Service (Port 8085)
| Method | Endpoint                           | Description                |
|--------|------------------------------------|----------------------------|
| POST   | /api/notifications                 | Send notification          |
| GET    | /api/notifications                 | Get all notifications      |
| GET    | /api/notifications/{id}            | Get notification by ID     |
| GET    | /api/notifications/user/{userId}   | Get notifications by user  |
| GET    | /api/notifications/booking/{id}    | Get notifications by booking|

---

## Pre-loaded Test Data

### Users (User Service)
| ID | Name       | Email              |
|----|------------|--------------------|
| 1  | John Doe   | john@example.com   |
| 2  | Jane Smith | jane@example.com   |
| 3  | Bob Wilson | bob@example.com    |

### Flights (Flight Service)
| ID | Flight No | Origin   | Destination | Price  | Available |
|----|-----------|----------|-------------|--------|-----------|
| 1  | FL001     | Colombo  | Singapore   | $250   | Yes       |
| 2  | FL002     | Colombo  | Dubai       | $350   | Yes       |
| 3  | FL003     | Singapore| Tokyo       | $400   | No        |

### Hotels (Hotel Service)
| ID | Name         | Location  | Price/Night | Available |
|----|--------------|-----------|-------------|-----------|
| 1  | Grand Hotel  | Singapore | $150        | Yes       |
| 2  | Beach Resort | Maldives  | $300        | Yes       |
| 3  | City Inn     | Dubai     | $100        | No        |

---

## Key Implementation Details

### WebClient Usage (Booking Service)
```java
// Validating user via WebClient
ApiResponse<UserDTO> userResponse = webClientBuilder.build()
    .get()
    .uri(userServiceUrl + "/api/users/" + request.getUserId())
    .retrieve()
    .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserDTO>>() {})
    .block();
```

### Feign Client Usage (Booking Service)
```java
// FlightServiceClient.java
@FeignClient(name = "flight-service", url = "${flight.service.url}")
public interface FlightServiceClient {
    @GetMapping("/api/flights/{id}/availability")
    ApiResponse<Boolean> checkFlightAvailability(@PathVariable("id") Long id);
}
```

---

## Author

Assignment 02 - Smart Travel Booking Platform
