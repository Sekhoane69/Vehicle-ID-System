# Vehicle Identification System (VIS)
### JavaFX + PostgreSQL MVC Application

---

## Overview

A full-featured desktop application for vehicle identification, built with JavaFX (FXML/MVC), PostgreSQL via JDBC, and Maven.

---

## Architecture: MVC Pattern

```
com.vis/
├── model/          ← MODEL: Data classes (Vehicle, Customer, etc.)
│   ├── BaseEntity.java       (Abstract — Inheritance base)
│   ├── Vehicle.java
│   ├── Customer.java
│   ├── User.java
│   ├── ServiceRecord.java
│   ├── InsuranceRecord.java
│   ├── PoliceReport.java
│   ├── Violation.java
│   └── CustomerQuery.java
│
├── dao/            ← DATA ACCESS (Repository layer)
│   ├── DatabaseConnection.java   (Singleton JDBC)
│   ├── BaseDAO.java              (Abstract generic DAO — Polymorphism)
│   ├── UserDAO.java
│   ├── VehicleDAO.java
│   ├── CustomerDAO.java
│   ├── ServiceRecordDAO.java
│   ├── InsuranceDAO.java
│   ├── PoliceReportDAO.java
│   └── ViolationDAO.java
│
├── controller/     ← CONTROLLER: JavaFX event handlers
│   ├── LoginController.java
│   ├── RegisterController.java
│   ├── MainController.java
│   ├── DashboardController.java
│   ├── WorkshopController.java
│   ├── CustomerController.java
│   ├── PoliceController.java
│   ├── InsuranceController.java
│   └── AdminController.java
│
└── util/
    ├── AnimationUtil.java        (Reusable animations)
    └── SessionManager.java       (Singleton login session)

resources/com/vis/
├── fxml/           ← VIEW: FXML UI layout files
│   ├── login.fxml
│   ├── register.fxml
│   ├── main.fxml
│   ├── dashboard.fxml
│   ├── workshop.fxml
│   ├── customer.fxml
│   ├── police.fxml
│   ├── insurance.fxml
│   └── admin.fxml
└── css/
    └── style.css
```

---

## Requirements Met

| Requirement                        | Implementation |
|------------------------------------|----------------|
| Menu Bar & Menu Items              | `main.fxml` — File/Modules/Account/Help menus |
| Pagination & ScrollPane            | All modules — 20+ scroll items + Pagination control |
| Progress Indicators                | ProgressBar + ProgressIndicator on every module |
| Visual Effects (DropShadow)        | `AnimationUtil.applyDropShadow()` on all primary buttons |
| FadeTransition (fade loop)         | `AnimationUtil.createFadeLoop()` on register button |
| PostgreSQL via JDBC                | `DatabaseConnection.java` using `org.postgresql:postgresql:42.7.3` |
| Exception Handling                 | Try-catch throughout all DAO and controller classes |
| Inheritance                        | `BaseEntity` (model) + `BaseDAO<T>` (DAO) — extended by all subclasses |
| Polymorphism                       | `getSummary()` method overridden in every model class |
| MVC Pattern                        | Strict Model/View(FXML)/Controller separation |
| Graphs instead of tables           | BarChart, PieChart, LineChart on every module |
| Account creation                   | Register screen with role selection |

---

## Setup Instructions

### 1. Prerequisites
- Java 17+ (JDK)
- Maven 3.8+
- PostgreSQL 14+

### 2. Database Setup
```sql
-- Create the database
CREATE DATABASE vehicle_identification_db;
```
Then run `database/schema.sql` in pgAdmin or psql:
```bash
psql -U postgres -d vehicle_identification_db -f database/schema.sql
```

### 3. Configure Database Connection
Edit `src/main/java/com/vis/dao/DatabaseConnection.java`:
```java
private static final String DB_URL  = "jdbc:postgresql://localhost:5432/vehicle_identification_db";
private static final String DB_USER = "postgres";
private static final String DB_PASS = "your_password";
```

### 4. Build & Run
```bash
mvn clean install
mvn javafx:run
```
Or run the fat JAR:
```bash
mvn clean package
java -jar target/vehicle-identification-system-1.0-SNAPSHOT.jar
```

---

## Default Login Credentials
| Username    | Password   | Role       |
|-------------|------------|------------|
| admin       | admin123   | ADMIN      |
| workshop1   | admin123   | WORKSHOP   |
| police1     | admin123   | POLICE     |
| insurance1  | admin123   | INSURANCE  |

---

## Modules

| Module     | Features |
|------------|----------|
| Dashboard  | Stats cards, 4 live charts, progress bars |
| Workshop   | Vehicle registration, service records, pie/bar charts |
| Customer   | Customer CRUD, vehicle fleet charts |
| Police     | Reports & violations, pie/bar charts |
| Insurance  | Policy management, provider/status charts |
| Admin      | User management, role-based access control |

---

## Database Schema
See `database/schema.sql` for full schema including:
- Tables: users, customers, vehicles, service_records, insurance_records, police_reports, violations, customer_queries
- Stored procedure: `get_vehicle_full_info()`
- View: `vehicle_summary`
- Seed data: 10 customers, 10 vehicles, 11 service records, 10 insurance records, 5 police reports, 12 violations

---

## GitHub
```bash
git init
git add .
git commit -m "Initial commit: Vehicle Identification System"
git remote add origin https://github.com/YOUR_USERNAME/vehicle-identification-system.git
git push -u origin main
```
