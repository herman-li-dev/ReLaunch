package com.relaunch.analysis;

import com.relaunch.api.AnalyzeRequest;
import com.relaunch.api.ReentryResponse;

public class AnalysisService {
    private final RawAnalysisProvider provider;
    private final AnalysisResponseProcessor processor;

    public AnalysisService(RawAnalysisProvider provider, AnalysisResponseProcessor processor) {
        this.provider = provider;
        this.processor = processor;
    }

    public ReentryResponse analyze(AnalyzeRequest request, ClassificationRules rules) {
        for (int attempt = 0; attempt < 2; attempt++) {
            String rawOutput;
            try {
                rawOutput = provider.analyze(request);
            } catch (RuntimeException exception) {
                throw new AnalysisFailedException();
            }
            try {
                return processor.process(rawOutput, request.resumeText(), request.breakMonths(), rules);
            } catch (InvalidAnalysisResponseException exception) {
                // A malformed provider response receives exactly one retry.
            } catch (RuntimeException exception) {
                throw new AnalysisFailedException();
            }
        }
        throw new AnalysisFailedException();
    }
}
