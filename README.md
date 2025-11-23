````markdown
# File Service API (Java Developer Challenge)

This repository implements a secure file storage and listing API using Java and Spring Boot, based on the “Java Developer Challenge” requirements.

The goal is to provide:

- REST endpoints to upload, list, download, update, and delete files.
- Validation of file size and allowed extensions.
- Relational database storage for file metadata.
- JWT-secured API access.
- OpenAPI/Swagger documentation.
- Optional React frontend and Postman collection as bonus items.

---

## 1. High-Level Overview

### Backend

- **Language:** Java 24  
- **Framework:** Spring Boot 3.5.8 (modular monolith)  
- **Build Tool:** Maven  
- **Database (Phase 1):** Embedded/file-based H2 (JPA/Hibernate)  
- **Security:** Spring Security + JWT (stateless bearer tokens)  
- **Storage:** Local file system (`./data/files`) with structured folder layout  
- **Documentation:** Swagger / OpenAPI  
- **Tests:** JUnit + Mockito + AssertJ (+ JaCoCo for coverage)

### Architecture Style

- **Modular monolith** with clearly separated bounded contexts:
  - `auth` – authentication, authorization, JWT handling.
  - `file` – file metadata and physical storage.
  - `common` – cross-cutting concerns (exceptions, utilities).
- Clean separation between:
  - `controller` (REST layer)
  - `service` (business logic)
  - `dao` (data access via Spring Data)
  - `pojo.entity` / `pojo.dto` (domain vs transport objects)

---

## 2. Screenshots (Frontend Concept)

The UI is designed as a React SPA consuming the Spring Boot API. It is optional for the challenge but demonstrates how the API is intended to be used.

### Login & Registration

![Login & Registration](docs/screenshots/login_registration.png)

### Dashboard – File List

![Dashboard – File List](docs/screenshots/file_list_dashboard.png)

### File Details & Preview

![File Details & Preview](docs/screenshots/file_details_preview.png)

### Upload Dialog

![Upload Dialog](docs/screenshots/file_upload.png)

> All images are static design references only. The actual React implementation lives in a separate Vite project described below.

---

## 3. Project Structure

### Backend Project: `file-service-api/`

