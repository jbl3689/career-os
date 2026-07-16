# Career OS

Career OS is a learning-led career assistant. Stage 2 is making the existing
manual job application tracker durable with PostgreSQL.

## Requirements

- Node.js 24 LTS
- npm 11 (included with Node.js 24)
- Java Development Kit (JDK) 21 or newer; JDK 25 LTS is recommended
- Docker Desktop with Docker Compose

A system Maven installation is not required; the backend includes the Maven
Wrapper. PostgreSQL runs inside Docker, so a separate PostgreSQL installation is
not required. Authentication, Gmail, and AI are intentionally not part of this
stage.

Check your installed versions:

```bash
node --version
npm --version
java -version
docker --version
docker compose version
```

## Run PostgreSQL locally

Docker Compose reads `compose.yaml` and creates two things:

- a PostgreSQL container, which is an isolated running PostgreSQL process;
- a named volume called `postgres-data`, which stores the database files outside
  the container so they survive container replacement.

Start PostgreSQL from the repository root:

```bash
docker compose up -d
```

The `-d` flag runs it in the background. Check its state:

```bash
docker compose ps
```

The database is ready when the `database` service is shown as `healthy`. If it
does not become healthy, inspect its logs:

```bash
docker compose logs database
```

The local defaults are documented in `.env.example`:

- database: `career_os`;
- username: `career_os`;
- password: `career_os_local`;
- Mac port: `5432`.

These are development-only credentials. To override them, copy `.env.example`
to a root `.env` file and change the `POSTGRES_*` values. Docker Compose reads
that file automatically, and Git ignores it.

The root `.env` file configures Docker Compose, but it does not automatically
export variables to a Spring Boot process running directly on your Mac. The API
therefore has matching local defaults in `application.properties`. In another
environment, use `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and
`SPRING_DATASOURCE_PASSWORD` to override those defaults. If you change the local
Compose database name, credentials, or port, update the Spring overrides to
match.

Connect with PostgreSQL's command-line client inside the running container:

```bash
docker compose exec database psql -U career_os -d career_os
```

At the `psql` prompt, run `SELECT current_database();` to verify the connection
and `\q` to exit.

Stop and remove the container while preserving its data:

```bash
docker compose down
```

Start it again with `docker compose up -d`; the named volume restores the same
database data.

To perform a destructive local reset, remove the container and its volume:

```bash
docker compose down --volumes
```

This permanently deletes all local Career OS database data. Use it only when a
clean local database is intentional.

## Install the frontend

From the repository root:

```bash
cd apps/web
npm install
```

The backend downloads its Maven dependencies automatically the first time the
Maven Wrapper runs.

## Run locally

Start PostgreSQL first, then open two terminals from the repository root for the
API and frontend.

Start the API:

```bash
cd apps/api
./mvnw spring-boot:run
```

Start the frontend:

```bash
cd apps/web
npm run dev
```

Then open:

- Frontend application tracker: http://localhost:3000/applications
- API health endpoint: http://localhost:8080/api/v1/health

The frontend uses `http://localhost:8080` by default. Next.js proxies browser
requests under `/api/v1` to the Spring Boot API, so local development does not
need separate CORS configuration. To use another API URL, copy `.env.example`
to `apps/web/.env.local`, change `API_BASE_URL`, and restart Next.js.

From the application tracker you can:

- create an application;
- view all applications;
- select an application to see its details;
- update its status and notes.

The UI deliberately uses simple styling. During the first Stage 2 setup slice,
Spring Boot connects to PostgreSQL and runs Flyway during startup, but job
applications still use temporary in-memory storage. The next slice will add the
first schema migration before application persistence is switched over.

### How the API database startup works

When Spring Boot starts:

1. the PostgreSQL JDBC driver opens a connection using the `spring.datasource`
   settings;
2. Flyway checks the database for versioned migrations and applies any that have
   not run before;
3. Hibernate validates JPA mappings against the migrated schema;
4. the web server starts only if those database steps succeed.

The V1 migration and matching JPA entities are now present. The application
service still uses in-memory storage, but startup proves that the SQL schema and
Java mappings agree without changing job-application behaviour.
`spring.jpa.hibernate.ddl-auto=validate` deliberately prevents Hibernate from
silently creating or changing tables. Flyway will remain the only schema owner.

