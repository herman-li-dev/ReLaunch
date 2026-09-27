package com.relaunch.analysis;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record ClassificationRules(Set<String> readySkills, Set<String> refreshSkills) {
    public ClassificationRules {
        readySkills = normalize(Objects.requireNonNull(readySkills));
        refreshSkills = normalize(Objects.requireNonNull(refreshSkills));
    }

    public String classify(String skillName, int breakMonths) {
        String normalizedSkillName = normalize(skillName);
        if (breakMonths <= 12 || readySkills.contains(normalizedSkillName)) {
            return "READY";
        }
        if (refreshSkills.contains(normalizedSkillName)) {
            return "REFRESH";
        }
        return "REFRESH";
    }

    private static Set<String> normalize(Set<String> skills) {
        return skills.stream().map(ClassificationRules::normalize).collect(Collectors.toUnmodifiableSet());
    }

    private static String normalize(String skill) {
        return Objects.requireNonNull(skill).trim().toLowerCase(Locale.ROOT);
    }
}
