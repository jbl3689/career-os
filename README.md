# Career OS

Career OS is a learning-led career assistant. The manual job tracker is durable
with PostgreSQL, Google authentication and Gmail connection are complete, and
Stage 4 is adding user-triggered Gmail scanning in small, reviewable slices.

## Requirements

- Node.js 24 LTS
- npm 11 (included with Node.js 24)
- Java Development Kit (JDK) 21 or newer; JDK 25 LTS is recommended
- Docker Desktop with Docker Compose

A system Maven installation is not required; the backend includes the Maven
Wrapper. PostgreSQL runs inside Docker, so a separate PostgreSQL installation is
not required. Gmail scanning and AI are intentionally not part of this stage.

Before implementing Google sign-in, follow the development-project walkthrough
in [`docs/google-auth-setup.md`](docs/google-auth-setup.md). Do not put the
generated Google client secret in a tracked file.

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

Start PostgreSQL first. The API now requires the Google OAuth client credentials
created in `docs/google-auth-setup.md`.

Create an ignored local environment file from the repository root:

```bash
cp .env.example .env.local
```

Open `.env.local` and fill in these private values:

```text
GOOGLE_CLIENT_ID=your client ID
GOOGLE_CLIENT_SECRET=your client secret
TOKEN_ENCRYPTION_KEY=a Base64-encoded 32-byte key
```

Generate the encryption key once with `openssl rand -base64 32`. Before testing
Gmail connection, also enable the Gmail API, add `gmail.readonly`, and add the
second callback URI by following `docs/google-auth-setup.md`.

Do not commit `.env.local`; Git already ignores it. Keep the encryption key
stable: changing it means existing Gmail connections must be recreated. Then
open two terminals from the repository root. Start the API after loading the
local values into that terminal:

```bash
cd apps/api
set -a
source ../../.env.local
set +a
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
requests under `/api/v1` and the Google login-start path to Spring Boot, so local
development does not need separate CORS configuration. To use another API URL,
create `apps/web/.env.local`, set `API_BASE_URL`, and restart Next.js.

From the application tracker you can:

- sign in and out with Google;
- separately connect and disconnect the same Google account for future
  read-only Gmail access;
- manually scan a capped recent window and persist candidate message metadata;
- create an application;
- view all applications;
- select an application to see its details;
- update its status and notes.

The UI deliberately uses simple styling. Job applications are now stored in
PostgreSQL, so they remain available after the API or database container is
stopped and restarted, provided the Docker volume is preserved.

### How the API database startup works

When Spring Boot starts:

1. the PostgreSQL JDBC driver opens a connection using the `spring.datasource`
   settings;
2. Flyway checks the database for versioned migrations and applies any that have
   not run before;
3. Hibernate validates JPA mappings against the migrated schema;
4. the web server starts only if those database steps succeed.

The V1 migration, matching JPA entities, and Spring Data repositories are now
present. The application service uses those repositories instead of an
in-memory collection. Each create or update operation runs in a database
transaction, meaning its application and timeline-event changes either all
succeed or all fail together.
`spring.jpa.hibernate.ddl-auto=validate` deliberately prevents Hibernate from
silently creating or changing tables. Flyway will remain the only schema owner.

The persistence path is:

```text
HTTP controller -> application service -> Spring Data repository -> PostgreSQL
```

Creating an application also stores an `APPLICATION_CREATED` event. An update
stores `STATUS_CHANGED` and/or `NOTES_UPDATED` events only when the corresponding
value actually changes. Timeline events are persisted for later product work;
they are not exposed in the UI yet.

### Current database schema

Flyway migration `V1__create_initial_schema.sql` creates:

- `companies`, containing one row per exact company name;
- `job_applications`, containing application details and a `company_id` foreign
  key pointing to `companies`;
- `job_events`, containing timeline entries and a `job_application_id` foreign
  key pointing to `job_applications`;
- `flyway_schema_history`, managed by Flyway to record applied migrations.

Migration `V2__add_users_and_application_ownership.sql` adds:

- `users`, containing the Career OS account identified by Google's stable
  subject identifier;
- a required `user_id` on every job application, preventing applications from
  being shared across user accounts.

Migration `V3__add_google_connections.sql` adds:

- `google_connections`, with one optional connection per Career OS user;
- the connected Gmail address and exact granted scopes;
- an AES-256-GCM encrypted refresh token, never the plaintext token.

Migration `V4__add_gmail_scan_results.sql` adds:

- `email_messages`, containing only the Gmail identifiers, sender, subject, and
  received time needed for Stage 4 processing;
- a unique `(user_id, gmail_message_id)` constraint that prevents duplicate
  messages when a user scans again;
- `gmail_scan_results`, containing one processing state per stored message.

Migration `V5__add_gmail_classification.sql` adds:

- a deterministic classification of `JOB_RELATED`, `NOT_JOB_RELATED`, or
  `UNCERTAIN`;
- a likely event type, such as `INTERVIEW`, `ASSESSMENT`, or `APPLICATION`;
- an explainable 0–100 rule score and short reason;
- processing states for matching, review, and ignored non-job results.

The relationships are:

```text
users     1 ─── many job_applications 1 ─── many job_events
users     1 ─── zero-or-one google_connections
users     1 ─── many email_messages 1 ─── one gmail_scan_results
companies 1 ─── many job_applications
```

Inspect the tables from the repository root:

```bash
docker compose exec database psql -U career_os -d career_os
```

Then use these `psql` commands:

```text
\dt
\d users
\d companies
\d job_applications
\d job_events
\d google_connections
\d email_messages
\d gmail_scan_results
SELECT * FROM flyway_schema_history;
SELECT id, email, display_name FROM users;
SELECT * FROM companies;
SELECT * FROM job_applications;
SELECT * FROM job_events ORDER BY event_date, id;
SELECT user_id, gmail_address, granted_scopes FROM google_connections;
SELECT gmail_message_id, subject, first_seen_at, last_seen_at
FROM email_messages ORDER BY received_at DESC;
SELECT email_message_id, status, classification, event_type,
       confidence_score, classification_reason