```text
file-service-api/                 # Backend root (this project)
├── README.md
├── .gitignore
├── .gitattributes
├── pom.xml                       # Spring Boot backend build
├── mvnw
├── mvnw.cmd
├── HELP.md
│
├── docs/
│   └── screenshots/              # ONLY for README/documentation
│       ├── login_registration.png
│       ├── file_list_dashboard.png
│       ├── file_details_preview.png
│       └── file_upload.png
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/fileservice/
│   │   │       ├── FileServiceApiApplication.java
│   │   │       │
│   │   │       ├── common/
│   │   │       │   ├── exception/
│   │   │       │   │   ├── ApiError.java
│   │   │       │   │   ├── BusinessException.java
│   │   │       │   │   └── GlobalExceptionHandler.java
│   │   │       │   └── util/
│   │   │       │       ├── DateTimeUtil.java
│   │   │       │       └── FilePathUtil.java
│   │   │       │
│   │   │       ├── security/
│   │   │       │   ├── JwtTokenProvider.java
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── CustomUserDetails.java
│   │   │       │   └── CustomUserDetailsService.java
│   │   │       │
│   │   │       ├── auth/
│   │   │       │   ├── controller/
│   │   │       │   │   └── AuthController.java
│   │   │       │   ├── service/
│   │   │       │   │   ├── AuthService.java
│   │   │       │   │   └── impl/
│   │   │       │   │       └── AuthServiceImpl.java
│   │   │       │   ├── dao/
│   │   │       │   │   ├── UserDao.java
│   │   │       │   │   └── RoleDao.java
│   │   │       │   └── pojo/
│   │   │       │       ├── entity/
│   │   │       │       │   ├── User.java
│   │   │       │       │   └── Role.java
│   │   │       │       └── dto/
│   │   │       │           ├── LoginRequest.java
│   │   │       │           ├── LoginResponse.java
│   │   │       │           └── UserDto.java
│   │   │       │
│   │   │       └── file/
│   │   │           ├── controller/
│   │   │           │   └── FileController.java
│   │   │           ├── service/
│   │   │           │   ├── FileService.java
│   │   │           │   └── impl/
│   │   │           │       └── FileServiceImpl.java
│   │   │           ├── dao/
│   │   │           │   └── FileDao.java
│   │   │           ├── storage/
│   │   │           │   ├── FileStorageService.java
│   │   │           │   └── LocalFileStorageService.java
│   │   │           └── pojo/
│   │   │               ├── entity/
│   │   │               │   └── FileEntity.java
│   │   │               └── dto/
│   │   │                   ├── FileMetadataResponse.java
│   │   │                   ├── FileListItemResponse.java
│   │   │                   └── FileUploadResult.java
│   │   │
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── application-dev.yaml
│   │       ├── logback-spring.xml
│   │       ├── schema.sql
│   │       └── data.sql
│   │
│   └── test/
│       └── java/com/fileservice/
│           ├── auth/...
│           ├── file/...
│           └── common/security/...
│
└── target/                       # Maven build output (ignored in Git)
````

### Frontend Project: `file-api-frontend/` (React + Vite, JavaScript)

The React frontend is a separate Vite app (pure JavaScript, no TypeScript) that consumes the backend API.

```text
file-api-frontend/
├── node_modules/              # Created by `npm install` (ignored in Git)
├── public/
│   └── vite.svg               # Vite default icon (not important)
├── src/
│   ├── assets/
│   │   └── react.svg          # React default icon
│   │
│   ├── pages/                 # Page-level components
│   │   ├── DashboardPage.jsx
│   │   ├── FileDetailsPage.jsx
│   │   └── LoginPage.jsx
│   │
│   ├── services/              # API client layer
│   │   └── api.js             # Axios/fetch wrappers for `/api/auth` and `/api/files`
│   │
│   ├── App.jsx                # React Router configuration and layout
│   ├── index.css              # Tailwind directives and global styles
│   └── main.jsx               # Application entry point (Vite mount)
│
├── .gitignore
├── index.html                 # Main HTML file
├── package.json               # Dependencies and scripts
├── package-lock.json
├── postcss.config.js          # Tailwind/PostCSS configuration
├── tailwind.config.js         # Tailwind configuration
└── vite.config.js             # Vite configuration (Dev server, proxy to backend, etc.)
```

You can keep backend and frontend in separate repositories or in a single mono-repo as two top-level folders (`file-service-api` and `file-api-frontend`).

---

## 4. Data Model (Backend)

H2 schema is managed via `schema.sql` and `data.sql`.

### Auth Tables

* `users`

    * `id` (PK, identity)
    * `email` (unique)
    * `password_hash`
    * `full_name`
    * `is_active`
    * `created_at`, `updated_at`
* `roles`

    * `id` (PK)
    * `name` (unique, e.g. `ROLE_USER`, `ROLE_ADMIN`)
* `user_roles`

    * Composite PK: (`user_id`, `role_id`)
    * FKs to `users` and `roles`

### File Table

* `files`

    * `id` (PK, internal)
    * `public_id` (UUID, public identifier in URLs)
    * `owner_id` (FK → `users.id`)
    * `original_name`
    * `stored_name` (physical filename)
    * `extension` (validated set: PNG, JPEG, JPG, DOCX, PDF, XLSX)
    * `content_type`
    * `size_bytes` (checked ≤ 5 MB)
    * `storage_path` (e.g. `2025/11/20/pdf/R/q4-2024-report.pdf`)
    * `sha256_hash` (optional integrity / deduplication)
    * `is_temp`, `expires_at`
    * `created_at`, `updated_at`, `deleted_at` (soft delete)

Indexed columns: `created_at`, `public_id`, `extension`, `owner_id`.

---

## 5. REST API Overview (Backend)

### Authentication (`/api/auth`)

* `POST /api/auth/register`
  Registers a new user (email + password).
  Returns a basic user DTO or an access token depending on the chosen flow.

* `POST /api/auth/login`
  Accepts credentials, validates them, and returns:

  ```json
  {
    "accessToken": "jwt-token-here",
    "tokenType": "Bearer",
    "expiresIn": 1800,
    "user": {
      "id": 1,
      "email": "admin@example.com",
      "fullName": "System Administrator",
      "roles": ["ROLE_ADMIN", "ROLE_USER"]
    }
  }
  ```

All file endpoints require:

```http
Authorization: Bearer <accessToken>
```

### File Management (`/api/files`)

* `POST /api/files`
  Uploads a single file as `multipart/form-data`:

    * Validates:

        * size ≤ 5 MB
        * extension ∈ {png, jpeg, jpg, docx, pdf, xlsx}
    * Stores the file on disk under a structured path.
    * Persists metadata in `files`.
    * Returns `FileUploadResult`.

* `GET /api/files`
  Returns a paged list of files for the current user (or all, if admin).
  Query params: `page`, `size`, optional `search`, `extension`.

* `GET /api/files/{publicId}`
  Returns detailed metadata (`FileMetadataResponse`).

* `GET /api/files/{publicId}/content`
  Returns the file content as a byte stream (`application/octet-stream`) with proper headers (`Content-Disposition`, `Content-Length`).

* `PUT /api/files/{publicId}`
  Replaces an existing file with a new upload:

    * Validates the same constraints as upload.
    * Updates `stored_name`, `storage_path`, `size_bytes`, `sha256_hash`, and timestamps.

* `DELETE /api/files/{publicId}`
  Performs a logical deletion:

    * Marks `deleted_at` and optionally removes physical file from disk.

Error responses are normalized via `GlobalExceptionHandler`:

```json
{
  "timestamp": "2025-11-23T10:15:30Z",
  "status": 400,
  "error": "Bad Request",
  "message": "File extension 'exe' is not allowed",
  "path": "/api/files"
}
```

---

## 6. Security & JWT (Backend)

* Stateless authentication:

    * On login, the server issues a **signed JWT** (HS256).
    * Token includes `sub` (user id), `email`, and `roles`.
    * All protected endpoints check the `Authorization` header and populate the `SecurityContext`.
* Logout:

    * Handled client-side by discarding the token.
    * For a production setup, token blacklisting/refresh tokens can be added (Phase 3).

---

## 7. Validation Rules (Backend)

* **Max file size:** 5 MB

    * Enforced via `spring.servlet.multipart.max-file-size` and explicit checks.
* **Allowed extensions:** `png`, `jpeg`, `jpg`, `docx`, `pdf`, `xlsx`.
* **Content type:** checked for consistency with extension.
* **Ownership:** non-admin users can only access their own files.
* **Soft delete:** `deleted_at` indicates logical deletion; queries filter out deleted entries by default.

---

## 8. Running the Backend (`file-service-api`)

### Prerequisites

* Java 24
* Maven 3.9+

### Commands

1. Build and run:

   ```bash
   cd file-service-api
   mvn clean spring-boot:run
   ```

2. Access the API:

    * Swagger UI: `http://localhost:8080/swagger-ui/index.html`
    * H2 Console: `http://localhost:8080/h2-console`

        * JDBC URL: `jdbc:h2:file:./data/file-service-db;MODE=PostgreSQL`
        * User: `sa`, password: (empty by default)

