# Gym CRM application
**CRM system for gym management.**

![Build](https://github.com/borderForNoone/gym-crm/actions/workflows/ci.yml/badge.svg?branch=develop)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=borderForNoone_gym-crm&metric=coverage)](https://sonarcloud.io/summary/overall?id=borderForNoone_gym-crm)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=borderForNoone_gym-crm&metric=alert_status)](https://sonarcloud.io/summary/overall?id=borderForNoone_gym-crm)
# Getting Started (Local Setup)

## Prerequisites

To run this application, you should have the following installed:

- **Java Development Kit (JDK) 17**
- **Maven**
- **Git**

## 1. Clone the project

```bash
git clone https://github.com/juliakhomyn/gym-crm.git
cd gym-crm
```

## 2. Database Setup MySQL
Run the following script to create the database and add a user:

```sql
CREATE DATABASE gym_db;
CREATE USER 'gymuser'@'localhost' IDENTIFIED BY 'gympass';
GRANT ALL PRIVILEGES ON gym_db.* TO 'gymuser'@'localhost';
FLUSH PRIVILEGES;
```

## 3. Environment Variables
Add following configuration for environment variables:

```text
DB_URL=jdbc:mysql://localhost:3306/gym_crm
DB_USERNAME=gymuser
DB_PASSWORD=gympass
```

If you want to use specific environment, you can configure it by adding:

```text
SPRING_PROFILES_ACTIVE=dev
```

## 4. Build the project

```bash
mvn clean compile
```

## 5. Run tests

```bash
mvn test
```

## 5. Run the application from console

```bash
mvn spring-boot:run
```

After startup, the application will be available at:

* Base API Path: http://localhost:8080/gym-crm/api/v1
* OpenAPI / Swagger UI: http://localhost:8080/gym-crm/swagger-ui/index.html
* OpenAPI Spec (JSON): http://localhost:8080/gym-crm/v3/api-docs

## Postman Collection

The project includes a Postman collection for testing the API. You can find it at the following relative path:

```text
postman/gym-crm.postman_collection.json
```

## Actuator and Metrics

Spring Boot Actuator and Micrometer are configured to expose system and custom metrics.

Base local URL: http://localhost:8080/gym-crm/actuator

### Health:

* Database Health: http://localhost:8080/gym-crm/actuator/health/database
* Disk Space Health: http://localhost:8080/gym-crm/actuator/health/diskSpace
* Memory Health: http://localhost:8080/gym-crm/actuator/health/memory
* Prometheus metrics: http://localhost:8080/gym-crm/actuator/prometheus

### Custom Metrics

* **User Registrations**: http://localhost:8080/gym-crm/actuator/metrics/gym.auth.login.attempts
  * Tags: `type` (trainee, trainer), `status` (success, failure)
* **Login Attempts**: http://localhost:8080/gym-crm/actuator/metrics/gym.auth.login.attempts
  * Tags: `status` (success, failure)
* **Trainings Created**: http://localhost:8080/gym-crm/actuator/metrics/gym.training.creations
  * Tags: `type` (training type name e.g. Yoga, Pilates)
  * *Note: Returns 404 until at least one training has been created.*
* **Active Users**: http://localhost:8080/gym-crm/actuator/metrics/gym.users.active
  * Tags: `type` (trainee, trainer)
* **Total Users**: http://localhost:8080/gym-crm/actuator/metrics/gym.users.total
  * Tags: `type` (trainee, trainer)