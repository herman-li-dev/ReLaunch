package com.relaunch.api;

import java.util.List;

import static com.relaunch.api.ReentryResponse.*;

final class PriyaFixture {
    private PriyaFixture() {}

    static ReentryResponse response() {
        return new ReentryResponse(
                List.of(
                        new Skill("Journal entries", "READY", "Prepared monthly journal entries and supported month-end close across three business units.", "Your resume shows direct experience preparing monthly journal entries."),
                        new Skill("Month-end close", "READY", "Prepared monthly journal entries and supported month-end close across three business units.", "Your resume shows direct experience with month-end close."),
                        new Skill("Account reconciliations", "READY", "Completed bank, balance-sheet, and general-ledger account reconciliations.", "Your resume shows direct experience completing account reconciliations."),
                        new Skill("Financial statements", "READY", "Prepared monthly financial statements and variance-analysis reports for management.", "Your resume shows direct experience preparing financial statements."),
                        new Skill("Variance analysis", "READY", "Prepared monthly financial statements and variance-analysis reports for management.", "Your resume shows direct experience with variance analysis."),
                        new Skill("Audit support", "READY", "Supported annual external audits by preparing schedules and responding to auditor requests.", "Your resume shows direct experience supporting external audits."),
                        new Skill("Excel", "REFRESH", "Used Oracle ERP and Excel for accounting reports and account analysis.", "Your resume shows prior Excel use, and its tools and workflows are worth refreshing."),
                        new Skill("NetSuite", "LEARN", null, "The target role prefers NetSuite, but your resume does not show prior NetSuite experience."),
                        new Skill("Power BI", "LEARN", null, "The target role lists Power BI, but your resume does not show prior Power BI experience.")
                ),
                List.of(new Credential("CPA", "CPA (Chartered Professional Accountant), 2021")),
                List.of(
                        new PlanWeek(1, 270, List.of(
                                new PlanBlock(60, "Excel", "Recreate a simple variance-analysis report using sample financial data."),
                                new PlanBlock(60, "NetSuite", "Explore the current NetSuite interface and identify the basic month-end workflow."),
                                new PlanBlock(60, "Power BI", "Build a simple accounting dashboard from sample financial data."),
                                new PlanBlock(45, "Excel", "Rebuild a basic account reconciliation schedule in Excel."),
                                new PlanBlock(45, "NetSuite", "Practice entering sample journal entries in a NetSuite training environment.")
                        )),
                        new PlanWeek(2, 270, List.of(
                                new PlanBlock(60, "Excel", "Complete one sample bank reconciliation in Excel."),
                                new PlanBlock(60, "NetSuite", "Review a sample NetSuite month-end close checklist."),
                                new PlanBlock(60, "Power BI", "Create a simple variance visual using sample accounting data."),
                                new PlanBlock(45, "Excel", "Use formulas to compare actual and budgeted expenses."),
                                new PlanBlock(45, "Power BI", "Explore how filters change a sample financial report.")
                        )),
                        new PlanWeek(3, 210, List.of(
                                new PlanBlock(45, "Excel", "Recreate a simple variance-analysis report using sample financial data."),
                                new PlanBlock(60, "NetSuite", "Identify the basic reconciliation workflow in a NetSuite training environment."),
                                new PlanBlock(60, "Power BI", "Build a simple monthly close summary using sample data."),
                                new PlanBlock(45, "Resume update", "Update your resume with skills you refreshed and can now confidently discuss.")
                        ))
                ),
                List.of(),
                new Interview("I took parental leave after building accounting experience in journal entries, month-end close, and account reconciliations. I'm now refreshing Excel and preparing to learn NetSuite and Power BI for this role. I'm ready to bring my accounting experience back to a Senior Accountant team.")
        );
    }
}
