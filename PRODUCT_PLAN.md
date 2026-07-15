# Career OS Product Plan

## Product vision

Career OS is a personal job-search workspace that reduces the amount of manual tracking required during a job hunt.

The first useful version should let a user:

- create and manage job applications manually;
- connect a Gmail account;
- detect likely job-related emails;
- attach those emails to the correct application;
- view a timeline of activity for each role.

The longer-term vision is a broader career system that can also organise interviews, documents, recruiter conversations, notes, achievements, and career history.

This document describes the intended direction. It is **not** an instruction to build everything immediately.

---

## Guiding principles

1. Build one useful vertical slice at a time.
2. Keep the architecture simple until real requirements justify complexity.
3. Prefer a modular monolith over microservices.
4. Keep all AI-generated or automatically detected data editable by the user.
5. Treat privacy and permissions as core product requirements.
6. Do not add infrastructure because it is fashionable.
7. A working, understandable feature is more valuable than an elaborate unfinished system.

---

## Proposed technology stack

### Repository

Use a single Git repository containing both applications.

```text
career-os/
├── AGENTS.md
├── PRODUCT_PLAN.md
├── README.md
├── compose.yaml
├── .env.example
└── apps/
    ├── web/
    └── api/
```

Do not introduce Nx, Turborepo, Kubernetes, Kafka, Redis, or a separate deployment repository unless a later requirement clearly needs them.

### Frontend

- Next.js with the App Router
- React
- TypeScript in strict mode
- Tailwind CSS
- shadcn/ui when reusable components become helpful
- TanStack Query once the frontend begins calling the backend
- React Hook Form and Zod for non-trivial forms
- Vitest and React Testing Library
- Playwright for a small number of end-to-end tests later

### Backend

- Java 21
- Spring Boot
- Maven Wrapper
- Spring Web
- Spring Validation
- Spring Data JPA when database persistence begins
- Spring Security and OAuth2 Client when Google authentication begins
- Flyway for database migrations
- JUnit 5, AssertJ, and Mockito
- Testcontainers once database integration tests are needed

### Database

- PostgreSQL
- Docker Compose for local development
- Flyway for every schema change

PostgreSQL is not required in the very first setup stage. The backend may initially run without persistence while the project structure and HTTP connection are proven.

### External integrations

- Gmail API first
- Google Calendar later
- LinkedIn integration is explicitly excluded from the initial plan because access is heavily restricted

### AI

AI is a later enhancement, not an initial dependency.

Start with deterministic rules for detecting job-related emails. Introduce an LLM only after:

- Gmail ingestion works reliably;
- email data can be reviewed and corrected;
- the application domain model is stable enough to accept structured extraction results.

---

## Intended domain model

The likely core entities are:

- User
- Company
- JobApplication
- JobEvent
- EmailMessage
- GoogleConnection
- SyncCursor

A `JobApplication` represents one role at one company.

A `JobEvent` represents something that happened during the application process, such as:

- application submitted;
- confirmation received;
- recruiter contact;
- interview scheduled;
- assessment received;
- offer received;
- rejection received;
- manual note added.

The timeline should be built from job events instead of adding a new database table for every event type.

---

## Gmail ingestion design

The eventual ingestion flow should be:

```text
Fetch message
    ↓
Determine whether it is job-related
    ↓
Extract company, role, and event type
    ↓
Match or create a job application
    ↓
Create a timeline event
    ↓
Allow the user to review and edit the result
```

### Initial classification

Start with rule-based detection using:

- known applicant-tracking-system sender domains;
- common subject lines;
- phrases such as “thanks for applying” or “interview invitation”;
- recruiter and careers terminology.

The classifier should return structured data with a confidence score.

Low-confidence detections should be placed into a review queue rather than silently changing the user’s data.

### Later AI classification

An LLM-based classifier may later be added behind an interface so it can replace or supplement the rule-based classifier without rewriting the ingestion pipeline.

Possible later AI uses include:

- extracting role and company names from messy emails;
- summarising application history;
- generating interview preparation notes;
- semantic search across applications;
- identifying overdue follow-ups.

