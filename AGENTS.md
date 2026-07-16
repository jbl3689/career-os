# Career OS Development Guide

## Purpose of this file

This file directs both Codex and the developer through the project in small, controlled stages.

The full product direction lives in `PRODUCT_PLAN.md`.

Do **not** attempt to implement the full product plan at once. Work only on the current stage unless the user explicitly asks to move forward.

---

## Core rule

Before making changes:

1. identify the current stage;
2. state the smallest useful goal for the current task;
3. avoid adding dependencies or infrastructure needed only by a later stage;
4. keep the project runnable after every meaningful change;
5. document any manual setup the developer must perform.

When a task is ambiguous, prefer the simpler implementation that preserves a path to the next stage.

---

## Current stage

**Stage 2: PostgreSQL persistence**

Stages 0 and 1 are complete. The current goal is:

> Replace temporary in-memory storage with PostgreSQL while preserving the
> existing manual job-tracker behaviour and API shape.

### Stage 2 deliverables

- PostgreSQL through Docker Compose;
- Spring Data JPA and the PostgreSQL JDBC driver;
- Flyway migrations as the source of truth for the database schema;
- persisted companies, job applications, and job events;
- integration tests using PostgreSQL through Testcontainers;
- plain-language database startup, inspection, reset, and troubleshooting documentation.

### Stage 2 progress

- complete: local PostgreSQL container and initial Docker Compose documentation;
- complete: Spring Boot database connection and persistence dependencies;
- complete: initial Flyway schema migration;
- complete: JPA entities;
- complete: Spring Data JPA repositories;
- complete: replacement of in-memory application storage;
- complete: persisted job events;
- complete: PostgreSQL integration tests;
- complete: final setup, reset, inspection, and troubleshooting documentation.

Do not add Google integration, authentication, Gmail processing, or AI code in
this stage.

When Stage 2 is complete, stop and request a review. Do not automatically begin
Stage 3.

---

## Later stages

These stages are planned but must not be started early.

### Stage 3: Google authentication

Start only after PostgreSQL-backed manual tracking is stable.

Goal:

- create and configure a Google Cloud project;
- add Google sign-in;
- securely store the user and Google connection details;
- request only the minimum required scopes;
- support connecting and disconnecting Gmail.

The developer is new to Google Cloud and Google OAuth setup. Provide exact, current setup instructions in project documentation, including:

- where to create the project;
- which API to enable;
- how to configure the OAuth consent screen;
- how to create OAuth credentials;
- which redirect URI to add;
- which environment variables are required;
- how to keep secrets out of Git;
- common local-development errors.

Do not scan Gmail yet unless the user explicitly moves to Stage 4.

### Stage 4: Manual Gmail scan

Start only after Google authentication works.

Goal:

- add a user-triggered Gmail scan;
- search for likely job-related messages;
- retrieve only required message data;
- classify messages using deterministic rules;
- attach messages to existing applications or propose new applications;
- provide a review flow for uncertain detections;
- prevent duplicate processing.

Do not introduce an LLM in this stage.

### Stage 5: Incremental synchronisation

Start only after manual scans are reliable.

Goal:

- scan only new or changed messages;
- track sync progress;
- add scheduled backend processing;
- retry transient failures;
- expose understandable sync status to the user.

Do not add Pub/Sub push notifications unless polling becomes a real limitation.

### Stage 6: Calendar integration

Start only after Gmail synchronisation is stable.

Goal:

- request Calendar permissions separately;
- import interview-related events;
- match them to job applications;
- avoid duplicate timeline entries.

### Stage 7: AI assistance

Start only after ingestion, correction, and timeline flows work reliably.

Goal:

- improve extraction for emails that rules cannot classify;
- return structured output rather than free-form prose;
- record confidence and failures;
- let the user correct results;
- measure model cost;
- build a small evaluation dataset before relying on the feature.

Do not allow AI to delete data, send emails, or silently overwrite user corrections.

### Stage 8: Deployment

Start only after the local product is useful.

Goal:

- deploy the frontend, backend, and database;
- configure production secrets;
- add monitoring;
- add account deletion and privacy controls;
- complete any Google verification required for public users.

---

## Architecture rules

- Use a modular monolith.
- Keep frontend and backend responsibilities separate.
- The Java backend owns business logic, persistence, Google tokens, and Gmail processing.
- The browser must never store Google refresh tokens.
- Prefer package-by-feature organisation in the backend.
- Use REST JSON endpoints under `/api/v1`.
- Add database migrations through Flyway only once PostgreSQL is introduced.
- Keep automatic detections editable.
- Store only the minimum inbox data required for the product.
- Avoid logging secrets, tokens, or full email bodies.

---

## Dependency rules

Before adding a dependency, explain:

1. which current-stage requirement needs it;
2. why the standard library or existing dependencies are insufficient;
3. whether it can wait until a later stage.

Do not add dependencies for hypothetical future features.

Explicitly avoid until required:

- Redux or Zustand;
- Redis;
- Kafka or RabbitMQ;
- Kubernetes;
- vector databases;
- LLM frameworks;
- WebSockets;
- microservices;
- LinkedIn scraping libraries.

---

## Testing rules

For each stage:

- add tests for new domain behaviour;
- test API success and failure paths;
- keep tests small and readable;
- do not introduce heavy end-to-end infrastructure before it is useful;
- run the relevant test, lint, and type-check commands before declaring work complete.

Once PostgreSQL is introduced, prefer Testcontainers for backend integration tests instead of relying on an in-memory database that behaves differently.

---

## Coding rules

### General

- Prefer clear code over clever abstractions.
- Keep changes scoped to the current task.
- Do not refactor unrelated code.
- Remove dead code created by the change.
- Do not commit secrets or generated credentials.
- Update documentation when setup or behaviour changes.

### Frontend

- Use TypeScript strict mode.
- Keep server state separate from local UI state.
- Include loading, empty, and error states.
- Use accessible labels and semantic HTML.
- Do not add a state-management library until local React state and query caching are insufficient.

### Backend

- Use constructor injection.
- Validate API input.
- Return clear error responses.
- Keep controllers thin.
- Put business rules in application or domain services.
- Avoid exposing persistence entities directly as API responses.
- Make ingestion operations idempotent.

---

## How Codex should report completed work

At the end of each task, report:

- what changed;
- which files were added or modified;
- how to run the result;
- which checks were run;
- any manual setup still required;
- what remains for the current stage;
- whether the stage is complete.

Do not begin the next stage without an explicit request.

---

## Immediate next task

Stage 2 is complete. Stop and request a review before moving forward. Do not
begin Stage 3 without the user's explicit request.