3. Typical flow:

    1. `POST /api/auth/register` – create a user.
    2. `POST /api/auth/login` – obtain JWT.
    3. Use the token to call `/api/files` endpoints from Swagger, Postman, or the React UI.

4. Run tests:

   ```bash
   mvn test
   ```

---

## 9. Running the Frontend (`file-api-frontend` – React JS + Vite + Tailwind)

### Prerequisites

* Node.js 20+
* npm (or pnpm/yarn if you configure it)

### Setup

```bash
cd file-api-frontend
npm install
```

### Development server

```bash
npm run dev
```

* Default Vite dev URL: `http://localhost:5173`
* Backend API: `http://localhost:8080` (configure a Vite proxy in `vite.config.js` if you want to avoid CORS issues).

### Frontend responsibilities

* **`src/services/api.js`**

    * Exposes functions like `login`, `register`, `getFiles`, `uploadFile`, `downloadFile`, `deleteFile`, etc.
    * Central place to attach the `Authorization: Bearer <token>` header.

* **`src/pages/LoginPage.jsx`**

    * Handles user login and registration (or routes to separate register page if needed).
    * On successful login, stores JWT (e.g. in `localStorage`) and redirects to dashboard.

* **`src/pages/DashboardPage.jsx`**

    * Calls `GET /api/files` to list files.
    * Provides search, pagination, and navigation to file details.

