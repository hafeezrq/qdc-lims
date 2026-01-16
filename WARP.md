# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

QDC-LIMS is a Laboratory Information Management System built with Spring Boot 4.0.1 and Java 21. It's a full-stack web application using Thymeleaf for server-side rendering, Spring Security for authentication/authorization, and PostgreSQL for persistence. The system manages patient registration, lab orders, test results, inventory, billing, and doctor commissions.

## Development Commands

### Build & Run
```bash
# Run the application locally (requires PostgreSQL running)
./mvnw spring-boot:run

# Build JAR file
./mvnw clean package

# Build with tests skipped
./mvnw clean package -DskipTests

# Clean build artifacts
./mvnw clean
```

### Testing
```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=QdcLimsApplicationTests

# Run tests with verbose output
./mvnw test -X
```

### Docker
```bash
# Start database and application
docker-compose up

# Start in detached mode
docker-compose up -d

# Stop all containers
docker-compose down

# Rebuild and start
docker-compose up --build

# View logs
docker-compose logs -f app
```

### Database Access
```bash
# Connect to local PostgreSQL
psql -U postgres -d qdc_lims_db -h localhost -p 5432

# Connect to Docker PostgreSQL
docker exec -it qdc_postgres psql -U postgres -d qdc_lims_db
```

## Architecture Overview

### Layer Structure
The application follows a clean layered architecture:

1. **Web Layer** (`com.qdc.lims.web`): Thymeleaf-based MVC controllers returning HTML views
   - `MainWebController`: Core portal, settings, patient registration, search
   - `ReceptionController`: Patient booking and reception workflows
   - `LabController`: Lab technician workflows (test entry, worklist)
   - `FinanceController`: Commission tracking, daily reports
   - `SupplierController`: Supplier and purchase management
   - `LoginController`: Custom login handling
   - Role-based access via Spring Security (ADMIN, LAB, RECEPTION)

2. **Controller Layer** (`com.qdc.lims.controller`): REST API endpoints (JSON responses)
   - `OrderController`: Lab order creation API
   - `PatientController`: Patient data API
   - `TestController`: Test definition API
   - `ResultController`: Test result entry API
   - `ReportController`: PDF report generation

3. **Service Layer** (`com.qdc.lims.service`): Business logic and transactional operations
   - `OrderService`: Order creation with inventory deduction and commission calculation
   - `PatientService`: Patient registration with MRN generation
   - `ResultService`: Test result entry and order status management
   - `PurchaseService`: Purchase entry and supplier ledger management
   - `ReportService`: PDF report generation using OpenPDF

4. **Repository Layer** (`com.qdc.lims.repository`): Spring Data JPA repositories for database access

5. **Entity Layer** (`com.qdc.lims.entity`): JPA entities with relationships
   - `Patient`: MRN-based patient records with CNIC indexing
   - `LabOrder`: Central order entity with results, billing, and commission tracking
   - `LabResult`: Individual test results linked to orders
   - `TestDefinition`: Test catalog with pricing
   - `TestConsumption`: Recipe system linking tests to inventory items
   - `InventoryItem`: Stock management with purchase tracking
   - `Doctor`: Doctor information with commission percentages
   - `CommissionLedger`: Hidden commission tracking per order
   - `SupplierLedger`: Purchase payment tracking
   - `User`: Authentication with role-based access (uses BCrypt)

### Key Design Patterns

**Dual Controller Architecture**: The system uses both `@Controller` (web layer for HTML) and `@RestController` (API layer for JSON). When building new features:
- Use `web` package controllers for user-facing pages
- Use `controller` package for AJAX/API endpoints
- Web controllers inject services and repositories directly

**Automatic Stock Deduction**: `OrderService.createOrder()` automatically deducts inventory based on `TestConsumption` recipes. If stock is insufficient, the entire transaction rolls back with a descriptive error message.

**MRN Generation**: Patient registration automatically generates a unique 6-digit MRN (format: "XXX-XXX") using NanoID in `IdGenerator.generateMrn()`.

