# ReLaunch — Product Spec v3.1 (FROZEN)

> Built by Team Firefly (Herman Li, Forrest Liu) for Hack for Her 2026.  
> AI re-entry coach for women returning to **accounting** after a career break.  
> Headline: **Return to accounting without starting over.**  
> MVP focus: Accounting only.

---

## 0. Constitution

These rules apply to every change and every AI response.

1. **Never invent.**  
   No experience, skills, dates, employers, credentials, or achievements that are not in the user's resume. Every claim about her past must be supported by her resume.

2. **Never state rules we can't verify.**  
   Do not generate specific accounting-standard, licensing, tax, CPD, or regulatory requirements. Credential guidance should always tell the user to confirm with the issuing body.

3. **Privacy first.**  
   Resume and job text are processed only for the current request. Nothing is stored. User text must never appear in logs. Logs may contain request ID, status, and latency only.

4. **Her time is real.**  
   Every comeback plan must fit within the number of hours per week the user says she has available.

5. **A career break is not a defect.**  
   Never describe a career break as a weakness, something to hide, or a reason to apologize.

6. **Explain every recommendation.**  
   Every READY / REFRESH / LEARN item must include a one-sentence reason.

7. **One phase at a time.**  
   Only touch files required for the current phase. Do not redesign or rewrite unrelated parts of the project.

---

# 1. Problem

A woman who worked in accounting may take time away because of parental leave, caregiving, health, relocation, or another personal reason.

When she wants to return, she may not know:

- which accounting skills still count;
- which tools or workflows need refreshing;
- what her target role requires that is genuinely new;
- whether her professional credentials need attention;
- how to prepare with only a few hours available each week.

Generic job-matching tools often treat skills that have not been used recently as missing.

ReLaunch takes a different approach:

> **A career break does not erase accounting experience.**

ReLaunch separates:

**READY** — skills the user already has and can still use.

**REFRESH** — skills she has used before but should revisit.

**LEARN** — skills required by the target job that are not supported by her previous experience.

---

# 2. Demo persona — Priya

Priya is a Senior Accountant with 6 years of professional experience.

Her previous experience includes:

- journal entries;
- month-end close;
- account reconciliations;
- financial statement preparation;
- variance analysis;
- audit support;
- Excel;
- Oracle ERP.

She holds a CPA designation.

She took a **24-month parental leave**.

She now wants to apply for a **Senior Accountant** position.

She can spend **5 hours per week** preparing for her return.

---

# 3. User flow

One page.

Three steps.

No login.

```text
INPUT
Tell us about your return

        ↓

MAP
Your Re-entry Map

        ↓

PLAN
Your Comeback Plan
```

---

## Step INPUT — Tell us about your return

| Field | Type | Rule |
|---|---|---|
| Your accounting experience / resume | textarea | required, 200–15000 chars |
| Target job description | textarea | required, 100–10000 chars |
| Time away | number, months | required, 0–240 |
| Reason for break | select | optional |
| Hours per week you can prepare | number | required, 1–40 |

Break-reason options:

- Prefer not to say — default
- Parental leave
- Caregiving
- Health
- Relocation
- Other

Helper text:

> Optional. We only use this to tailor your career-break wording if you choose to share it.

Buttons:

**Build my re-entry map**

**Load example**

Loading state:

> Reading your experience…

Validation errors appear beside the relevant field.

`Load example` fills Priya's inputs and loads the validated Priya fixture.

---

## Step MAP — Your Re-entry Map

Headline:

> **You don't need to relearn everything.**

Show three columns:

### READY

Skills supported by the user's previous accounting experience that remain directly relevant.

### REFRESH

Skills the user has previously used but where software, interfaces, workflows, or practices may have changed.

### LEARN

Skills required by the target job where the resume contains no evidence of previous experience.

Each card shows:

```text
Skill name

READY / REFRESH / LEARN

Why:
One-sentence explanation
```

READY and REFRESH cards also show:

> From your experience: "exact quote from resume"

Summary:

> **X of Y skills this role needs are already part of your experience.**