* **`src/pages/FileDetailsPage.jsx`**

    * Calls `GET /api/files/{publicId}` and `GET /api/files/{publicId}/content`.
    * Shows metadata and preview (for images/PDF via `<img>` or `<iframe>`).

* **`App.jsx`**

    * Defines React Router routes:

        * `/login`
        * `/dashboard`
        * `/files/:publicId`
    * Wraps protected routes with an auth guard based on the presence of a valid token.

* **Tailwind CSS**

    * Tailwind directives are added in `index.css`.
    * Configuration is in `tailwind.config.js` and `postcss.config.js`.

---

## 10. Implementation Phases

### Phase 1 – Coding Challenge Scope (Backend-centric)

* Embedded/file-based **H2** database.
* Local filesystem storage under `./data/files`.
* Simple HS256 JWT using a secret from configuration.
* Full CRUD for file metadata and content.
* Exception handling and input validation.
* Swagger/OpenAPI configuration.
* High-coverage unit and integration tests for core flows.
* Optional minimal React UI (`file-api-frontend`) aligned with the provided design.

### Phase 2 – Production-Oriented Hardening

* Replace H2 with a real RDBMS (PostgreSQL/MySQL):

    * Use Flyway or Liquibase for schema migrations.
    * Externalize connection details via environment variables.
* Move binary storage to **object storage**:

    * MinIO (local/dev) or S3/GCS in real environments.
    * Store only object keys in `files.storage_path`.
* Introduce:

    * `application-prod.yaml` with proper secrets and profiles.
    * Docker/Docker Compose for local orchestration.

### Phase 3 – Advanced Features

* Pre-signed download URLs (JWT per file, short-lived).
* File versioning (multiple revisions per `public_id`).
* Audit logging and download history.
* Improved observability:

    * Metrics (Micrometer/Prometheus).
    * Structured logging and correlation IDs.
* Optional split into two microservices:

    * **Auth service** with its own DB.
    * **File service** as resource server validating tokens.

---

## 11. Commit Strategy

Suggested commit sequence to make review easy:

1. `chore: bootstrap spring boot project`
2. `chore: configure h2 database and schema.sql`
3. `feat(auth): add user and role entities with dao layer`
4. `feat(auth): implement jwt login and registration endpoints`
5. `feat(file): add file entity and local storage service`
6. `feat(file): implement upload and download endpoints`
7. `feat(file): add list, update and delete operations`
8. `feat: expose swagger ui and openapi docs`
9. `test: add unit tests for auth and file services`
10. `test: add integration tests for core api flows`
11. `docs: add README and api usage examples`
12. `chore: add postman collection and minor refactoring`
13. (Frontend repo) `feat(frontend): scaffold vite react app with tailwind and routing`
14. (Frontend repo) `feat(frontend): integrate auth and file listing with backend api`

This pattern shows:

* Progression from infrastructure to features.
* Clear separation between `feat`, `test`, `docs`, and `chore`.
* Easy navigation for reviewers across backend and frontend.

---

## 12. Notes & Trade-Offs

* H2 is used only for development and challenge evaluation. In a real system, a dedicated RDBMS and versioned migrations would be mandatory.
* The JWT secret lives in configuration for simplicity; in production it must come from environment/secret management.
* Filesystem storage is straightforward but not ideal for horizontal scaling; object storage (MinIO/S3) is the natural next step.
* The backend is intentionally a **modular monolith**:

    * Simple to run and review for a coding challenge.
    * Structured so that it can be split into microservices with minimal friction later.
* The frontend is a separate Vite React JS app:

    * Keeps concerns isolated.
    * Can be deployed independently behind the same domain or a reverse proxy pointing to the backend API.

```
::contentReference[oaicite:0]{index=0}
```
