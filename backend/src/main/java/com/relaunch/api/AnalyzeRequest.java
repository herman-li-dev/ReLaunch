package com.relaunch.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AnalyzeRequest(
        @NotBlank(message = "Resume is required.") @Size(min = 200, max = 15000, message = "Must be between 200 and 15000 characters.") String resumeText,
        @NotBlank(message = "Target job description is required.") @Size(min = 100, max = 10000, message = "Must be between 100 and 10000 characters.") String jobText,
        @NotNull(message = "Time away is required.") @Min(value = 0, message = "Must be between 0 and 240.") @Max(value = 240, message = "Must be between 0 and 240.") Integer breakMonths,
        @NotNull(message = "Break reason is required.") BreakReason breakReason,
        @NotNull(message = "Hours per week is required.") @Min(value = 1, message = "Must be between 1 and 40.") @Max(value = 40, message = "Must be between 1 and 40.") Integer hoursPerWeek
) {}