FROM gmail_scan_results;
\q
```

The matching Java classes live in the application feature's `persistence`
package. They are named `CompanyEntity`, `JobApplicationEntity`, and
`JobEventEntity` to distinguish database mappings from API and domain records.
The user mapping lives in the authentication feature's persistence package.

Do not edit an applied Flyway migration. Future schema changes must be added as
new files such as `V6__describe_the_change.sql`, preserving a repeatable schema
history for every environment.

## Current backend API

All `/api/v1/applications` endpoints require an authenticated Career OS
session. Unauthenticated requests return `401 Unauthorized`, and requests that
change data also require a CSRF token. The frontend handles both, so use
http://localhost:3000/applications for normal local testing.

`GET /api/v1/auth/me` returns the current Career OS user. `POST
/api/v1/auth/logout` invalidates the server-side session.

`GET /api/v1/google-connection` reports whether Gmail is connected without
returning token data. `DELETE /api/v1/google-connection` revokes Google's grant
and deletes the encrypted local token. Connecting begins at
`/oauth2/authorization/google-gmail` and is intentionally separate from sign-in.

`POST /api/v1/gmail/scan` starts the first Stage 4 manual scan slice. It:

1. decrypts the current user's refresh token inside the API;
2. exchanges it with Google for a short-lived access token;
3. searches at most 10 messages from the last year using a small set of
   job-related terms;
4. retrieves only the Gmail message ID, thread ID, sender, subject, and received
   time;
5. stores new candidate metadata and creates its classification record;
6. applies deterministic rules using sender and subject metadata;
7. stores the classification, likely event type, rule score, and reason;
8. updates `last_seen_at` instead of duplicating a message seen in an earlier
   scan;
9. returns candidates with their classification and a `newlyDiscovered` flag.

The rules recognise job-process terminology and common recruiting-system
senders. They also treat terms such as visa, immigration, and passport as
negative evidence when stronger job evidence is absent. Conflicting or broad
signals remain `UNCERTAIN` for later review. The score is a heuristic strength
indicator, not a statistically calibrated probability.

The current slice deliberately does not retrieve email bodies, match
applications, or change any application data. Refreshing the page clears the
displayed response, but the metadata and classification remain in PostgreSQL.

Google's `messages.list` endpoint initially returns only message and thread IDs,
so the API follows each capped result with a `messages.get` request using
`format=metadata`. The cap avoids downloading large sections of an inbox while
the search rules are still experimental.

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

Docker Desktop must be running for backend tests, but the Compose database does
not need to be started. Testcontainers launches a separate, disposable
PostgreSQL 18.4 container on a random port, Flyway creates its schema, and the
container is removed after the test run. This keeps tests isolated from your
local development data while testing against the same database type used by the
application.

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
- Backend tests also require Docker Desktop because Testcontainers launches a
  temporary PostgreSQL container. If tests report that no Docker environment is
  available, start Docker Desktop and rerun them.
- If port 5432 is already in use, set a different `POSTGRES_PORT` in the root
  `.env` file. The later Spring connection settings must use the same port.
- PostgreSQL uses `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` only
  when it initializes an empty volume. Changing them after the first startup
  does not alter the existing database or user. During local setup, either
  restore the original values or deliberately reset the volume with
  `docker compose down --volumes` before restarting.
- Never edit a Flyway migration after it has run. If startup reports a checksum
  mismatch for V1, restore the committed V1 file. Put intentional schema changes
  in a new migration instead.
- PostgreSQL IDs are generated identifiers, not row counts. Deleting every row
  does not reset the next ID; gaps such as `1, 2, 5` are normal and harmless.
- `npm audit` currently reports a moderate PostCSS advisory through Next.js.
  The suggested forced fix would install an incompatible Next.js version, so do
  not run `npm audit fix --force`; update when a compatible Next.js release
  includes the patched transitive dependency.
- If the frontend shows **API unavailable**, confirm the backend is running on
  port 8080 and refresh the page.
- If port 3000 or 8080 is already in use, stop the conflicting process or
  configure another port and update `API_BASE_URL` accordingly.
- If the API reports that `GOOGLE_CLIENT_ID` or `GOOGLE_CLIENT_SECRET` cannot be
  resolved, load the ignored `.env.local` values in the API terminal before
  starting Spring Boot.
- If Google reports `redirect_uri_mismatch`, confirm the OAuth client's exact
  redirects are `http://localhost:8080/login/oauth2/code/google` and
  `http://localhost:8080/login/oauth2/code/google-gmail`.
- If startup reports that `TOKEN_ENCRYPTION_KEY` cannot be resolved, generate
  one with `openssl rand -base64 32`, add it to `.env.local`, reload that file
  in the API terminal, and restart Spring Boot.
- In Google Auth Platform Testing mode, Gmail authorizations and refresh tokens
  expire after seven days. Reconnect during development; this does not happen
  to identity-only sign-in.
- If a manual scan reports that Gmail access expired, use **Disconnect Gmail**,
  connect it again, and retry. Testing-mode expiry and manual revocation both
  invalidate the stored refresh token.