`X = READY + REFRESH`

Button:

**See my comeback plan**

---

## Credential card

If professional credentials are present:

### Before you apply: check your credentials

Show the credential name and:

> Confirm your current status and any continuing-education or renewal requirements with the issuing body. Requirements may change while you are away.

The product does **not** determine whether a credential is currently valid.

---

# 4. Step PLAN — Your Comeback Plan

Display a maximum four-week preparation plan.

Each week shows:

> **4h 30m of 5h available**

Each task shows:

- duration;
- skill;
- concrete task.

Example:

### Week 1

**4h 30m of 5h**

60 min — Excel  
Recreate a simple variance-analysis report using sample financial data.

60 min — Reconciliation  
Complete one sample bank reconciliation in Excel.

60 min — Month-end close  
Review a sample month-end close checklist.

45 min — NetSuite  
Explore the current interface and identify the basic month-end workflow.

45 min — Job preparation  
Review how your previous accounting experience maps to the target role.

---

## Talking about your break

Show a short first-person interview answer.

Include:

**Copy**

button.

---

## Handling personal interview questions

**Optional — build last.**

Static frontend content only.

Do not generate this section with AI.

Example:

**Question**

> How will you manage work with your family responsibilities?

Suggested redirect:

> I'm fully able to meet the schedule and responsibilities of this role. I'd be happy to talk more about how my accounting experience fits the position.

Small print:

> Interview rules vary by location. This is general career guidance, not legal advice.

Button:

**Start over**

---

# 5. API

## GET `/api/health`

Response:

```json
{
  "status": "ok"
}
```

---

## GET `/api/reentry/demo`

Returns the fixed, pre-validated Priya response.

This endpoint is:

- the Phase 1 fixture;
- the live-demo fallback;
- independent of the AI model.

It must always work even if the model API is unavailable.

---

## POST `/api/reentry/analyze`

Request:

```json
{
  "resumeText": "string",
  "jobText": "string",
  "breakMonths": 24,
  "breakReason": "PARENTAL_LEAVE",
  "hoursPerWeek": 5
}
```

Allowed `breakReason` values:

```text
PREFER_NOT_TO_SAY
PARENTAL_LEAVE
CAREGIVING
HEALTH
RELOCATION
OTHER
```

Response:

```json
{
  "skills": [
    {
      "name": "Month-end close",
      "status": "READY",
      "resumeQuote": "Prepared monthly journal entries and supported month-end close across three business units.",
      "reason": "Your resume shows direct experience with month-end close."
    }
  ],

  "credentials": [
    {
      "name": "CPA",
      "resumeQuote": "CPA (Chartered Professional Accountant), 2021"
    }
  ],

  "plan": [
    {
      "week": 1,
      "totalMinutes": 270,
      "blocks": [
        {
          "minutes": 60,
          "skill": "Excel",
          "task": "Recreate a simple variance-analysis report using sample financial data."
        }
      ]
    }
  ],

  "continueWith": [],

  "interview": {
    "breakStory": "string"
  }
}
```

---

# 6. Errors

Invalid input:

```text
400 INVALID_INPUT
```

Example:

```json
{
  "error": "INVALID_INPUT",
  "fields": {
    "hoursPerWeek": "Must be between 1 and 40."
  }
}
```

Model failure:

```text
502 ANALYSIS_FAILED
```

Includes:

- model unavailable;
- invalid model response;
- timeout;
- second JSON parse failure.

Frontend message:

> We couldn't complete the analysis right now. You can try again or load the example.

---

# 7. Classification rules

Only classify skills required by the **target job description**.

Maximum:

```text
12 skills
```

Professional credentials do not count toward this limit.

---

## LEARN

The target job requires the skill, but the resume contains no evidence of it.

```text
resumeQuote = null
```

---

## READY

The resume contains evidence of the skill AND:

```text
breakMonths <= 12
```

OR the skill belongs to the READY list.

---

## REFRESH

The resume contains evidence of the skill AND:

```text
breakMonths > 12
```

