📘 Trading Signal Backend — Spring Boot + PostgreSQL

A production‑grade backend service for managing crypto trading signals with:

i) Live price integration (Binance Public API)
ii) Dynamic signal evaluation (TARGET / STOPLOSS / EXPIRED)
iii) ROI calculation
iv) Scheduled background updates
v) PostgreSQL persistence
vi) Swagger API documentation
vii) Full unit test coverage

🚀 1. Setup Instructions
Prerequisites
Ensure the following are installed:

Java 17+

Maven 3.8+

PostgreSQL 14+

Git

(Optional) Docker & Docker Compose

##Clone the Repository
git clone https://github.com/your-username/trading-signals-backend.git
cd trading-signals-backend

##Install Dependencies
mvn clean install

##Run the Application
Option A — Using Maven
mvn spring-boot:run

Option B — Using IDE
Run the main class:  com.example.trading.TradingApplication

🗄️ 2. Database Setup (PostgreSQL)
Create Database
Login to PostgreSQL: psql -U postgres
Create the database: CREATE DATABASE trading_signals_db;

##Configure Credentials
Update src/main/resources/application.properties:

spring.datasource.url=jdbc:postgresql://localhost:5432/trading_signals_db
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

##Verify Connection
psql -U postgres -d trading_signals_db

📚 3. API Documentation
Once the application is running, open:

👉 Swagger UI
http://localhost:8081/swagger-ui/index.html

##Available Endpoints
| HTTP Method | Route                     | Purpose                                      |
|-------------|---------------------------|----------------------------------------------|
| POST        | /api/signals              | Create a new trading signal                  |
| GET         | /api/signals              | Retrieve all trading signals                 |
| GET         | /api/signals/{id}         | Retrieve a specific signal by ID             |
| GET         | /api/signals/{id}/status  | Evaluate and return the current status       |
| DELETE      | /api/signals/{id}         | Delete a specific signal                     |


🧠 4. Architecture Overview
This project follows a clean, layered architecture:

src/main/java/com/example/trading
│
├── controller        → REST endpoints (HTTP layer)
├── service           → Business logic, validation, status evaluation
├── repository        → Spring Data JPA interfaces
├── domain            → Entities & enums
├── dto               → Request/response models
├── mapper            → Entity ↔ DTO conversions
├── scheduler         → Background job for auto-updates
├── integration       → Binance API WebClient
└── exception         → Global exception handling

🔄 Business Logic Flow
1. Create Signal
User submits signal details - 

Validation:

Entry time ≤ 24 hours old
BUY: stopLoss < entryPrice < targetPrice
SELL: stopLoss > entryPrice > targetPrice
Signal saved with status OPEN

2. Status Evaluation
Triggered by:
/api/signals/{id}/status OR automatic scheduler (every minute)

Steps:

Fetch live price from Binance
GET https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT

Apply logic:

BUY
price ≥ target → TARGET_HIT
price ≤ stopLoss → STOPLOSS_HIT

SELL
price ≤ target → TARGET_HIT
price ≥ stopLoss → STOPLOSS_HIT

If expired and still OPEN → EXPIRED
If TARGET/STOPLOSS hit → calculate ROI

Save final state (never changes again)

3. ROI Calculation
BUY: ROI = (Current - Entry) / Entry × 100
SELL: ROI = (Entry - Current) / Entry × 100
Rounded to two decimals.

🌐 5. External API Integration (Binance)
The system uses Binance Public API (no API key required):  https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT

Integration is handled via Spring WebClient:
i) Non-blocking HTTP client
ii) Auto-retries handled by service layer
iii) Converts Binance response → BigDecimal price

⏱️ 6. Scheduled Background Updates
A scheduler runs every minute:  @Scheduled(cron = "0 * * * * *")

It:
i) Fetches all OPEN signals
ii) Evaluates them using live prices
iii) Updates status (TARGET / STOPLOSS / EXPIRED)
iv) Saves results automatically
This ensures signals update even without user requests.

🧪 7. Testing

Unit tests cover:
✔ Validation
BUY rules
SELL rules
Time validation (≤ 24 hours old)

✔ Status Logic
TARGET_HIT
STOPLOSS_HIT
EXPIRED

✔ ROI
BUY ROI
SELL ROI

External dependencies (Binance API) are mocked using Mockito.

Run tests:
mvn test

8. Conclusion

This backend is designed to be:
i) Robust (final states never change)
ii) Accurate (live price evaluation)
iii) Scalable (clean architecture)
iv) Extensible (JWT, Redis, Docker can be added easily)
v) Production-ready (scheduler, validation, error handling)
