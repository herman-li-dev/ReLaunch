package com.relaunch.analysis;

public class AnalysisFailedException extends RuntimeException {
    public AnalysisFailedException() {
        super("Analysis failed.");
    }
}