AND the skill belongs to the REFRESH list.

If a skill appears in neither list:

```text
default = REFRESH
```

The AI must not make its own confidence-based decision.

---

# 8. READY list — core accounting capabilities

**Forrest finalizes this list before Phase 3.**

Initial list:

- journal entries;
- general ledger;
- account reconciliation;
- month-end close;
- financial statement preparation;
- variance analysis;
- accounts payable;
- accounts receivable;
- accruals;
- cash-flow analysis;
- audit support;
- stakeholder communication.

These represent accounting concepts and capabilities that generally remain relevant even after time away.

---

# 9. REFRESH list — tools, systems, and changing workflows

**Forrest finalizes this list before Phase 3.**

Initial list:

- Oracle;
- SAP;
- NetSuite;
- QuickBooks Online;
- Sage;
- Workday;
- ERP systems;
- Excel;
- Power Query;
- Power BI;
- Tableau;
- automation tools;
- reporting systems.

Accounting-standard updates may also be suggested as a general refresh area, but ReLaunch must never generate specific regulatory or professional requirements.

---

# 10. Resume evidence rules

For every READY or REFRESH skill:

```text
resumeQuote != null
```

`resumeQuote` must be an exact substring of the original resume.

Maximum quote length:

```text
200 characters
```

If the server cannot verify the quote:

```text
status = LEARN

resumeQuote = null

reason =
"We couldn't find evidence of this skill in your experience."
```

Professional credentials must also contain an exact resume quote.

If the credential quote cannot be verified:

```text
remove credential
```

---

# 11. Plan rules

Priority:

```text
REFRESH
↓
LEARN
↓
Resume update
```

READY skills receive no study blocks.

Allowed task durations:

- 30 minutes;
- 45 minutes;
- 60 minutes.

Weekly total must satisfy:

```text
totalMinutes <= hoursPerWeek × 60
```

Maximum plan length:

```text
4 weeks
```

If every skill cannot fit:

```json
{
  "continueWith": [
    "remaining skill"
  ]
}
```

Skills in `continueWith` follow job-description order.

---

## Task-writing rules

Tasks must be small and concrete.

Good:

> Complete one sample bank reconciliation in Excel.

Good:

> Prepare three sample month-end journal entries.

Good:

> Recreate a simple variance-analysis report using sample financial data.

Good:

> Review a sample month-end close checklist.

Good:

> Rebuild a basic account reconciliation schedule.

Good:

> Explore the current NetSuite interface and identify the basic month-end workflow.

Bad:

> Learn accounting.

Bad:

> Improve Excel.

Bad:

> Study NetSuite.

---

## Final week

Include one block:

> Update your resume with skills you refreshed and can now confidently discuss.

---

## Optional accounting refresh

If the user has been away for an extended period, the plan **may** recommend:

> Review accounting standards or professional guidance that changed while you were away using the relevant standard-setter or professional body's official resources.

This is **not mandatory** for the MVP.

Do not name specific standards or requirements unless verified externally.

---

# 12. Break-story rules

The story must be:

- 3–4 sentences;
- first person;
- honest;
- concise;
- not apologetic.

Mention the break reason only when:

```text
breakReason != PREFER_NOT_TO_SAY
```

Never claim that the user:

- studied;
- freelanced;
- volunteered;
- completed courses;
- refreshed skills;

during the break unless the resume or user input explicitly says so.

Refreshing should be described as present or future activity:

> I'm now refreshing…

not:

> During my break I refreshed…

Move quickly from the break to:

- previous accounting experience;
- relevant capabilities;
- readiness to return.

---

## Neutral story template

When the break reason should not be mentioned:

> I took time away from full-time work. Before my break, I built experience in {top 3 READY skills}. I'm now refreshing {top REFRESH skills}, and I'm ready to bring my experience back to an accounting team.

---

# 13. Server-side validation

Implement validation as **pure functions** wherever possible.

Input:

```text
request + parsed model response
```

Output:

```text
corrected validated response
```

This allows testing without calling the model.

