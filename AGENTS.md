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

**Stage 4B: Email-driven application updates**

Stages 0, 1, 2, and 3 are complete. Stage 4A established the manual Gmail
scan foundation. Developer review found that linking read-only results does not
yet create enough product value. The current goal is:

> Let a user understand a likely job email, correct the proposed action, and
> confirm it as a useful application or timeline update.

### Stage 4A progress

- complete: manual scan using token refresh, a capped recent
  Gmail search, and minimal candidate metadata;
- complete: persisted email-message and scan-result schema;
- complete: idempotent storage keyed by user and Gmail message ID;
- complete: deterministic metadata classification with explainable rule scores;
- complete: conservative application-match suggestions and a user-confirmed
  review queue for suggested, uncertain, and unmatched messages;
- complete: Stage 4A backend, frontend, migration, and documentation checks.

### Stage 4B deliverables

- retrieve and display a short excerpt only for likely candidate messages;
- clearly show the detected event type, confidence, and explanation;
- let the user mark a message as not job-related without allowing a later scan
  to rediscover it;
- let the user correct the application and event type before confirmation;
- support creating an application from an unmatched review using editable
  company and role fields;
- make confirmation create exactly one email-sourced timeline event;
- update application status and last activity only as part of an explicit,
  understandable confirmation;
- prevent duplicate events when a Gmail message is processed again;
- display email-sourced events on the application detail timeline.

### Stage 4B progress

- complete: normalized message excerpts capped at 500 characters, excerpt-aware
  deterministic classification, and useful classification details in the UI;
- complete: a persisted not-job-related decision that immediately removes the
  message from the review queue and visible scan results;
- complete: deterministic company and role proposals plus an editable,
  user-confirmed application creation flow for unmatched reviews;
- pending: editable review actions;
- pending: confirmed creation and update behaviour;
- pending: idempotent email-sourced timeline events;
- pending: application timeline presentation and Stage 4B verification.

Stage 4A's technical foundation is complete, but Stage 4 is not product-complete
until Stage 4B is reviewed and accepted.

Do not schedule scans or add Gmail push notifications in this stage. Do not add
Google Calendar, an LLM, or AI classification. Begin with user-confirmed actions;
do not silently create applications or change statuses.

When Stage 4B is complete, stop and request a review. Do not automatically begin
Stage 5.

---

## Later stages

These stages are planned but must not be started early.

### Stage 5: AI-assisted extraction

Start only after deterministic review-to-update behaviour is reliable.

Goal:

- improve extraction for emails that rules cannot classify;
- return structured company, role, and event data rather than free-form prose;
- record confidence, costs, model versions, and failures;
- let the user correct results;
- build a small evaluation dataset from real corrections before relying on the
  feature.

Do not allow AI to delete data, send emails, or silently overwrite user
corrections.

### Stage 6: Incremental synchronisation

Start only after manual email-driven updates are useful and reliable.

Goal:

- scan only new or changed messages;
- track sync progress;
- add scheduled backend processing;
- retry transient failures;
- expose understandable sync status to the user.

Do not add Pub/Sub push notifications unless polling becomes a real limitation.

### Stage 7: Calendar integration

Start only after Gmail synchronisation is stable and Calendar is a higher-value
priority than improving the application tracker.

Goal:

- request Calendar permissions separately;
- import interview-related events;
- match them to job applications;
- avoid duplicate timeline entries.

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

Manually review application creation with real unmatched Gmail results. Confirm
that weak company or role guesses stay blank, useful guesses are editable, and
the created application appears once on the dashboard. Do not begin
email-sourced timeline events or existing-application status updates until the
developer accepts this slice.
