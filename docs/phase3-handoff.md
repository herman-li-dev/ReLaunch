# Phase 3 provider handoff — pending Forrest/Root confirmation

**Date:** 2026-09-26  
**Status:** A handoff record pending Forrest and Root confirmation. It does **not** mean a real AI provider is integrated or authorized.

Current state: the provider-independent `analysis` slice validates/parses raw responses but is not wired to `ReentryController`. `GET /api/reentry/demo` and `POST /api/reentry/analyze` still return the fixed Priya fixture. Do not change that fixture behavior until the gates below are approved.

## Forrest decision checklist

### Classification candidates (not final)

- [ ] Finalize or revise the initial READY candidates: journal entries; general ledger; account reconciliation; month-end close; financial statement preparation; variance analysis; accounts payable; accounts receivable; accruals; cash-flow analysis; audit support; stakeholder communication.
- [ ] Finalize or revise the initial REFRESH candidates: Oracle; SAP; NetSuite; QuickBooks Online; Sage; Workday; ERP systems; Excel; Power Query; Power BI; Tableau; automation tools; reporting systems.
- [ ] Confirm Priya’s resume, Senior Accountant job description, and accounting terminology are realistic.
- [ ] Confirm or correct the expected Priya result (the named classifications matter, not only the counts):
  - READY: journal entries; month-end close; account reconciliations; financial statements; variance analysis; audit support.
  - REFRESH: Excel.
  - LEARN: NetSuite; Power BI.
  - Credential: CPA, evidenced by `CPA (Chartered Professional Accountant), 2021`.
- [ ] Confirm or correct these six candidate accounting comeback tasks, each scoped to 30, 45, or 60 minutes:
  - Complete one sample bank reconciliation in Excel.
  - Prepare three sample month-end journal entries.
  - Recreate a simple variance-analysis report using sample financial data.
  - Review a sample month-end close checklist.
  - Rebuild a basic account reconciliation schedule.
  - Explore the current NetSuite interface and identify the basic month-end workflow.
- [ ] Confirm READY/REFRESH evidence uses an exact resume quote and LEARN evidence is `null`.
- [ ] Confirm each Priya plan week is at most 300 minutes.
- [ ] Confirm the final week contains the resume-update task.
- [ ] Confirm credential wording remains “confirm with the issuing body”; ReLaunch must not verify credentials or state accounting, licensing, CPD, tax, or regulatory requirements.

## Provider integration plan and gates

Proceed in this order. A later step is blocked until the preceding one has passed review and regression checks.

1. **Fix whitespace normalization in `AnalysisResponseProcessor`.** Align server-side quote verification with the frozen whitespace-normalization rule while preserving the requirement that accepted evidence is traceable to the original resume. Add deterministic edge-case tests before provider wiring.
2. **Complete the required security dependency upgrade, then run the full regression suite.** Do not add a provider starter until the upgrade is reviewed and tests/package/build are green.
3. **Choose and verify a compatible dependency matrix.** The project currently uses Spring Boot **3.4.5**. The official Spring AI Alibaba main branch currently declares Boot **3.5.8**, Spring AI **1.1.2**, and extension **1.1.2.2**. This is not evidence of direct compatibility with this project; select and verify a compatible matrix before adding any starter.
4. **Keep `RawAnalysisProvider` provider-neutral.** The existing application-facing boundary remains provider independent; no Alibaba, DashScope, Spring AI, model, region, or base-URL detail belongs in `AnalysisService` or `AnalysisResponseProcessor`.
5. **Add a DashScope adapter only after the gates above.** Only that adapter may use DashScope/Spring AI Alibaba. Use the Alibaba Cloud-recommended environment-variable name `DASHSCOPE_API_KEY` (name only; never record a value) and map it to Spring AI Alibaba `spring.ai.dashscope.api-key`.
6. **Lock provider settings before implementation.** Choose the model, region, and base URL from verified provider documentation/configuration; do not guess. Configure temperature `0.2`, JSON-only output, no tools, no RAG, no streaming, and a finite timeout.
7. **Wire routes deliberately.** `GET /api/reentry/demo` remains fixture-based and independent forever. Only `POST /api/reentry/analyze` may call the service after provider approval.
8. **Preserve bounded failure handling.** Invalid JSON/schema receives at most one retry. Provider/timeout failures do not retry indefinitely and map safely to `502 {"error":"ANALYSIS_FAILED"}`.
9. **Preserve privacy.** Never log request bodies, resumes, job text, raw model output, or keys. Logs may contain only request ID, status, and latency.
10. **Keep tests offline by default.** Deterministic fake-provider tests are the default. A live-provider smoke test requires separate user authorization.

## Scope held back

This handoff does not authorize or include login, database, uploads, RAG, Phase 4 plan trimming/`continueWith`/break-story privacy semantics, remote deployment, commits, or pushes.

## Root acceptance checks

- [ ] Unit tests cover parser, quote/credential handling, parameterized classification, retry bounds, provider failure, and 502 mapping.
- [ ] Backend package/build passes after the selected dependency matrix is added.
- [ ] Docker smoke verifies the fixture flow through the local deployment boundary.
- [ ] Priya fixture remains 6 READY / 1 REFRESH / 2 LEARN with CPA.
- [ ] Log review confirms no resume, job text, request body, raw provider output, or key leakage.
- [ ] `ANALYSIS_FAILED` remains a safe 502 response.
- [ ] Security dependencies are rescanned after the upgrade.
- [ ] Root performs the final diff and frozen-spec review before merging/deploying.

## Official references

- [Spring AI Alibaba DashScope](https://java2ai.com/en/integration/chatmodels/dashScope/)
- [Spring AI ChatClient](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
- [Alibaba Cloud API key guidance](https://help.aliyun.com/en/model-studio/get-api-key)
- [Spring AI Alibaba project](https://github.com/alibaba/spring-ai-alibaba)