---

## 1. JSON validation

Strip code fences if necessary.

Parse JSON.

Validate against response schema.

If invalid:

```text
retry once
```

If invalid again:

```text
502 ANALYSIS_FAILED
```

---

## 2. Skill evidence validation

Normalize whitespace.

For every READY / REFRESH skill:

```text
resumeQuote must exist in resumeText
```

Otherwise:

```text
status = LEARN
resumeQuote = null
```

---

## 3. Classification re-check

The server re-applies the READY / REFRESH rules using:

- `breakMonths`;
- READY list;
- REFRESH list.

If model classification disagrees:

```text
server classification wins
```

---

## 4. Credential validation

Credential quote must exist in the resume.

Otherwise:

```text
remove credential
```

---

## 5. Time-budget validation

Recalculate every week's:

```text
totalMinutes
```

If:

```text
totalMinutes > hoursPerWeek × 60
```

remove blocks from the end until it fits.

Removed or unplanned skills should appear in:

```text
continueWith
```

---

## 6. Privacy validation

If:

```text
breakReason = PREFER_NOT_TO_SAY
```

the story must not contain inferred reasons such as:

```text
parental
maternity
caregiving
caring for
health
illness
relocation
```

If detected:

```text
replace with neutral template
```

---

# 14. Model prompt

Temperature:

```text
0.2
```

## System

```text
You are a career re-entry coach for women returning to accounting roles.

Return ONLY valid JSON matching the provided schema.

No markdown.
No additional prose.

Rules:

- Never invent experience.
- Every statement about the user's past must come from the RESUME.
- resumeQuote must be copied character-for-character from the RESUME.
- Follow the READY / REFRESH / LEARN rules and lists exactly.
- Follow the weekly time budget.
- Never describe a career break as a weakness.
- Never state specific accounting-standard, licensing, tax, regulatory, CPD, or continuing-education requirements.
- If credential requirements may matter, tell the user to confirm them with the issuing body.
```

## User

```text
RESUME:

{resumeText}


TARGET ACCOUNTING JOB:

{jobText}


BREAK MONTHS:

{breakMonths}


BREAK REASON:

{breakReason}


AVAILABLE HOURS PER WEEK:

{hoursPerWeek}


ACCOUNTING CLASSIFICATION RULES:

{classificationRules}


PLAN RULES:

{planRules}


BREAK STORY RULES:

{breakStoryRules}


OUTPUT SCHEMA:

{responseSchema}
```

---

# 15. Demo fixture — Priya

**Forrest reviews this for accounting realism before Phase 1 ends.**

## Resume

```text
Priya Sharma — Senior Accountant, CPA

Harbourline Services, Vancouver
Accountant / Senior Accountant
June 2018 – August 2024

- Prepared monthly journal entries and supported month-end close across three business units.
- Completed bank, balance-sheet, and general-ledger account reconciliations.
- Prepared monthly financial statements and variance-analysis reports for management.
- Used Oracle ERP and Excel for accounting reports and account analysis.
- Supported annual external audits by preparing schedules and responding to auditor requests.
- Worked with department managers to investigate expense and budget variances.

Education:
Bachelor of Commerce — Accounting

Designation:
CPA (Chartered Professional Accountant), 2021
```

Break:

```text
September 2024 – present
Parental leave
24 months
```

Available:

```text
5 hours/week
```

---

## Target job

```text
Senior Accountant

Responsibilities and requirements:

- Prepare journal entries and support month-end close.
- Complete balance-sheet and bank reconciliations.
- Prepare financial statements and variance reports.
- Strong Microsoft Excel skills.
- Experience with NetSuite is preferred.
- Experience with Power BI is an asset.
- Support external audit requests.
- CPA designation preferred.
```

---

# 16. Expected demo result

### READY

- journal entries;
- month-end close;
- account reconciliations;
- financial statements;
- variance analysis;
- audit support.

### REFRESH

- Excel.

### LEARN

- NetSuite;
- Power BI.

### Credential

- CPA.

