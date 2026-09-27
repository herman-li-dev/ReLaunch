package com.relaunch.api;

import java.util.List;

public record ReentryResponse(
        List<Skill> skills,
        List<Credential> credentials,
        List<PlanWeek> plan,
        List<String> continueWith,
        Interview interview
) {
    public record Skill(String name, String status, String resumeQuote, String reason) {}
    public record Credential(String name, String resumeQuote) {}
    public record PlanWeek(int week, int totalMinutes, List<PlanBlock> blocks) {}
    public record PlanBlock(int minutes, String skill, String task) {}
    public record Interview(String breakStory) {}
}
