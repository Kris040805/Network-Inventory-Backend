# Network Inventory Backend

REST API backend for managing a network inventory consisting of network sites, routers, shelves, slots, and cards.

## Tech Stack

* Java 21
* Spring Boot 3.5.16
* Maven
* Spring Web
* Spring Data JPA / Hibernate
* Microsoft SQL Server
* Bean Validation
* Springdoc OpenAPI / Swagger UI
* JUnit 5
* Mockito
* Postman
* Spring Security (HTTP Basic authentication)



## Architecture

The application follows a layered architecture:

```text id="r4q8x1"
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

DTOs are used for API requests and responses, while mappers handle conversion between entities and DTOs.

The main layers are:

* **Controller** — exposes REST endpoints and handles HTTP requests.
* **Service** — contains business logic and validation of business rules.
* **Repository** — provides database access through Spring Data JPA.
* **Entity** — represents the database tables.
* **DTO** — defines the API request and response models.
* **Mapper** — converts between entities and DTOs.
* **Exception handling** — centralized through `@RestControllerAdvice`.


## Database Setup

The application uses Microsoft SQL Server.

Database scripts are located in the `db/` directory:

* `schema.sql` — creates the database tables (including `app_user` for authentication), constraints, and indexes.
* `seed.sql` — inserts sample network inventory data and sample users (see [Authentication](#authentication)).
* `drop.sql` — removes the created database objects.

### Setup

1. Create an empty SQL Server database.
2. Run `db/schema.sql` against the database.
3. Run `db/seed.sql` to populate the database with sample data.
4. Configure the application's database connection in the local Spring Boot configuration.
5. Start the application.

Hibernate schema generation is disabled. The application validates the existing database schema on startup.


## Configuration

Database connection properties are configured locally.

Use the provided example configuration as a template and create the corresponding local configuration with the appropriate SQL Server connection details.

The application expects the following database configuration:

```properties id="g3p8n2"
spring.datasource.url=jdbc:sqlserver://<server>:<port>;databaseName=<database>;encrypt=true;trustServerCertificate=true
spring.datasource.username=<username>
spring.datasource.password=<password>
```

Hibernate is configured to validate the existing schema rather than generate or modify database tables.




## Build and Run

### Prerequisites

Make sure the following are installed:

* JDK 21
* Microsoft SQL Server
* Maven (or use the included Maven Wrapper)

### Build

On Windows:

```powershell
.\mvnw.cmd clean compile
```

### Run

Start the application with:

```powershell
.\mvnw.cmd spring-boot:run
```

The API is available at:

```text
http://localhost:8080
```



## API

The API uses the following base path:

```text
/api/v1
```

The main resources are:

| Resource | Base endpoint     |
| -------- | ----------------- |
| Sites    | `/api/v1/sites`   |
| Routers  | `/api/v1/routers` |
| Shelves  | `/api/v1/shelves` |
| Slots    | `/api/v1/slots`   |
| Cards    | `/api/v1/cards`   |

### Hierarchy endpoints

The API also provides endpoints for navigating the network inventory hierarchy:

* `GET /api/v1/sites/{id}/routers`
* `GET /api/v1/routers/{id}/shelves`
* `GET /api/v1/shelves/{id}/slots`
* `GET /api/v1/slots/{id}/card`
* `GET /api/v1/routers/{id}/tree`

Card installation and removal are handled through the slot resource:

* `POST /api/v1/slots/{id}/card`
* `DELETE /api/v1/slots/{id}/card`



## Authentication   

The API is protected with HTTP Basic authentication. Every request to `/api/v1/**` must include valid credentials.

The following paths are public:

* `/swagger-ui.html` and `/swagger-ui/**`
* `/v3/api-docs/**`

### Users

Users are stored in the `app_user` table. Passwords are stored as BCrypt hashes and never in plain text.

The `db/seed.sql` script creates two sample users:

| Username | Password     | Role    |
| -------- | ------------ | ------- |
| `admin`  | `Admin#2026` | `ADMIN` |
| `user`   | `User#2026`  | `USER`  |

These credentials are intended for local development only and must be changed in any other environment.

Roles are enforced only for creating users: `POST /api/v1/users` requires the `ADMIN` role. All other endpoints are available to any authenticated user.

### Sending credentials

Send the credentials in the `Authorization` header as `Basic` followed by the Base64 encoding of `username:password`:

```text
Authorization: Basic <base64(username:password)>
```

Example:

```powershell
curl.exe -u "admin:Admin#2026" http://localhost:8080/api/v1/sites
```

In Swagger UI, click **Authorize** and enter the username and password. In Postman, the `username` and `password` variables of the environment are used by the collection.

### Creating users

Users are created with `POST /api/v1/users`. This endpoint requires the `ADMIN` role.

Request body:

| Field      | Required | Rules                                                          |
| ---------- | -------- | -------------------------------------------------------------- |
| `username` | yes      | 3-50 characters: letters, digits, `.`, `_` and `-`; must be unique |
| `password` | yes      | 8-72 characters                                                |
| `role`     | no       | `ADMIN` or `USER` (default: `USER`)                            |

Example:

```json
{
  "username": "new.user",
  "password": "Str0ng#Pass",
  "role": "USER"
}
```

The response is `201 Created` with the new user. The password and its hash are never returned:

```json
{
  "id": 3,
  "username": "new.user",
  "role": "USER",
  "enabled": true,
  "createdAt": "2026-10-19T10:15:30.123"
}
```

Possible errors: `400` (invalid data), `401` (missing or invalid credentials), `403` (the user is not an `ADMIN`) and `409` (the username already exists).

There are no endpoints for changing, disabling or deleting users yet. To disable a user, set `enabled = 0` in the `app_user` table.

### Authentication errors

Requests without valid credentials receive `401 Unauthorized` with a `WWW-Authenticate: Basic` header and the standard JSON error structure:

```json
{
  "timestamp": "...",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication failed: invalid or missing credentials",
  "path": "/api/v1/sites"
}
```
Authenticated users that do not have the required role receive `403 Forbidden` with the same JSON error structure (message: `Access denied: insufficient permissions`).

### Security note

HTTP Basic authentication sends the credentials with every request. They are only Base64-encoded, not encrypted. Use HTTPS in any environment other than local development.


## API Documentation

The API is documented using OpenAPI and can be explored through Swagger UI.

After starting the application, open:

```text id="8j2n4p"
http://localhost:8080/swagger-ui.html
```

Swagger UI provides an interactive overview of the available endpoints, request parameters, request bodies, and response models.

Swagger UI itself is public. To try the protected endpoints, click **Authorize** and enter a username and password (see [Authentication](#authentication)).


## Postman

The repository contains a Postman collection and environment under the `postman/` directory.

Files:

* `Network Inventory.postman_collection.json`
* `Network Inventory Environment.postman_environment.json`

The collection is organized into:

* Authentication
* Users
* Sites
* Routers
* Shelves
* Slots
* Cards
* Hierarchy
* Scenario (E2E)

The `Scenario (E2E)` folder provides an end-to-end lifecycle:

```text
Create Site
    ↓
Create Router
    ↓
Create Shelf
    ↓
Create Slot
    ↓
Install Card into Slot
    ↓
Get Router Tree
    ↓
Remove Card from Slot
    ↓
Delete Site (cascade)
```

The collection uses environment variables for the base URL, generated resource IDs, and credentials. Basic authentication is configured once at the collection level and uses the `username` and `password` variables. The `password` value is not stored in the repository, so set it locally in the environment (for example, the password of a seed user).

The `Authentication` folder contains negative requests that verify `401 Unauthorized` responses for missing credentials and a wrong password.

The `Users` folder also uses `regularUsername` and `regularPassword` (a non-admin user, such as the seed user `user`) to verify that regular users cannot create accounts. Each run of the `Users` folder creates a new `postman.*` user, because users cannot be deleted through the API.

### Running the E2E scenario

1. Start the application.
2. Open the `Network Inventory` collection in Postman.
3. Select `Network Inventory Environment` and set the `username` and `password` variables.
4. Run the `Scenario (E2E)` folder using the Collection Runner.
5. Verify that all requests and tests pass.

The scenario is designed to clean up the data it creates, leaving the database in its original state.



## Testing

The project includes automated tests using JUnit 5, Mockito, Spring MVC Test, and Spring Boot integration testing.

The test suite covers:

* Service-layer unit tests for Sites and Routers
* Controller tests using `@WebMvcTest` and MockMvc
* Validation and error handling
* Duplicate resource scenarios
* Missing parent resource scenarios
* Conflict cases
* Cascade deletion behavior
* Full CRUD integration flows
* Network inventory lifecycle integration flow
* Authentication scenarios: missing credentials, wrong password, unknown user, valid credentials, and public Swagger endpoints
* Unit tests for loading users from the database
* User creation: validation, duplicate usernames, role restriction (`ADMIN` only) and logging in with a newly created user


The integration tests run against the configured SQL Server database and require the `admin` and `user` accounts from `db/seed.sql`.

### Run tests

Run the complete test suite with:

```powershell id="f5q3n8"
.\mvnw.cmd test
```

A successful build should finish with:

```text
BUILD SUCCESS
```



## Error Handling

The application uses centralized exception handling through `@RestControllerAdvice`.

The API returns the following HTTP status codes:

| Status                       | Meaning                                |
|------------------------------|----------------------------------------|
| `200 OK`                     | Successful read or update              |
| `201 Created`                | Resource successfully created          |
| `204 No Content`             | Resource successfully deleted          |
| `400 Bad Request`            | Invalid request or validation error    |
| `401 Unauthorized`           | Missing or invalid credentials         |
| `403 Forbidden`              | Not enough permissions for the action  |
| `404 Not Found`              | Requested resource does not exist      |
| `409 Conflict`               | Business rule or uniqueness conflict   |
| `500 Internal Server Error`  | Unexpected server error                |

Error responses contain a consistent structure with fields such as:

```json id="h2m7qk"
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "...",
  "path": "..."
}
```


## Business Rules

The backend enforces the following business rules:

* A router must belong to an existing network site.
* A shelf must belong to an existing router.
* A slot must belong to an existing shelf.
* A slot can contain at most one card.
* A card can be installed in at most one slot.
* Site codes are globally unique.
* Router hostnames and serial numbers are globally unique.
* Card serial numbers are globally unique.
* Shelf numbers are unique within a router.
* Slot numbers are unique within a shelf.
* Installing a card into an occupied slot is rejected.
* Deleting a resource with children is rejected unless `cascade=true` is provided.


