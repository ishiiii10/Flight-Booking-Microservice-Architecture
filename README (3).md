# ✈️ Flight Booking System – Microservices Architecture (Spring Boot + MongoDB + Eureka + API Gateway + Config Server)

## 🚀 Features
```
  Microservice                       Responsibilities
 ---------------------------------  --------------------------------------------------------
 Flight Service                     Search flights, add inventory, update seat count
 Booking Service                    Book tickets, cancel tickets, manage booking history
 API Gateway                        Single entry point for all external clients
 Eureka Server                      Service registry for discovery & load balancing
 Config Server                      Centralized configuration management for all services
 RabbitMQ                           Asynchronous communication for email notifications
 Notification Service               Sends emails on booking confirmation
 MongoDB Databases                  Independent DB per microservice (Booking & Flight)
 Resilience4j Circuit Breaker       Protection from downstream failures
 OpenFeign                          Declarative inter-service communication
```
## 🛠️ Tech Stack
```
  Layer                    Technology
 -----------------------  -------------------------------------------------------
 API Gateway              Spring Cloud Gateway
 Service Discovery        Eureka Server
 Config Management        Spring Cloud Config Server
 Flight Service           Spring Boot 3, Webmvc, MongoDB Reactive
 Booking Service          Spring Boot 3, Webmvc, MongoDB Reactive
 Messaging                RabbitMQ (AMQP)
 Inter-service Calls      OpenFeign + WebClient
 Resilience               Resilience4j Circuit Breaker & Retry
 Testing                  JUnit 5, Mockito, WebTestClient
 Language                 Java 17
 Build Tool               Maven                       
```
## 📂 Project Structure
<img width="409" height="222" alt="Screenshot 2025-12-01 at 11 52 32 PM" src="https://github.com/user-attachments/assets/1646a1ec-09c3-4b80-9207-0929b19d5c60" />
<img width="288" height="224" alt="Screenshot 2025-12-01 at 11 54 30 PM" src="https://github.com/user-attachments/assets/a8e52440-c1fe-4ce9-ac56-e21bfbad8207" />
<img width="332" height="448" alt="Screenshot 2025-12-01 at 11 53 18 PM" src="https://github.com/user-attachments/assets/4bd26dfb-7155-4150-a450-d316d2f471a8" />
<img width="321" height="314" alt="Screenshot 2025-12-01 at 11 55 35 PM" src="https://github.com/user-attachments/assets/71370364-567f-4328-88dc-b70a629b6a69" />
<img width="1246" height="444" alt="Screenshot 2025-12-01 at 11 55 15 PM" src="https://github.com/user-attachments/assets/6d9bc045-f2f2-4dd3-8ca0-2209b896b3f4" />
<img width="385" height="666" alt="Screenshot 2025-12-01 at 11 53 00 PM" src="https://github.com/user-attachments/assets/d116885e-bc90-462e-a3e8-abda00503fe5" />


## ⚙️ Setup & Installation
### 1️⃣ Clone the repository
git clone https://github.com/yourusername/flight-microservices.git
cd flight-microservices

### 2️⃣ Start MongoDB
Use local MongoDB (default port: 27017) or Atlas.

## 📌 Microservice Configuration
### Eureka Server — application.properties
server.port=8761
spring.application.name=eureka-server
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false

### Config Server — application.properties
server.port=8888
spring.application.name=config-server
spring.cloud.config.server.git.uri=https://github.com/ishiiii10/config-server

### Run the application
mvn spring-boot:run

### 🧪 Running Tests
mvn test
Test coverage includes:
Flight Services Tests
Booking Service Tests

## 🧑‍💻 Available API Endpoints
### ✈ Airline Management
```
 Method  Endpoint                        Description      
 ------  ------------------------------  ---------------- 
 `POST`  `/api/flight/airline`      Create airline   
 `GET`   `/api/flight/airline/all`  Get all airlines 
```

### 🛫 Flight Inventory
```
 Method                                                    Endpoint 
 --------------------------------------------------------  -------- 
 `POST` `/api/flight/airline/inventory` → Add flight           
 `POST` `/api/flight/search` → Search flights                  
```