Every READY / REFRESH skill and CPA must include an exact resume quote.

Every week:

```text
<= 300 minutes
```

The final week includes the resume-update block.

---

# 17. Test cases

| # | Test | Expected |
|---|---|---|
| 1 | Priya fixture, live model | Matches expected classification |
| 2 | Priya with `breakMonths = 6` | Existing Excel experience may classify READY |
| 3 | `hoursPerWeek = 1` | Every week ≤ 60 minutes; overflow goes to `continueWith` |
| 4 | `breakMonths = 60` | Core accounting skills READY; Excel REFRESH |
| 5 | `PREFER_NOT_TO_SAY` | Story contains no inferred break reason |
| 6 | Resume under 200 chars | 400 validation error |
| 7 | CPA line removed | `credentials` empty |
| 8 | Target requires NetSuite but resume does not contain it | NetSuite = LEARN |
| 9 | Fake model response contains Oracle quote not present in resume | Validator rejects evidence |
| 10 | Fake response totals 400 minutes with 5 hours/week | Validator trims to ≤ 300 |
| 11 | Model unavailable | Error shown; Load Example still works |

---

# 18. Out of scope

Do NOT build:

- login;
- accounts;
- database;
- file upload;
- PDF parsing;
- DOCX parsing;
- RAG;
- embeddings;
- saved reports;
- email;
- analytics;
- credential verification;
- CPA renewal checking;
- tax advice;
- legal advice;
- finance roles outside accounting;
- FP&A-specific coaching;
- investment banking;
- wealth management.

The MVP focuses only on:

> **Women returning to accounting roles.**

---

# 19. Tech

## Frontend

```text
Vue 3
Vite
Plain CSS
```

No router.

`App.vue` holds:

```text
step =
INPUT
MAP
PLAN
```

Components:

```text
ReturnProfile.vue
ReentryMap.vue
ComebackPlan.vue
```

---

## Backend

```text
Java 21
Spring Boot
Spring AI
Existing DashScope provider
```

API key:

```text
environment variable only
```

No database.

---

## Repository

New repository:

```text
/frontend
/backend
```

Vite development server proxies:

```text
/api
```

to the Spring Boot backend.

---

## Deployment

Use:

```text
Docker Compose
Existing DigitalOcean server
Nginx
New subdomain
```

Nginx:

- serves the frontend build;
- forwards `/api` to Spring Boot.

CareerPilot build/deployment configuration may be used as reference.

All ReLaunch:

- product logic;
- prompts;
- UI;
- domain rules;
- validation;

must be written specifically for this hackathon.

---

# 20. Team roles

## Herman Li

**Full-stack development and AI integration**

Responsible for:

- Vue frontend;
- Spring Boot backend;
- AI integration;
- response validation;
- deployment;
- complete demo flow.

---

## Forrest Liu

**Accounting domain expertise, testing, and backend support**

Responsible for:

- reviewing and finalizing READY accounting skills;
- reviewing and finalizing REFRESH tools / workflows;
- checking accounting terminology;
- reviewing Priya's resume and target job for realism;
- reviewing expected READY / REFRESH / LEARN classifications;
- providing 5–6 realistic accounting comeback tasks;
- reviewing AI-generated plans for accounting realism;
- running acceptance tests;
- backend support where needed.

Forrest does **not** need to review:

- frontend architecture;
- deployment;
- AI provider configuration;
- overall application architecture.

---

# 21. Build phases

Test after every phase.

Commit after every phase.

---

## Phase 0 — Skeleton

Owner:

**Herman**

Build:

```text
Vue frontend
Spring Boot backend
GET /api/health
```

Done when:

```text
Frontend loads locally
GET /api/health → {"status":"ok"}
```

---

## Phase 1 — Fixture-first full flow

Owner:

**Herman**

Build all three steps using the fixed Priya fixture.

For now:

```text
POST /api/reentry/analyze
```

may return the same fixture response as:

```text
GET /api/reentry/demo
```

Required flow:

