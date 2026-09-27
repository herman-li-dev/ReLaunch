package com.relaunch.analysis;

import com.alibaba.cloud.ai.dashscope.api.DashScopeResponseFormat;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.relaunch.api.AnalyzeRequest;
import java.util.List;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * DashScope-only implementation of the provider-neutral raw-output boundary.
 * It deliberately returns untrusted JSON text for {@link AnalysisResponseProcessor} to validate.
 */
public final class DashScopeRawAnalysisProvider implements RawAnalysisProvider {
    private static final String SYSTEM_PROMPT = """
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
            """;

    private static final String PLAN_RULES = """
            Prioritize REFRESH, then LEARN, then a resume update. READY skills receive no study blocks.
            Use only 30, 45, or 60 minute blocks. Each week must not exceed AVAILABLE HOURS PER WEEK × 60.
            Use at most four weeks. Put unplanned skills in continueWith in target-job order.
            The final week includes: Update your resume with skills you refreshed and can now confidently discuss.
            Tasks must be small and concrete. Do not state unverified accounting, licensing, CPD, tax, or regulatory requirements.
            """;

    private static final String BREAK_STORY_RULES = """
            The break story is 3–4 first-person, concise, non-apologetic sentences.
            Mention the break reason only when it is not PREFER_NOT_TO_SAY.
            Never claim the person studied, freelanced, volunteered, completed courses, or refreshed skills during the break without source evidence.
            Describe refreshing as present or future activity. Move quickly from the break to previous accounting experience and readiness to return.
            If PREFER_NOT_TO_SAY, do not infer parental leave, maternity, caregiving, health, illness, or relocation.
            """;

    private static final String RESPONSE_SCHEMA = """
            {
              "skills": [{"name":"string","status":"READY|REFRESH|LEARN","resumeQuote":"string|null","reason":"string"}],
              "credentials": [{"name":"string","resumeQuote":"string"}],
              "plan": [{"week":1,"totalMinutes":0,"blocks":[{"minutes":30,"skill":"string","task":"string"}]}],
              "continueWith": ["string"],
              "interview": {"breakStory":"string"}
            }
            """;

    private final ChatModel chatModel;
    private final ClassificationRules classificationRules;

    public DashScopeRawAnalysisProvider(ChatModel chatModel, ClassificationRules classificationRules) {
        this.chatModel = chatModel;
        this.classificationRules = classificationRules;
    }

    @Override
    public String analyze(AnalyzeRequest request) {
        ChatResponse response = chatModel.call(new Prompt(
                List.of(new SystemMessage(SYSTEM_PROMPT), new UserMessage(userPrompt(request))), options()));
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null
                || response.getResult().getOutput().getText() == null
                || response.getResult().getOutput().getText().isBlank()) {
            throw new IllegalStateException("DashScope returned no analysis content.");
        }
        return response.getResult().getOutput().getText();
    }

    private DashScopeChatOptions options() {
        return DashScopeChatOptions.builder()
                .withModel("qwen-plus")
                .withTemperature(0.2)
                .withResponseFormat(DashScopeResponseFormat.builder()
                        .type(DashScopeResponseFormat.Type.JSON_OBJECT)
                        .build())
                .withStream(false)
                .withEnableSearch(false)
                .withTools(List.of())
                .withToolCallbacks(List.of())
                .withToolNames(java.util.Set.of())
                .withInternalToolExecutionEnabled(false)
                .build();
    }

    private String userPrompt(AnalyzeRequest request) {
        return """
                RESUME:

                %s


                TARGET ACCOUNTING JOB:

                %s


                BREAK MONTHS:

                %d


                BREAK REASON:

                %s


                AVAILABLE HOURS PER WEEK:

                %d


                ACCOUNTING CLASSIFICATION RULES:

                Only classify target-job skills, maximum 12; credentials do not count toward this limit.
                A target-job skill with no resume evidence is LEARN and has resumeQuote null.
                With resume evidence, a skill is READY when breakMonths <= 12 or it is in the READY list.
                With resume evidence, a skill is REFRESH when breakMonths > 12 and it is in the REFRESH list.
                With resume evidence not in either list, default to REFRESH. Do not use confidence to change this.
                READY list: %s
                REFRESH list: %s


                PLAN RULES:

                %s


                BREAK STORY RULES:

                %s


                OUTPUT SCHEMA:

                %s
                """.formatted(
                request.resumeText(),
                request.jobText(),
                request.breakMonths(),
                request.breakReason().name(),
                request.hoursPerWeek(),
                String.join("; ", classificationRules.readySkills()),
                String.join("; ", classificationRules.refreshSkills()),
                PLAN_RULES,
                BREAK_STORY_RULES,
                RESPONSE_SCHEMA);
    }
}
