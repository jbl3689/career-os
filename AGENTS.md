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

**Stage 3: Google authentication**

Stages 0, 1, and 2 are complete. The current goal is:

> Let a user sign in with Google and optionally connect Gmail without exposing
> Google credentials to the browser or scanning any email yet.

### Stage 3 deliverables

- Google Cloud project setup documentation;
- OAuth consent screen configuration;
- Spring Security OAuth2 client setup;
- persisted Career OS users;
- server-side user session handling;
- encrypted refresh-token storage;
- separate connect and disconnect Gmail controls;
- authentication tests and troubleshooting documentation.

### Stage 3 progress

- complete: minimal user, session, and Google-connection design;
- complete: current Google Auth Platform local-setup documentation;
- complete: developer created the development Google Cloud project and OAuth client;
- complete: user schema and ownership migration;
- complete: basic Google sign-in using identity scopes only;
- complete: authenticated API session, CSRF protection, and frontend state;
- pending: developer verifies the real Google sign-in flow locally;
- pending: separate Gmail connection and encrypted token storage;
- pending: disconnect flow, tests, and final troubleshooting documentation.

Do not read, classify, or import Gmail messages in this stage. Do not add Google
Calendar or AI code.

When Stage 3 is complete, stop and request a review. Do not automatically begin
Stage 4.

---

## Later stages

These stages are planned but must not be started early.

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

Ask the developer to load the private Google client values using the README and
verify the identity-only sign-in flow locally. Fix any configuration issue
before starting the separate Gmail connection and encrypted-token slice. Do not
request a Gmail scope yet.
