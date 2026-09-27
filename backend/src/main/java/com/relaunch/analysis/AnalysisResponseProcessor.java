package com.relaunch.analysis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.relaunch.api.ReentryResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AnalysisResponseProcessor {
    static final String MISSING_EVIDENCE_REASON = "We couldn't find evidence of this skill in your experience.";
    private static final Set<String> STATUSES = Set.of("READY", "REFRESH", "LEARN");

    private final ObjectMapper objectMapper;

    public AnalysisResponseProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ReentryResponse process(String rawOutput, String resumeText, int breakMonths, ClassificationRules rules) {
        JsonNode root = parse(rawOutput);
        requireObject(root, "response");
        JsonNode skills = requiredArray(root, "skills");
        if (skills.size() > 12) {
            throw new InvalidAnalysisResponseException();
        }
        JsonNode credentials = requiredArray(root, "credentials");
        JsonNode plan = requiredArray(root, "plan");
        JsonNode continueWith = requiredArray(root, "continueWith");
        JsonNode interview = requiredObject(root, "interview");

        List<ReentryResponse.Skill> processedSkills = new ArrayList<>();
        for (JsonNode skill : skills) {
            processedSkills.add(processSkill(skill, resumeText, breakMonths, rules));
        }

        List<ReentryResponse.Credential> processedCredentials = new ArrayList<>();
        for (JsonNode credential : credentials) {
            ReentryResponse.Credential processed = processCredential(credential, resumeText);
            if (processed != null) {
                processedCredentials.add(processed);
            }
        }

        List<ReentryResponse.PlanWeek> parsedPlan = new ArrayList<>();
        for (JsonNode week : plan) {
            parsedPlan.add(parseWeek(week));
        }
        List<String> parsedContinueWith = new ArrayList<>();
        for (JsonNode item : continueWith) {
            parsedContinueWith.add(requiredText(item, "continueWith item"));
        }

        return new ReentryResponse(processedSkills, processedCredentials, parsedPlan, parsedContinueWith,
                new ReentryResponse.Interview(requiredText(interview, "breakStory")));
    }

    private JsonNode parse(String rawOutput) {
        if (rawOutput == null) {
            throw new InvalidAnalysisResponseException();
        }
        try {
            return objectMapper.reader()
                    .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .readTree(stripCodeFence(rawOutput));
        } catch (JsonProcessingException exception) {
            throw new InvalidAnalysisResponseException();
        }
    }

    private String stripCodeFence(String rawOutput) {
        String candidate = rawOutput.trim();
        if (!candidate.startsWith("```")) {
            return candidate;
        }
        int lineEnd = candidate.indexOf('\n');
        if (lineEnd < 0) {
            throw new InvalidAnalysisResponseException();
        }
        String opening = candidate.substring(0, lineEnd).trim();
        if (!(opening.equals("```") || opening.equals("```json")) || !candidate.endsWith("```")) {
            throw new InvalidAnalysisResponseException();
        }
        return candidate.substring(lineEnd + 1, candidate.length() - 3).trim();
    }

    private ReentryResponse.Skill processSkill(JsonNode skill, String resumeText, int breakMonths, ClassificationRules rules) {
        requireObject(skill, "skill");
        String name = requiredText(skill, "name");
        String status = requiredText(skill, "status");
        if (!STATUSES.contains(status)) {
            throw new InvalidAnalysisResponseException();
        }
        JsonNode quoteNode = requiredField(skill, "resumeQuote");
        String quote = nullableText(quoteNode, "resumeQuote");
        String reason = requiredText(skill, "reason");

        if (status.equals("LEARN")) {
            return new ReentryResponse.Skill(name, "LEARN", null, reason);
        }
        if (!isVerifiedQuote(quote, resumeText)) {
            return new ReentryResponse.Skill(name, "LEARN", null, MISSING_EVIDENCE_REASON);
        }
        return new ReentryResponse.Skill(name, rules.classify(name, breakMonths), quote, reason);
    }

    private ReentryResponse.Credential processCredential(JsonNode credential, String resumeText) {
        requireObject(credential, "credential");
        String name = requiredText(credential, "name");
        String quote = nullableText(requiredField(credential, "resumeQuote"), "resumeQuote");
        return isVerifiedQuote(quote, resumeText) ? new ReentryResponse.Credential(name, quote) : null;
    }

    private ReentryResponse.PlanWeek parseWeek(JsonNode week) {
        requireObject(week, "plan week");
        int number = requiredInt(week, "week");
        int totalMinutes = requiredInt(week, "totalMinutes");
        List<ReentryResponse.PlanBlock> blocks = new ArrayList<>();
        for (JsonNode block : requiredArray(week, "blocks")) {
            requireObject(block, "plan block");
            blocks.add(new ReentryResponse.PlanBlock(requiredInt(block, "minutes"), requiredText(block, "skill"), requiredText(block, "task")));
        }
        return new ReentryResponse.PlanWeek(number, totalMinutes, blocks);
    }

    private boolean isVerifiedQuote(String quote, String resumeText) {
        return quote != null && !quote.isBlank() && quote.length() <= 200 && resumeText != null
                && normalizeWhitespace(resumeText).contains(normalizeWhitespace(quote));
    }

    private String normalizeWhitespace(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

    private JsonNode requiredObject(JsonNode parent, String field) {
        JsonNode value = parent.isObject() && parent.has(field) ? parent.get(field) : null;
        requireObject(value, field);
        return value;
    }

    private JsonNode requiredArray(JsonNode parent, String field) {
        JsonNode value = parent.isObject() && parent.has(field) ? parent.get(field) : null;
        if (value == null || !value.isArray()) {
            throw new InvalidAnalysisResponseException();
        }
        return value;
    }

    private JsonNode requiredField(JsonNode parent, String field) {
        if (!parent.has(field)) {
            throw new InvalidAnalysisResponseException();
        }
        return parent.get(field);
    }

    private void requireObject(JsonNode node, String ignoredName) {
        if (node == null || !node.isObject()) {
            throw new InvalidAnalysisResponseException();
        }
    }

    private String requiredText(JsonNode parent, String field) {
        JsonNode value = parent.isObject() ? requiredField(parent, field) : parent;
        if (!value.isTextual() || value.asText().isBlank()) {
            throw new InvalidAnalysisResponseException();
        }
        return value.asText();
    }

    private String nullableText(JsonNode value, String ignoredName) {
        if (value.isNull()) {
            return null;
        }
        if (!value.isTextual()) {
            throw new InvalidAnalysisResponseException();
        }
        return value.asText();
    }

    private int requiredInt(JsonNode parent, String field) {
        JsonNode value = requiredField(parent, field);
        if (!value.isInt()) {
            throw new InvalidAnalysisResponseException();
        }
        return value.intValue();
    }
}

class InvalidAnalysisResponseException extends RuntimeException {
}