---

## Security and privacy requirements

When Google integration is introduced:

- use the server-side OAuth flow;
- request the narrowest possible Gmail scope;
- keep access and refresh tokens on the backend;
- encrypt stored refresh tokens;
- never place Google tokens in browser local storage;
- support disconnecting the Google account;
- avoid logging email bodies or authentication tokens;
- store only the email data required for the product;
- do not download or retain attachments during the early phases.

The application should favour storing message metadata, excerpts, detected fields, and Gmail identifiers over storing complete unrelated inbox data.

---

## Delivery stages

### Stage 0: Repository and application skeletons

Goal: prove that the frontend and backend both start successfully.

Deliverables:

- repository structure;
- Next.js frontend;
- Spring Boot backend;
- backend health endpoint;
- frontend page that can display the backend health result;
- basic README with setup commands;
- linting, formatting, and basic tests;
- no database;
- no Google integration;
- no AI.

### Stage 1: Manual in-memory job tracker

Goal: prove the core product interaction before adding persistence.

Deliverables:

- job application list page;
- create application form;
- application detail page;
- edit status and notes;
- temporary in-memory backend storage or clearly labelled mock data;
- a small set of backend and frontend tests.

This stage may be skipped if moving directly to PostgreSQL is simpler once the skeleton is stable.

### Stage 2: PostgreSQL persistence

Goal: make the manual tracker durable.

Deliverables:

- PostgreSQL through Docker Compose;
- Spring Data JPA;
- Flyway migrations;
- persisted companies, applications, and job events;
- integration tests using Testcontainers;
- documented local database setup and reset steps.

### Stage 3: Google sign-in and Gmail connection

Goal: allow a user to securely connect Gmail.

Deliverables:

- Google Cloud project setup documentation;
- OAuth consent screen configuration;
- Spring Security OAuth2 client setup;
- user session handling;
- encrypted refresh-token storage;
- connect and disconnect controls;
- no automatic inbox classification yet.

### Stage 4: Manual Gmail scan

Goal: import likely job-related emails on demand.

Deliverables:

- a “Scan Gmail” action;
- Gmail search queries;
- message metadata retrieval;
- idempotent processing;
- rule-based classification;
- application matching;
- review queue for uncertain results;
- visible scan status and errors.

### Stage 5: Incremental synchronisation

Goal: keep the tracker updated without rescanning the entire mailbox.

Deliverables:

- stored sync cursor;
- scheduled incremental scans;
- retry handling;
- duplicate prevention;
- sync history and user-visible status.

### Stage 6: Calendar integration

Goal: add interview events from Google Calendar.

Deliverables:

- additional OAuth scope requested only when the feature is enabled;
- calendar-event retrieval;
- interview event matching;
- duplicate handling between email and calendar events.

### Stage 7: AI-assisted organisation

Goal: improve classification and summaries after the deterministic system works.

Deliverables:

- structured LLM outputs;
- confidence thresholds;
- evaluation dataset;
- cost logging;
- prompt and model version tracking;
- user correction workflow;
- no autonomous destructive changes.

### Stage 8: Production deployment

Goal: create a safe public demo.

Deliverables:

- production hosting;
- managed PostgreSQL;
- secrets management;
- monitoring and structured logs;
- privacy policy and account deletion;
- OAuth verification work if required;
- end-to-end tests for critical flows.

---

## Explicit non-goals for the early product

Do not build these during the initial stages:

- microservices;
- Kafka or RabbitMQ;
- Kubernetes;
- Redis;
- a mobile application;
- a browser extension;
- LinkedIn scraping;
- payments;
- CV generation;
- interview coaching;
- vector databases;
- autonomous AI agents;
- real-time WebSockets;
- production deployment before the local product works.

---

## Definition of a successful MVP

The MVP is successful when a user can:

1. create and edit a job application manually;
2. connect Gmail securely;
3. trigger a Gmail scan;
4. see likely job-related messages grouped into applications;
5. correct mistakes;
6. view a useful application timeline.

Everything beyond that is expansion, not a prerequisite.
