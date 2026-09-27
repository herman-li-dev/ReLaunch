package com.relaunch.api;

import java.util.List;

import static com.relaunch.api.ReentryResponse.Interview;
import static com.relaunch.api.ReentryResponse.PlanBlock;
import static com.relaunch.api.ReentryResponse.PlanWeek;
import static com.relaunch.api.ReentryResponse.Skill;

final class MayaJuniorAccountantFixture {
    private static final String RESUME_UPDATE =
            "Update your resume with skills you refreshed and can now confidently discuss.";

    private MayaJuniorAccountantFixture() {
    }

    static ReentryResponse response() {
        return new ReentryResponse(
                List.of(
                        new Skill("Accounts payable processing", "READY",
                                "Processed accounts-payable invoices and prepared weekly payment runs.",
                                "Your resume shows accounts-payable invoice processing and weekly payment runs."),
                        new Skill("Accounts receivable processing", "READY",
                                "Prepared customer invoices and followed up on outstanding accounts receivable.",
                                "Your resume shows customer invoicing and accounts-receivable follow-up."),
                        new Skill("Bank and credit-card reconciliations", "READY",
                                "Completed monthly bank and corporate credit-card reconciliations.",
                                "Your resume shows monthly bank and corporate credit-card reconciliations."),
                        new Skill("Year-end working papers and schedules", "READY",
                                "Prepared invoices, reconciliations, and supporting schedules for the annual audit.",
                                "Your resume shows supporting schedules prepared for an annual audit."),
                        new Skill("Excel", "REFRESH",
                                "Maintained accounting schedules and reports using Excel and QuickBooks Online.",
                                "Your resume shows Excel experience; refresh it for this role."),
                        new Skill("Computerized accounting systems / QuickBooks Online", "REFRESH",
                                "Maintained accounting schedules and reports using Excel and QuickBooks Online.",
                                "Your resume shows QuickBooks Online experience; refresh it for this role."),
                        new Skill("General ledger reconciliation", "LEARN", null,
                                "The role requires general-ledger reconciliation, which is not shown directly in your resume."),
                        new Skill("Expense reports and petty-cash reimbursements", "LEARN", null,
                                "The role requires expense-report and petty-cash work, which is not shown in your resume."),
                        new Skill("Property-management software", "LEARN", null,
                                "Property-management software is an asset for this role and is not shown in your resume."),
                        new Skill("Project and site manager liaison", "LEARN", null,
                                "The role calls for direct liaison with project and site managers, which is not shown in your resume.")),
                List.of(),
                List.of(
                        new PlanWeek(1, 150, List.of(
                                new PlanBlock(60, "Excel",
                                        "Rebuild an Excel reconciliation template using sample property accounting data."),
                                new PlanBlock(60, "General ledger reconciliation",
                                        "Reconcile one sample general-ledger account and document outstanding items."),
                                new PlanBlock(30, "Expense reports and petty-cash reimbursements",
                                        "Review and process sample expense reports and petty-cash reimbursements."))),
                        new PlanWeek(2, 120, List.of(
                                new PlanBlock(45, "Computerized accounting systems / QuickBooks Online",
                                        "Refresh a sample AP/AR workflow in QuickBooks Online using non-production data."),
                                new PlanBlock(45, "Property-management software",
                                        "Review a current property-management accounting interface and identify its invoice and reconciliation workflows."),
                                new PlanBlock(30, "Project and site manager liaison",
                                        "Draft a concise email to a project manager about an invoice discrepancy."))),
                        new PlanWeek(3, 45, List.of(
                                new PlanBlock(45, "Resume update", RESUME_UPDATE)))),
                List.of(),
                new Interview("I took parental leave after building hands-on experience in accounts payable, accounts receivable, and account reconciliations. I'm preparing to refresh Excel and QuickBooks Online while building familiarity with general-ledger reconciliation and property-management software. I'm ready to return to a Junior Accountant role and support project and property management teams."));
    }
}
