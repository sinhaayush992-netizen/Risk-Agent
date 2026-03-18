package com.example.AnalyzerAgent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SnykReporterParser {

    // public static void parse() {
    //     System.out.println("Reading Snyk Report...");

    //     try {
    //         ObjectMapper mapper = new ObjectMapper();
    //         JsonNode root = mapper.readTree(new File("snyk-report.json"));
    //         JsonNode vulns = root.get("vulnerabilities");

    //         if (vulns == null || !vulns.isArray() || vulns.size() == 0) {
    //             System.out.println("Snyk report is empty. Skipping.");
    //             return;
    //         }

    //         // Step 1: Filter high-risk vulnerabilities
    //         List<JsonNode> highRiskVulns = new ArrayList<>();
    //         int vulnerabilityNumber = 0;
    //         for (JsonNode v : vulns) {
    //             vulnerabilityNumber++;
    //             double cvssScore = v.has("cvssScore") ? v.get("cvssScore").asDouble() : 0.0;
    //             String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
    //             String title = v.get("title").asText();

    //             if (cvssScore >= 8.0 && (severity.equalsIgnoreCase("critical") || title.toLowerCase().contains("remote code execution"))) {
    //                 highRiskVulns.add(v);
    //             } else {
    //                 System.out.println("Skipping low-risk vulnerability #" + vulnerabilityNumber + ": " + title +
    //                         " (CVSS: " + cvssScore + ", Severity: " + severity + ")");
    //             }
    //         }

    //         // Step 2: Print filtered high-risk vulnerabilities
    //         System.out.println("\n=== High-Risk Vulnerabilities ===");
    //         for (JsonNode v : highRiskVulns) {
    //             String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
    //             System.out.println("- " + v.get("title").asText() + " | CVSS: " + v.get("cvssScore").asDouble() + " | Severity: " + severity);
    //         }
    //         System.out.println("================================\n");

    //         // Step 3: Process filtered vulnerabilities
    //         int processedCount = 0;
    //         int maxTicketsPerRun = 2; // limit tickets per run
    //         for (JsonNode v : highRiskVulns) {
    //             if (processedCount >= maxTicketsPerRun) break;

    //             String title = v.get("title").asText();
    //             String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
    //             String pkg = v.get("packageName").asText();

    //             System.out.println("Processing high-risk vulnerability: " + title);

    //             // Call AI for risk analysis
    //             RiskAnalysis risk = AIRiskAnalyzer.analyze("SNYK", title, severity, pkg);

    //             if (risk == null) {
    //                 System.out.println("AI analysis failed. Creating Jira ticket anyway.");
    //                 risk = new RiskAnalysis();
    //                 risk.criticality = severity.equalsIgnoreCase("critical") ? "Critical" : "High";
    //                 risk.riskScore = (int) Math.round(v.has("cvssScore") ? v.get("cvssScore").asDouble() : 9);
    //                 risk.businessImpact = "Vulnerability detected in package: " + pkg + ". Manual review required.";
    //                 risk.remediation = "Refer to Snyk report for remediation steps.";
    //             }

    //             String summary = "[SNYK] " + title;
    //             JiraService.createTicketIfNeeded(summary, risk);

    //             processedCount++;
    //             Thread.sleep(2000);
    //         }

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    //----------------For Groq-------------------------
     public static void parse() {
        System.out.println("Reading Snyk Report...");

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File("snyk-report.json"));
            JsonNode vulns = root.get("vulnerabilities");

            if (vulns == null || !vulns.isArray() || vulns.size() == 0) {
                System.out.println("Snyk report is empty. Skipping.");
                return;
            }

            // Step 1: Filter high-risk vulnerabilities
            List<JsonNode> highRiskVulns = new ArrayList<>();
            int vulnerabilityNumber = 0;
            for (JsonNode v : vulns) {
                vulnerabilityNumber++;
                double cvssScore = v.has("cvssScore") ? v.get("cvssScore").asDouble() : 0.0;
                String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
                String title = v.get("title").asText();

                if (cvssScore >= 8.0 && (severity.equalsIgnoreCase("critical") || title.toLowerCase().contains("remote code execution"))) {
                    highRiskVulns.add(v);
                } else {
                    System.out.println("Skipping low-risk vulnerability #" + vulnerabilityNumber + ": " + title +
                            " (CVSS: " + cvssScore + ", Severity: " + severity + ")");
                }
            }

            // Step 2: Print filtered high-risk vulnerabilities
            System.out.println("\n=== High-Risk Vulnerabilities ===");
            for (JsonNode v : highRiskVulns) {
                String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
                System.out.println("- " + v.get("title").asText() + " | CVSS: " + v.get("cvssScore").asDouble() + " | Severity: " + severity);
            }
            System.out.println("================================\n");

            // Step 3: Process vulnerabilities and create Jira tickets
            int processedCount = 0;
            int maxTicketsPerRun = 3; // limit tickets per run
            for (JsonNode v : highRiskVulns) {
                if (processedCount >= maxTicketsPerRun) break;

                String title = v.get("title").asText();
                String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
                String pkg = v.get("packageName").asText();
                double cvssScore = v.has("cvssScore") ? v.get("cvssScore").asDouble() : 0.0;
                System.out.println("Processing high-risk vulnerability: " + title);

                // Call AI for risk analysis (Groq)
                RiskAnalysis risk = AIRiskAnalyzer.analyze("SNYK", title, severity, pkg);

                if (risk == null) {
                    System.out.println("AI analysis failed.");
                    continue;
                }

                //String summary = "[SNYK] " + title;
                String summary = "[SNYK] " + title + " | " + pkg ;
                JiraService.createTicketIfNeeded(summary, risk); // Implement your Jira ticket logic here

                processedCount++;
                Thread.sleep(2000); // avoid API spam
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}