### Current database schema

Flyway migration `V1__create_initial_schema.sql` creates:

- `companies`, containing one row per exact company name;
- `job_applications`, containing application details and a `company_id` foreign
  key pointing to `companies`;
- `job_events`, containing timeline entries and a `job_application_id` foreign
  key pointing to `job_applications`;
- `flyway_schema_history`, managed by Flyway to record applied migrations.

The relationships are:

```text
companies 1 ─── many job_applications 1 ─── many job_events
```

Inspect the tables from the repository root:

```bash
docker compose exec database psql -U career_os -d career_os
```

Then use these `psql` commands:

```text
\dt
\d companies
\d job_applications
\d job_events
SELECT * FROM flyway_schema_history;
\q
```

The matching Java classes live in the application feature's `persistence`
package. They are named `CompanyEntity`, `JobApplicationEntity`, and
`JobEventEntity` to distinguish database mappings from API and domain records.

Do not edit an applied Flyway migration. Future schema changes must be added as
new files such as `V2__describe_the_change.sql`, preserving a repeatable schema
history for every environment.

## Current backend API

List applications:

```bash
curl http://localhost:8080/api/v1/applications
```

Create an application:

```bash
curl -X POST http://localhost:8080/api/v1/applications \
  -H 'Content-Type: application/json' \
  -d '{
    "companyName": "Acme Ltd",
    "roleTitle": "Software Engineer",
    "status": "APPLIED",
    "applicationDate": "2026-07-16",
    "notes": "Applied through the company website."
  }'
```

Fetch application `1`:

```bash
curl http://localhost:8080/api/v1/applications/1
```

Update its status and notes:

```bash
curl -X PATCH http://localhost:8080/api/v1/applications/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "status": "INTERVIEWING",
    "notes": "First interview booked."
  }'
```

Supported statuses are `APPLIED`, `INTERVIEWING`, `OFFER`, `REJECTED`, and
`WITHDRAWN`. Company name, role title, status, and application date are required.
The create endpoint returns `201 Created`; invalid requests return `400 Bad
Request` with field-level errors where possible. Fetching or updating an unknown
ID returns `404 Not Found`. Updating an application refreshes its last activity
date using the API server's current local date.

## Run checks

Backend tests (requires JDK 21):

```bash
cd apps/api
./mvnw test
```

During the current Stage 2 transition, Spring context tests connect to the local
Compose database, so Docker Desktop and `docker compose up -d` must be running
first. The next testing slice will replace this temporary coupling with an
isolated PostgreSQL Testcontainer.

Frontend linting and type checking:

```bash
cd apps/web
npm run lint
npm run typecheck
npm run test
```

You can also verify a production frontend build with `npm run build`.

## Known setup issues

- Java 17 cannot compile this project. Install JDK 21 or newer and ensure the
  correct JDK is active before running the backend. The project targets Java 21
  bytecode and has been tested locally with JDK 25 LTS.
- This project has been tested with Node.js 24 LTS. If `node --version` still
  reports an older installation after upgrading through nvm, reload the terminal
  or run `source ~/.zshrc`.
- The first Maven Wrapper run needs internet access to download Maven and the
  Spring dependencies.
- Docker Desktop must be running before `docker compose` commands can reach the
  Docker engine.
- If port 5432 is already in use, set a different `POSTGRES_PORT` in the root
  `.env` file. The later Spring connection settings must use the same port.
- PostgreSQL uses `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` only
  when it initializes an empty volume. Changing them after the first startup
  does not alter the existing database or user. During local setup, either
  restore the original values or deliberately reset the volume with
  `docker compose down --volumes` before restarting.
- `npm audit` currently reports a moderate PostCSS advisory through Next.js.
  The suggested forced fix would install an incompatible Next.js version, so do
  not run `npm audit fix --force`; update when a compatible Next.js release
  includes the patched transitive dependency.
- If the frontend shows **API unavailable**, confirm the backend is running on
  port 8080 and refresh the page.
- If port 3000 or 8080 is already in use, stop the conflicting process or
  configure another port and update `API_BASE_URL` accordingly.
