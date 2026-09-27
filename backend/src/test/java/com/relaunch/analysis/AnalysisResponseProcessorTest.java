package com.relaunch.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relaunch.api.ReentryResponse;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AnalysisResponseProcessorTest {
    private static final String RESUME = "I used Excel. CPA (Chartered Professional Accountant), 2021.";
    private final AnalysisResponseProcessor processor = new AnalysisResponseProcessor(new ObjectMapper());
    private final ClassificationRules rules = new ClassificationRules(Set.of("core accounting"), Set.of("excel"));

    @Test
    void acceptsPlainJsonAndCompleteJsonFence() {
        assertThat(processor.process(validResponse(), RESUME, 24, rules).skills()).hasSize(1);
        assertThat(processor.process("```json\n" + validResponse() + "\n```", RESUME, 24, rules).skills()).hasSize(1);
        assertThat(processor.process("```\n" + validResponse() + "\n```", RESUME, 24, rules).skills()).hasSize(1);
    }

    @Test
    void rejectsMalformedOrStructurallyInvalidResponses() {
        List<String> invalid = List.of(
                "{",
                validResponse() + "{}",
                "{\"skills\":[]}",
                validResponse().replace("\"READY\"", "\"UNKNOWN\""),
                "{\"skills\":{},\"credentials\":[],\"plan\":[],\"continueWith\":[],\"interview\":{\"breakStory\":\"I am ready.\"}}"
        );
        invalid.forEach(raw -> assertThatThrownBy(() -> processor.process(raw, RESUME, 24, rules))
                .isInstanceOf(InvalidAnalysisResponseException.class));
    }

    @Test
    void rejectsMoreThanTwelveSkills() {
        String skill = "{\"name\":\"Excel\",\"status\":\"READY\",\"resumeQuote\":\"I used Excel.\",\"reason\":\"Evidence exists.\"}";
        String raw = "{\"skills\":[" + String.join(",", java.util.Collections.nCopies(13, skill))
                + "],\"credentials\":[],\"plan\":[],\"continueWith\":[],\"interview\":{\"breakStory\":\"I am ready.\"}}";
        assertThatThrownBy(() -> processor.process(raw, RESUME, 24, rules)).isInstanceOf(InvalidAnalysisResponseException.class);
    }

    @Test
    void verifiesEvidenceCredentialsAndParameterizedClassification() {
        String raw = """
                {
                  "skills": [
                    {"name":"Excel","status":"READY","resumeQuote":"I used Excel.","reason":"Evidence exists."},
                    {"name":"Core accounting","status":"REFRESH","resumeQuote":"I used Excel.","reason":"Evidence exists."},
                    {"name":"Oracle","status":"READY","resumeQuote":"I used Oracle.","reason":"Evidence exists."},
                    {"name":"NetSuite","status":"READY","resumeQuote":null,"reason":"Model claimed evidence."}
                  ],
                  "credentials": [
                    {"name":"CPA","resumeQuote":"CPA (Chartered Professional Accountant), 2021"},
                    {"name":"Invalid","resumeQuote":"Not in the resume"}
                  ],
                  "plan": [], "continueWith": [], "interview": {"breakStory":"I am ready."}
                }
                """;
        ReentryResponse result = processor.process(raw, RESUME, 24, rules);

        assertThat(result.skills()).extracting(ReentryResponse.Skill::status)
                .containsExactly("REFRESH", "READY", "LEARN", "LEARN");
        assertThat(result.skills().get(2).reason()).isEqualTo(AnalysisResponseProcessor.MISSING_EVIDENCE_REASON);
        assertThat(result.skills().get(2).resumeQuote()).isNull();
        assertThat(result.skills().get(3).reason()).isEqualTo(AnalysisResponseProcessor.MISSING_EVIDENCE_REASON);
        assertThat(result.skills().get(3).resumeQuote()).isNull();
        assertThat(result.credentials()).extracting(ReentryResponse.Credential::name).containsExactly("CPA");
        assertThat(processor.process(validResponse(), RESUME, 12, rules).skills().getFirst().status()).isEqualTo("READY");
    }

    @Test
    void removesOverlongCredentialQuoteEvenWhenResumeContainsIt() {
        String quote = "x".repeat(201);
        String raw = """
                {"skills":[],"credentials":[{"name":"CPA","resumeQuote":"%s"}],
                "plan":[],"continueWith":[],"interview":{"breakStory":"I am ready."}}
                """.formatted(quote);

        assertThat(processor.process(raw, quote, 24, rules).credentials()).isEmpty();
    }

    @Test
    void normalizesWhitespaceForComparisonAndReturnsOriginalResumeSubstrings() {
        String modelQuote = "  Prepared monthly\tjournal entries and supported month-end close.  ";
        String resume = "Prepared  monthly journal entries\r\nand supported month-end close.";
        String raw = """
                {"skills":[{"name":"Month-end close","status":"READY","resumeQuote":"  Prepared monthly\\tjournal entries and supported month-end close.  ","reason":"Evidence exists."}],
                "credentials":[{"name":"CPA","resumeQuote":"  Prepared monthly\\tjournal entries and supported month-end close.  "}],
                "plan":[],"continueWith":[],"interview":{"breakStory":"I am ready."}}
                """;

        ReentryResponse result = processor.process(raw, resume, 24, rules);
        assertThat(result.skills().getFirst().status()).isEqualTo("REFRESH");
        assertThat(result.skills().getFirst().resumeQuote()).isEqualTo(resume);
        assertThat(result.credentials().getFirst().resumeQuote()).isEqualTo(resume);
    }

    @Test
    void rejectsCaseAndPunctuationOnlyEvidenceDifferences() {
        String raw = """
                {"skills":[
                {"name":"Case","status":"READY","resumeQuote":"i used excel.","reason":"Evidence exists."},
                {"name":"Punctuation","status":"READY","resumeQuote":"I used Excel!","reason":"Evidence exists."}],
                "credentials":[],"plan":[],"continueWith":[],"interview":{"breakStory":"I am ready."}}
                """;

        ReentryResponse result = processor.process(raw, RESUME, 24, rules);
        assertThat(result.skills()).allSatisfy(skill -> {
            assertThat(skill.status()).isEqualTo("LEARN");
            assertThat(skill.resumeQuote()).isNull();
        });
    }

    @Test
    void rejectsEmptyAndWhitespaceOnlyEvidenceForSkillsAndCredentials() {
        String raw = """
                {"skills":[
                {"name":"Empty","status":"READY","resumeQuote":"","reason":"Evidence exists."},
                {"name":"Whitespace","status":"READY","resumeQuote":" \\t ","reason":"Evidence exists."}],
                "credentials":[{"name":"CPA","resumeQuote":" \\t "}],
                "plan":[],"continueWith":[],"interview":{"breakStory":"I am ready."}}
                """;

        ReentryResponse result = processor.process(raw, RESUME, 24, rules);
        assertThat(result.skills()).allSatisfy(skill -> assertThat(skill.status()).isEqualTo("LEARN"));
        assertThat(result.credentials()).isEmpty();
    }

    @Test
    void rejectsTwoHundredAndOneCharacterSkillQuoteEvenWhenItMatchesResume() {
        String quote = "x".repeat(201);
        String raw = """
                {"skills":[{"name":"Excel","status":"READY","resumeQuote":"%s","reason":"Evidence exists."}],
                "credentials":[],"plan":[],"continueWith":[],"interview":{"breakStory":"I am ready."}}
                """.formatted(quote);

        ReentryResponse result = processor.process(raw, quote, 24, rules);
        assertThat(result.skills().getFirst().status()).isEqualTo("LEARN");
        assertThat(result.skills().getFirst().resumeQuote()).isNull();
        assertThat(result.skills().getFirst().reason()).isEqualTo(AnalysisResponseProcessor.MISSING_EVIDENCE_REASON);
    }

    private String validResponse() {
        return """
                {"skills":[{"name":"Excel","status":"READY","resumeQuote":"I used Excel.","reason":"Evidence exists."}],
                "credentials":[{"name":"CPA","resumeQuote":"CPA (Chartered Professional Accountant), 2021"}],
                "plan":[{"week":1,"totalMinutes":30,"blocks":[{"minutes":30,"skill":"Excel","task":"Practice a sample report."}]}],
                "continueWith":[],"interview":{"breakStory":"I am ready."}}
                """;
    }
}