**Commission Tracking**: When a doctor is assigned to an order and has a commission percentage > 0, a `CommissionLedger` entry is automatically created. This is intentionally hidden from patient-facing interfaces.

**QR Code Integration**: Reports include QR codes generated using ZXing library via `QrCodeUtil.generateBase64Qr()`, embedded as Base64 PNG images in Thymeleaf templates.

**Security Configuration**: Three role levels (ADMIN, LAB, RECEPTION) with path-based restrictions. Users can have multiple roles (comma-separated). CSRF is disabled. Custom login page at `/login` with error handling.

## Configuration

### Application Properties
Located at `src/main/resources/application.properties`:
- Database connection: `jdbc:postgresql://localhost:5432/qdc_lims_db`
- Default credentials: `postgres` / `admin`
- Hibernate DDL: `update` mode (auto-creates/updates tables)
- SQL logging: enabled with formatting

### Database Configuration
- Docker Compose provides PostgreSQL 16 Alpine
- Database: `qdc_lims_db`
- Port: 5432 (mapped to host)
- Persistent volume: `db_data`
- Override with environment variables in docker-compose.yml

### First Run Setup
On first startup, the application redirects to `/setup` where lab information and the first admin user are configured. This is enforced by checking `LabInfoRepository.count() == 0`.

## Key Conventions

### Entity Relationships
- Use `@ManyToOne` with `@JoinColumn` for foreign keys
- Use `@OneToMany(mappedBy = "...", cascade = CascadeType.ALL)` for child collections
- Initialize collections with `new ArrayList<>()` to avoid NPEs
- Use `@PrePersist` for auto-setting timestamps
- Use `@PreUpdate` for calculated fields (e.g., `LabOrder.calculateBalance()`)

### Service Layer Transactions
- Mark service methods with `@Transactional` when multiple database operations must succeed or fail together
- Use constructor injection for repositories (not `@Autowired` fields)
- Throw `RuntimeException` with descriptive messages for business rule violations

### DTO Usage
- Use Java records for request DTOs (e.g., `OrderRequest`, `PurchaseRequest`)
- DTOs in `com.qdc.lims.dto` package
- Validate in controller or service layer before processing

### Repository Custom Queries
Use `@Query` for custom queries when method name derivation is insufficient. Index frequently queried columns (e.g., MRN, CNIC on Patient).

### Lombok Usage
All entities use `@Data` for getters/setters/toString. Exclude Lombok from the final JAR build via Maven plugin configuration.

## Testing Strategy

Currently minimal test coverage (only context loading test exists). When adding tests:
- Use `@SpringBootTest` for integration tests
- Use `@DataJpaTest` for repository layer tests
- Use `@WebMvcTest` for controller layer tests
- Test files belong in `src/test/java/com/qdc/lims/`

## Common Workflows

### Adding a New Entity
1. Create entity class in `entity` package with `@Entity` and `@Data`
2. Create repository interface extending `JpaRepository` in `repository` package
3. Add business logic in `service` package with `@Service`
4. Create REST endpoint in `controller` package with `@RestController`
5. Create web UI in `web` package with `@Controller` and Thymeleaf template

### Adding a New Test Definition
Tests are defined in `TestDefinition` entity with a recipe of `TestConsumption` entries linking to `InventoryItem`. When a test is ordered, the system automatically checks and deducts inventory.

### Generating Reports
`ReportService` uses OpenPDF to generate thermal receipts. It fetches `LabInfo` for header details and generates QR codes containing the order URL. PDFs are streamed as byte arrays with appropriate Content-Type headers.

## Security Notes

- Passwords are hashed with BCrypt
- User roles are stored as comma-separated strings (e.g., "ROLE_ADMIN,ROLE_LAB")
- The `SecurityConfig` automatically strips "ROLE_" prefix when building UserDetails
- Access denied redirects to `/access-denied`
- Failed login redirects to `/login?error=true`
- All `/api/**` endpoints require authentication

## Frontend Technology

Thymeleaf templates in `src/main/resources/templates/` with static assets (CSS, JS) in `src/main/resources/static/`. The application uses server-side rendering with minimal client-side JavaScript for AJAX calls to REST endpoints.
