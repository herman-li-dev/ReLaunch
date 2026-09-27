package com.relaunch.api;

import com.relaunch.analysis.AnalysisService;
import com.relaunch.analysis.ClassificationRules;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reentry")
public class ReentryController {
    private final AnalysisService analysisService;
    private final ClassificationRules classificationRules;

    public ReentryController(AnalysisService analysisService, ClassificationRules classificationRules) {
        this.analysisService = analysisService;
        this.classificationRules = classificationRules;
    }

    @GetMapping("/demo")
    public ReentryResponse demo() {
        return PriyaFixture.response();
    }

    @GetMapping("/examples/maya-junior-accountant")
    public ReentryResponse mayaJuniorAccountantExample() {
        return MayaJuniorAccountantFixture.response();
    }

    @PostMapping("/analyze")
    public ReentryResponse analyze(@Valid @RequestBody AnalyzeRequest request) {
        return analysisService.analyze(request, classificationRules);
    }
}
