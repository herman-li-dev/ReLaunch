package com.relaunch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ReentryApiTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private static final String PRIYA_RESUME = """
            Priya Sharma — Senior Accountant, CPA

            Harbourline Services, Vancouver
            Accountant / Senior Accountant
            June 2018 – August 2024

            - Prepared monthly journal entries and supported month-end close across three business units.
            - Completed bank, balance-sheet, and general-ledger account reconciliations.
            - Prepared monthly financial statements and variance-analysis reports for management.
            - Used Oracle ERP and Excel for accounting reports and account analysis.
            - Supported annual external audits by preparing schedules and responding to auditor requests.
            - Worked with department managers to investigate expense and budget variances.

            Education:
            Bachelor of Commerce — Accounting

            Designation:
            CPA (Chartered Professional Accountant), 2021
            """;

    private static final String MAYA_RESUME = """
            Maya Chen — Accounting Assistant

            North Shore Distribution Ltd., Vancouver
            Accounting Assistant
            September 2019 – August 2023

            - Processed accounts-payable invoices and prepared weekly payment runs.
            - Prepared customer invoices and followed up on outstanding accounts receivable.
            - Completed monthly bank and corporate credit-card reconciliations.
            - Posted recurring journal entries and supported the month-end close process.
            - Maintained accounting schedules and reports using Excel and QuickBooks Online.
            - Prepared invoices, reconciliations, and supporting schedules for the annual audit.

            Education:
            Diploma in Accounting
            """;

    @Test
    void healthIsExactlyOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"ok\"}"));
    }

    @Test
    void demoReturnsTheValidatedPriyaFixture() throws Exception {
        mockMvc.perform(get("/api/reentry/demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills", hasSize(9)))
                .andExpect(jsonPath("$.skills[?(@.status == 'READY')]", hasSize(6)))
                .andExpect(jsonPath("$.skills[?(@.status == 'REFRESH')]", hasSize(1)))
                .andExpect(jsonPath("$.skills[?(@.status == 'LEARN')]", hasSize(2)))
                .andExpect(jsonPath("$.skills[6].name").value("Excel"))
                .andExpect(jsonPath("$.skills[6].resumeQuote").value("Used Oracle ERP and Excel for accounting reports and account analysis."))
                .andExpect(jsonPath("$.skills[7].resumeQuote").value(nullValue()))
                .andExpect(jsonPath("$.credentials[0].name").value("CPA"))
                .andExpect(jsonPath("$.plan", hasSize(3)))
                .andExpect(jsonPath("$.plan[0].totalMinutes").value(270))
                .andExpect(jsonPath("$.plan[2].blocks[3].task").value("Update your resume with skills you refreshed and can now confidently discuss."))
                .andExpect(jsonPath("$.continueWith", empty()))
                .andExpect(jsonPath("$.interview.breakStory", containsString("parental leave")));
    }

    @Test
    void analyzeReturnsTheSameFixtureForValidPhaseOneInput() throws Exception {
        mockMvc.perform(post("/api/reentry/analyze").contentType(MediaType.APPLICATION_JSON).content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills", hasSize(9)))
                .andExpect(jsonPath("$.continueWith", empty()));
    }

    @Test
    void invalidBoundsReturnSpecifiedFieldErrors() throws Exception {
        mockMvc.perform(post("/api/reentry/analyze").contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().replace("\"hoursPerWeek\":5", "\"hoursPerWeek\":0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.fields.hoursPerWeek").value("Must be between 1 and 40."));
    }

    @Test
    void resumeShorterThanTwoHundredCharactersReturnsInvalidInput() throws Exception {
        mockMvc.perform(post("/api/reentry/analyze").contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().replace("\"" + "x".repeat(200) + "\"", "\"too short\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.fields.resumeText").value("Must be between 200 and 15000 characters."));
    }

    @Test
    void fixtureEvidenceAndPlanRespectFrozenContracts() throws Exception {
        String json = mockMvc.perform(get("/api/reentry/demo"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode fixture = objectMapper.readTree(json);

        for (JsonNode skill : fixture.path("skills")) {
            assertThat(skill.path("reason").asText()).isNotBlank();
            if (skill.path("status").asText().equals("LEARN")) {
                assertThat(skill.path("resumeQuote").isNull()).isTrue();
            } else {
                String quote = skill.path("resumeQuote").asText();
                assertThat(quote).hasSizeLessThanOrEqualTo(200);
                assertThat(PRIYA_RESUME).contains(quote);
            }
        }
        for (JsonNode credential : fixture.path("credentials")) {
            assertThat(PRIYA_RESUME).contains(credential.path("resumeQuote").asText());
        }

        Set<Integer> allowedDurations = Set.of(30, 45, 60);
        assertThat(fixture.path("plan")).hasSizeLessThanOrEqualTo(4);
        for (JsonNode week : fixture.path("plan")) {
            int calculatedTotal = 0;
            for (JsonNode block : week.path("blocks")) {
                int minutes = block.path("minutes").asInt();
                assertThat(allowedDurations).contains(minutes);
                calculatedTotal += minutes;
            }
            assertThat(week.path("totalMinutes").asInt()).isEqualTo(calculatedTotal).isLessThanOrEqualTo(300);
        }
        JsonNode finalWeek = fixture.path("plan").get(fixture.path("plan").size() - 1);
        assertThat(finalWeek.path("blocks").toString())
                .contains("Update your resume with skills you refreshed and can now confidently discuss.");

        String story = fixture.path("interview").path("breakStory").asText();
        assertThat(story).startsWith("I ").doesNotContainIgnoringCase("sorry", "apolog");
        assertThat(story.split("(?<=[.!?])\\s+")).hasSizeBetween(3, 4);
    }

    @Test
    void mayaJuniorAccountantExampleMeetsItsFixedContract() throws Exception {
        String body = mockMvc.perform(get("/api/reentry/examples/maya-junior-accountant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills", hasSize(10)))
                .andExpect(jsonPath("$.skills[?(@.status == 'READY')]", hasSize(4)))
                .andExpect(jsonPath("$.skills[?(@.status == 'REFRESH')]", hasSize(2)))
                .andExpect(jsonPath("$.skills[?(@.status == 'LEARN')]", hasSize(4)))
                .andExpect(jsonPath("$.credentials", empty()))
                .andExpect(jsonPath("$.plan", hasSize(3)))
                .andExpect(jsonPath("$.plan[2].blocks[0].task").value(
                        "Update your resume with skills you refreshed and can now confidently discuss."))
                .andReturn().getResponse().getContentAsString();

        JsonNode response = objectMapper.readTree(body);
        response.path("skills").forEach(skill -> {
            assertThat(skill.path("reason").asText()).isNotBlank();
            if ("LEARN".equals(skill.path("status").asText())) {
                assertThat(skill.path("resumeQuote").isNull()).isTrue();
            } else {
                String quote = skill.path("resumeQuote").asText();
                assertThat(quote).hasSizeLessThanOrEqualTo(200);
                assertThat(MAYA_RESUME).contains(quote);
            }
        });

        Set<String> plannedSkills = Set.of(
                "Excel",
                "Computerized accounting systems / QuickBooks Online",
                "General ledger reconciliation",
                "Expense reports and petty-cash reimbursements",
                "Property-management software",
                "Project and site manager liaison");
        response.path("plan").forEach(week -> {
            int minutes = 0;
            for (JsonNode block : week.path("blocks")) {
                int blockMinutes = block.path("minutes").asInt();
                assertThat(blockMinutes).isIn(30, 45, 60);
                minutes += blockMinutes;
                if (!"Resume update".equals(block.path("skill").asText())) {
                    assertThat(plannedSkills).contains(block.path("skill").asText());
                }
            }
            assertThat(minutes).isEqualTo(week.path("totalMinutes").asInt());
            assertThat(minutes).isLessThanOrEqualTo(240);
        });
    }

    private String validRequest() {
        return "{\"resumeText\":\"" + "x".repeat(200)
                + "\",\"jobText\":\"" + "y".repeat(100)
                + "\",\"breakMonths\":24,\"breakReason\":\"PARENTAL_LEAVE\",\"hoursPerWeek\":5}";
    }
}
