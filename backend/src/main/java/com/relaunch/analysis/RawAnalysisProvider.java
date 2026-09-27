package com.relaunch.analysis;

import com.relaunch.api.AnalyzeRequest;

@FunctionalInterface
public interface RawAnalysisProvider {
    String analyze(AnalyzeRequest request);
}