```text
Load Example
↓
Build my re-entry map
↓
READY / REFRESH / LEARN
↓
See my comeback plan
↓
Start over
```

Done when:

> Full user flow works without any AI model.

---

## Phase 2 — Deploy immediately

Owner:

**Herman**

Deploy the fixture-based product to the public subdomain.

Done when:

> The public URL successfully completes the Priya flow.

At this point:

> **The project is already submittable.**

Do not delay deployment waiting for AI integration.

---

## Phase 3 — Real AI analysis

Owner:

**Herman**

Implement:

- DashScope / Spring AI model call;
- JSON parsing;
- response schema;
- resume evidence validation;
- server-side READY / REFRESH rule re-check;
- credentials extraction.

Required tests:

```text
1
6
8
9
```

---

## Phase 4 — Comeback plan + break story

Owner:

**Herman**

Implement:

- weekly comeback plan;
- time-budget enforcement;
- `continueWith`;
- break-story rules;
- privacy handling.

Required tests:

```text
2
3
4
5
7
10
```

---

## Phase 5 — Accounting realism review

Owner:

**Forrest**

Review the live output.

Check:

- READY classifications;
- REFRESH classifications;
- LEARN classifications;
- accounting terminology;
- Priya example realism;
- task realism;
- credential behavior.

Report incorrect classifications or unrealistic tasks to Herman.

Do not add major new features.

---

## Phase 6 — Polish

Only if time remains.

Possible work:

- loading state;
- error state;
- responsive/mobile layout;
- static personal-interview-question section;
- minor visual improvements.

Required:

```text
Test 11
```

---

## Phase 7 — Feature freeze

Owners:

**Both**

Stop adding features.

Complete:

- demo video;
- poster;
- pitch deck;
- submission form;
- final rehearsal.

Only bug fixes after freeze.

---

# 22. Forrest parallel tasks

Forrest starts these while Herman works on Phase 0–2.

### Task 1

Finalize:

```text
READY accounting skills
REFRESH tools/workflows
```

Deliver before Phase 3.

### Task 2

Review:

```text
Priya resume
Senior Accountant job description
Expected READY / REFRESH / LEARN result
```

Deliver before Phase 1 ends.

### Task 3

Provide **5–6 realistic 30–60 minute accounting comeback tasks**.

Examples:

```text
Complete a sample bank reconciliation in Excel.

Prepare three sample month-end journal entries.

Recreate a simple variance-analysis report using sample data.

Review a sample month-end close checklist.

Rebuild a basic account reconciliation schedule.

Explore the current NetSuite interface and identify the basic month-end workflow.
```

### Task 4

After real AI is available:

run the accounting acceptance tests and report:

- incorrect classifications;
- unrealistic accounting terminology;
- unrealistic preparation tasks.

---

# 23. Cut order

If time becomes limited, cut features in this order:

1. Static personal-interview-question section
2. Mobile polish
3. Optional accounting-standards refresh suggestion
4. Retry after invalid JSON
5. Break-reason leak detector
6. Credentials card
7. Live AI

If live AI is not reliable:

> Use the validated Priya fixture and explain honestly that the demo is running from the validated example.

---

## Never cut

```text
READY / REFRESH / LEARN

hours-per-week budget

Load Example

Priya fixture

server-side resume evidence validation

accounting-focused positioning
```

---

# 24. Freeze rule

After Phase 6 — or earlier if time requires — only fix bugs.

Reserve at least several hours for:

- recording the demo;
- testing the public link;
- pitch deck;
- submission;
- rehearsal.

Do not keep adding features until the submission deadline.

---

# 25. Demo-day fallback

If the real AI call:

- stalls;
- times out;
- returns invalid data;
- fails because of network/API issues;

do not debug it during the presentation.

Immediately use:

> **Load example**

or switch to the recorded demo.

---

# 26. Product message

## Headline

> **Return to accounting without starting over.**

## Supporting message

> Your experience still counts. ReLaunch shows you what is ready, what needs refreshing, and what you actually need to learn before returning to work.

## Core principle

> **A career break does not erase your accounting experience.**