### 🎟 Booking APIs
```
 Method    Endpoint                                    Purpose               
 --------  ------------------------------------------  --------------------- 
 `POST`    `/api/flight/booking/{flightId}`       Book ticket           
 `DELETE`  `/api/flight/booking/cancel/{pnr}`     Cancel booking        
 `GET`     `/api/flight/ticket/{pnr}`             Get ticket details    
 `GET`     `/api/flight/booking/history/{email}`  Fetch booking history 
```
### 🧰 Error Handling Examples
```
 Scenario                 Status             Message                                 
 -----------------------  -----------------  --------------------------------------- 
 Airline already exists   `409 CONFLICT`     `"Airline with code AI already exists"` 
 Flight not found         `404 NOT FOUND`    `"Flight not found"`                    
 Seats unavailable        `400 BAD REQUEST`  `"Not enough available seats"`          
 Passenger phone invalid  `400 BAD REQUEST`  `"Contact number must be 10 digits"`    
```
## 📌 Postman APIs

### Add Flight to Inventory
<img width="467" height="321" alt="Screenshot 2025-12-02 at 2 31 04 AM" src="https://github.com/user-attachments/assets/5d84ee17-5ea5-42e0-81e7-56682b5359ea" />


### Search Flight
<img width="470" height="384" alt="Screenshot 2025-12-02 at 2 31 26 AM" src="https://github.com/user-attachments/assets/8d552ee3-2b73-4519-918a-74e1fb7691ab" />




### Book Flight
<img width="475" height="343" alt="Screenshot 2025-12-02 at 2 31 56 AM" src="https://github.com/user-attachments/assets/74f94439-e93d-46da-9d66-36dcad2fff74" />




### Get Ticket
<img width="466" height="383" alt="Screenshot 2025-12-02 at 2 32 09 AM" src="https://github.com/user-attachments/assets/42eafb37-444a-44cb-9b54-8fc1b31085a5" />



### Get history
<img width="471" height="386" alt="Screenshot 2025-12-02 at 2 32 57 AM" src="https://github.com/user-attachments/assets/96e53a6d-888b-4e01-a233-ef7de1c68b00" />



### Cancel Booking
<img width="467" height="360" alt="Screenshot 2025-12-02 at 2 33 10 AM" src="https://github.com/user-attachments/assets/8547c31d-e776-46ad-bb2b-a78ab699f87c" />


## 📌 Main Micro Architecture
<img width="800" height="400" alt="image" src="https://github.com/user-attachments/assets/ef39c8a5-b82e-48e0-a47d-02dfab5d73b4" />
<img width="500" height="355" alt="image" src="https://github.com/user-attachments/assets/1644b222-dcac-49d1-8446-4fe42ff9a452" />


## 📌 Jacoco Report for Flight Service
<img width="1470" height="575" alt="Screenshot 2025-12-02 at 1 43 48 AM" src="https://github.com/user-attachments/assets/453ce36a-6612-47f2-8628-e41f76ebb35c" />

## 📌 Jacoco Report for Booking Service
<img width="1470" height="628" alt="Screenshot 2025-12-02 at 1 40 53 AM" src="https://github.com/user-attachments/assets/ff62dd6c-aa17-4e1e-bd1a-da2639c262b7" />


## 📌 Sonar Qube Summary
<img width="705" height="354" alt="Screenshot 2025-12-02 at 2 36 23 AM" src="https://github.com/user-attachments/assets/11e0b845-e269-4f24-9262-2b72803d9176" />




## 📌 Eureka Server Dashboard
<img width="1470" height="916" alt="Screenshot 2025-12-02 at 1 21 41 AM" src="https://github.com/user-attachments/assets/2d4577cc-3b9e-4906-9326-276f8e5d855f" />

## 📌 RabbitMq Dashboard

<img width="423" height="185" alt="Screenshot 2025-12-02 at 12 56 25 AM" src="https://github.com/user-attachments/assets/0dd0c8f6-e3e2-40a0-8518-74a27a38bea7" />
<img width="1470" height="680" alt="Screenshot 2025-12-02 at 2 39 10 AM" src="https://github.com/user-attachments/assets/d27f0222-823a-4e84-81aa-ae97b1957505" />






