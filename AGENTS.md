# ReLaunch agent workflow

`spec.md` — **ReLaunch Product Spec v3.1 (FROZEN)** — is the source of truth. Read it completely before any project change. Preserve every specified API route, schema, error, classification, exact-resume-evidence, time-budget, privacy/logging, and phase contract unless Herman explicitly authorizes a spec change.

## Current repository state

- Phase 1 has a fixture-first `/frontend` and `/backend` implementation with the validated Priya flow and an explicitly authorized Junior Accountant fixed example. Forrest's accounting-realism decisions are recorded in `docs/forrest-review.md`.
- Phase 2 is publicly deployed at `https://relaunch.hermanlidev.com` from the source tree now recorded by commit `379a924`. The server release directory retains the pre-GitHub, email-only-history-rewrite label `dd69b03`; both commits have the same Git tree. The deployment uses the `relaunch` Compose project, binds the frontend only to `127.0.0.1:8088`, keeps the backend internal, and terminates TLS in the host Nginx virtual host. Operational details are in `deploy/README.md`.
- This directory is a Git repository on `main`. Inspect the current Git state before making claims about the active commit, cleanliness, or later changes. Do not push without Herman's explicit authorization.
- Phase 3 provider wiring is locally implemented: only `POST /api/reentry/analyze` may invoke the provider-neutral analysis service; `GET /api/reentry/demo` and the Junior Accountant example remain fixed, validated fixtures independent of model availability. Without `DASHSCOPE_API_KEY`, POST must safely return `502 ANALYSIS_FAILED` while both fixtures still work.
- Forrest has finalized the spec §8 READY and §9 REFRESH lists, plus Priya §16 classifications, CPA evidence, and credential wording for Phase 3. DashScope details remain isolated to `backend/.../analysis` adapter/configuration; no key value may be committed or logged. Offline tests only unless Herman separately authorizes a live call.
- Do not work ahead of the phase authorized by the user. Phase 4 remains unauthorized: do not change plan trimming, `continueWith`, or break-story privacy semantics; do not deploy or push without separate explicit authorization.
## Verified project commands

Run from `frontend/`:

- `npm test`
- `npm run build`

Run from `backend/` on Windows:

- `.\mvnw.cmd test`
- `.\mvnw.cmd package`

Run from the repository root for Phase 2:

- `docker compose config`
- `docker compose build`
- `docker compose up -d` (starts local services; requires explicit authorization)
- `docker compose down` (removes only this project's Compose containers and network)

Update this section only when actual project manifests or wrappers change. Never invent commands or test results.

## Three-agent workflow

**Root** owns scope, sequencing, and final decisions. Root reads the frozen spec, asks the sentry for an independent read-only review before material changes, gives laborer a narrow file/task boundary, then reviews the result against the spec and current phase.

**Sentry** is read-only: inspect the spec and relevant files, report contract/phase/privacy/security risks and verification gaps, and propose no edits. Sentry never creates, modifies, deletes, commits, pushes, deploys, changes branches, installs dependencies, or starts services.

**Laborer** makes only Root-authorized, bounded edits. Read the applicable spec sections first; preserve unrelated user changes; stop and report conflicts, missing authority, or ambiguity. Laborer must not expand scope or alter frozen contracts.

Flow: Root scopes → sentry reviews → Root delegates bounded work → laborer edits/verifies → Root performs final spec/phase review. Keep work within files required by the active phase.

## Non-negotiable product boundaries

- Follow the constitution: never invent user history; never state unverified accounting/licensing/tax/CPD/regulatory rules; treat career breaks respectfully; explain each READY/REFRESH/LEARN recommendation; and honor available weekly time.
- Privacy: process resume/job text only for the request; do not store it or include user text in logs. Logs may contain only request ID, status, and latency.
- Frozen architecture: Vue 3 + Vite + plain CSS frontend (no router); Java 21 + Spring Boot + Spring AI/DashScope backend; no database; API key only in environment variables; `/frontend` and `/backend`; deployment shape is Docker Compose, existing DigitalOcean server, Nginx, and a new subdomain.
- AI/server validation stays bounded by the spec: classify only target-job skills (max 12), use exact resume quotes (max 200 chars), server re-checks classification/evidence/credentials/time/privacy, and returns only specified errors. The Priya fixture/demo endpoint remains independent of model availability.
- Forrest owns accounting-domain review: READY/REFRESH lists, Priya/accounting realism, comeback tasks, and acceptance review. Do not treat Forrest as reviewer for frontend architecture, deployment, AI-provider configuration, or overall architecture.
- Out of scope: login/accounts/database; file/PDF/DOCX parsing; RAG/embeddings; saved reports/email/analytics; credential verification or renewal checking; tax/legal advice; and non-accounting, FP&A, investment-banking, or wealth-management coaching.

## Change controls

- Enforce phase discipline and test after each implemented phase where actual project commands exist. Do not add features after freeze except authorized bug fixes.
- Never commit, push, deploy, create/change branches, install dependencies, start services, delete/reset/overwrite broadly, or perform destructive actions without explicit user authorization.
- Preserve existing user work. Report changed files, verification performed, and residual risks.
