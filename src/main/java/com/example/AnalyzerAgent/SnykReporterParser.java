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
    //  public static void parse() {
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

    //         // Step 3: Process vulnerabilities and create Jira tickets
    //         int processedCount = 0;
    //         int maxTicketsPerRun = 3; // limit tickets per run
    //         for (JsonNode v : highRiskVulns) {
    //             if (processedCount >= maxTicketsPerRun) break;

    //             String title = v.get("title").asText();
    //             String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
    //             String pkg = v.get("packageName").asText();
    //             double cvssScore = v.has("cvssScore") ? v.get("cvssScore").asDouble() : 0.0;
    //             System.out.println("Processing high-risk vulnerability: " + title);

    //             // Call AI for risk analysis (Groq)
    //             RiskAnalysis risk = AIRiskAnalyzer.analyze("SNYK", title, severity, pkg);

    //             if (risk == null) {
    //                 System.out.println("AI analysis failed.");
    //                 continue;
    //             }

    //             //String summary = "[SNYK] " + title;
    //             String summary = "[SNYK] " + title;
    //           //  JiraService.createTicketIfNeeded(summary, risk); // Implement your Jira ticket logic here

    //             processedCount++;
    //             Thread.sleep(2000); // avoid API spam
    //         }

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

   // ------single card code--------
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
        for (JsonNode v : vulns) {
            double cvssScore = v.has("cvssScore") ? v.get("cvssScore").asDouble() : 0.0;
            String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
            String title = v.get("title").asText();

            if (cvssScore >= 8.0 && (severity.equalsIgnoreCase("critical") || title.toLowerCase().contains("remote code execution"))) {
                highRiskVulns.add(v);
            }
        }

        if (highRiskVulns.isEmpty()) {
            System.out.println("No high-risk Snyk vulnerabilities found.");
            return;
        }

        // Step 2: Aggregate issues for AI
        List<String> issueSummaries = new ArrayList<>();
        for (JsonNode v : highRiskVulns) {
            String title = v.get("title").asText();
            String pkg = v.get("packageName").asText();
            String severity = v.has("severityWithCritical") ? v.get("severityWithCritical").asText() : v.get("severity").asText();
            double cvssScore = v.has("cvssScore") ? v.get("cvssScore").asDouble() : 0.0;

            issueSummaries.add("[" + severity + "] " + pkg + " | " + title + " | CVSS: " + cvssScore);
        }

        // Step 3: Build AI prompt
        StringBuilder aggregatedIssues = new StringBuilder();
        aggregatedIssues.append("You are a DevSecOps security expert.\n");
        aggregatedIssues.append("Analyze the following Snyk vulnerabilities as a single security report:\n\n");
        for (String s : issueSummaries) {
            aggregatedIssues.append("- ").append(s).append("\n");
        }
        aggregatedIssues.append("\nProvide overall analysis in JSON format:\n");
        aggregatedIssues.append("{\"criticality\":\"Low/Medium/High/Critical\", \"riskScore\":0-10, \"businessImpact\":\"text\", \"remediation\":\"text\"}");

        // Step 4: Call AI once
        System.out.println("Calling AI for overall Snyk risk analysis...");
        RiskAnalysis risk = AIRiskAnalyzer.analyze(
                "SNYK",
                aggregatedIssues.toString(), // pass all issues here
                "CRITICAL",
                "code"
        );

        if (risk == null) {
            System.out.println("AI analysis failed. Using fallback risk.");
            return;
        }

        // Step 5: Create single Jira ticket
        String summary = "[SNYK] " + highRiskVulns.size() + " High-Risk Vulnerabilities Detected";

        StringBuilder description = new StringBuilder();
        description.append("Snyk Vulnerabilities Report:\n\n");
        for (String s : issueSummaries) {
            description.append(s).append("\n\n");
        }

        description.append("\n=== AI Risk Analysis ===\n");
        description.append("Criticality: ").append(risk.criticality).append("\n");
        description.append("Risk Score: ").append(risk.riskScore).append("\n");
        description.append("Impact: ").append(risk.businessImpact).append("\n");
        description.append("Remediation: ").append(risk.remediation).append("\n");

        // Only one ticket
        JiraService.createTicketIfNeeded(summary, risk);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

}