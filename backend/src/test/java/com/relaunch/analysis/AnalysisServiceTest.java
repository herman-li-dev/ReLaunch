package com.relaunch.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relaunch.api.AnalyzeRequest;
import com.relaunch.api.BreakReason;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class AnalysisServiceTest {
    private static final AnalyzeRequest REQUEST = new AnalyzeRequest("x".repeat(200), "y".repeat(100), 24, BreakReason.PARENTAL_LEAVE, 5);
    private static final ClassificationRules RULES = new ClassificationRules(Set.of(), Set.of("Excel"));

    @Test
    void retriesOnceThenAcceptsValidResponse() {
        AtomicInteger calls = new AtomicInteger();
        AnalysisService service = new AnalysisService(request -> calls.incrementAndGet() == 1 ? "not json" : validResponse(), processor());

        assertThat(service.analyze(REQUEST, RULES).skills()).hasSize(1);
        assertThat(calls).hasValue(2);
    }

    @Test
    void failsSafelyAfterTwoInvalidResponses() {
        AtomicInteger calls = new AtomicInteger();
        AnalysisService service = new AnalysisService(request -> { calls.incrementAndGet(); return "raw provider secret"; }, processor());

        assertThatThrownBy(() -> service.analyze(REQUEST, RULES))
                .isInstanceOf(AnalysisFailedException.class)
                .hasMessage("Analysis failed.")
                .hasNoCause();
        assertThat(calls).hasValue(2);
    }

    @Test
    void translatesProviderExceptionsWithoutRetryingOrLeakingDetails() {
        AtomicInteger calls = new AtomicInteger();
        AnalysisService service = new AnalysisService(request -> {
            calls.incrementAndGet();
            throw new IllegalStateException("provider secret and resume text");
        }, processor());

        assertThatThrownBy(() -> service.analyze(REQUEST, RULES))
                .isInstanceOf(AnalysisFailedException.class)
                .hasMessage("Analysis failed.")
                .hasNoCause();
        assertThat(calls).hasValue(1);
    }

    @Test
    void handlerMapsSafeFailureToSpecified502Response() {
        var response = new com.relaunch.api.ApiExceptionHandler().analysisFailed();
        assertThat(response.getStatusCode().value()).isEqualTo(502);
        assertThat(response.getBody()).containsEntry("error", "ANALYSIS_FAILED").hasSize(1);
    }

    private AnalysisResponseProcessor processor() {
        return new AnalysisResponseProcessor(new ObjectMapper());
    }

    private static String validResponse() {
        return """
                {"skills":[{"name":"Excel","status":"READY","resumeQuote":"xxxxxxxxxx","reason":"Evidence exists."}],
                "credentials":[],"plan":[],"continueWith":[],"interview":{"breakStory":"I am ready."}}
                """;
    }
}
