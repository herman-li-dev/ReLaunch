# Phase 3 provider handoff

**Date:** 2026-09-27
**Status:** The Phase 3 DashScope provider path is implemented and tested offline. It has not been deployed, no real AI call has been made, and public traffic must remain on the validated fixture release until separately authorized.

## Authorized Forrest decisions

- [x] The frozen spec §8 initial READY list is the final production list: journal entries; general ledger; account reconciliation; month-end close; financial statement preparation; variance analysis; accounts payable; accounts receivable; accruals; cash-flow analysis; audit support; stakeholder communication.
- [x] The frozen spec §9 initial REFRESH list is the final production list: Oracle; SAP; NetSuite; QuickBooks Online; Sage; Workday; ERP systems; Excel; Power Query; Power BI; Tableau; automation tools; reporting systems.
- [x] Priya’s §16 classifications are confirmed: six READY, Excel REFRESH, NetSuite and Power BI LEARN, and CPA.
- [x] CPA evidence is the exact original resume substring `CPA (Chartered Professional Accountant), 2021`; credential wording remains “confirm with the issuing body.” ReLaunch does not verify status or state licensing, CPD, tax, or regulatory requirements.
- [x] READY/REFRESH evidence must be an exact original-resume substring (at most 200 characters); LEARN evidence is `null`.

## Implemented provider boundary

- `RawAnalysisProvider` remains provider-neutral. `DashScopeRawAnalysisProvider` owns all DashScope/Spring AI prompt and option details.
- Only `POST /api/reentry/analyze` invokes `AnalysisService`. `GET /api/reentry/demo` and `GET /api/reentry/examples/maya-junior-accountant` always use fixed fixtures and never need model availability.
- The adapter uses `qwen-plus`, temperature `0.2`, `JSON_OBJECT`, synchronous non-streaming output, empty tools/callbacks, no search, and no RAG implementation.
- The full frozen system/user prompt supplies resume, target job, break inputs, finalized classification lists/rules, plan rules, break-story rules, and response schema. It does not log any of that content.
- The processor accepts whitespace-normalized matching only to locate evidence, then returns the corresponding literal substring from the original resume. Unverifiable skills become LEARN; unverifiable credentials are removed.
- Invalid JSON/schema is retried once by `AnalysisService`. Provider exceptions, timeouts, empty output, or two invalid responses map to `502 {"error":"ANALYSIS_FAILED"}` without leaking detail.

## Configuration and safety

- Dependency matrix verified from the official Spring AI Alibaba 1.0.0.2 guidance: Spring Boot 3.4.5, Spring AI 1.0.0, Spring AI Alibaba 1.0.0.2, and `spring-ai-alibaba-starter-dashscope`.
- The official properties are `spring.ai.dashscope.api-key`, `spring.ai.dashscope.base-url`, and `spring.ai.dashscope.read-timeout`. The project maps only `DASHSCOPE_API_KEY` (and optional `DASHSCOPE_BASE_URL`) into those properties; no value is tracked or logged.
- Version 1.0.0.2 auto-configures unrelated DashScope components and fails application startup when a key is absent. The app explicitly excludes those starter auto-configurations and creates only the synchronous chat model when a key is present. This preserves fixture GET routes when no key is configured.
- `spring.ai.dashscope.base-url` is configurable. Its default matches the resolved 1.0.0.2 library default, `https://dashscope.aliyuncs.com`; an international/Singapore endpoint must be separately verified for this provider API shape before use.
- Docker Compose passes the environment variable names only: `DASHSCOPE_API_KEY` and `DASHSCOPE_BASE_URL`.

## Offline verification

- `AnalysisResponseProcessorTest`: parser/schema, exact original evidence and credential quotes, classification, max-skill count, and evidence rejection.
- `AnalysisServiceTest`: exactly one invalid-response retry, provider failure safety, and 502 mapping.
- `DashScopeRawAnalysisProviderTest`: prompt sections and DashScope options using a mocked `ChatModel`; no network/model invocation.
- `ReentryApiTest`: both fixtures retain their fixed contracts; with no API key the application starts and POST returns the safe 502 response.

## Held back

Phase 4 is not implemented: no plan trimming, `continueWith` repair, break-story privacy rewrite, or changed fixture plan/story semantics. This Phase 3 implementation has not been remotely deployed or exercised against public live traffic. A live-provider smoke test requires separate user authorization.

## Official references

- [Spring AI Alibaba 1.0.0.2 component and compatibility guidance](https://www.java2ai.com/en/docs/1.0.0.2/tutorials/starters-and-quick-guide/)
- [Spring AI Alibaba compatibility FAQ](https://java2ai.com/docs/1.0.0.2/faq/)
- [Alibaba Cloud API-key guidance](https://help.aliyun.com/en/model-studio/get-api-key)
